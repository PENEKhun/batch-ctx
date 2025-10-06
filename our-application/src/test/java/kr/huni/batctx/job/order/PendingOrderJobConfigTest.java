package kr.huni.batctx.job.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import kr.huni.batctx.job.order.client.ThirdPartyOrderClient;
import kr.huni.batctx.job.order.client.ThirdPartyOrderResult;
import kr.huni.batctx.job.order.domain.PendingOrder;

@SpringBootTest
@SpringBatchTest
class PendingOrderJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ThirdPartyOrderClient thirdPartyOrderClient;

    @BeforeEach
    void resetTables() {
        jdbcTemplate.update("DELETE FROM processed_orders");
        jdbcTemplate.update("DELETE FROM pending_orders");
        jdbcTemplate.update("INSERT INTO pending_orders (username, total_price, applied_year_month) VALUES (?, ?, ?)",
                "tester", 10000.00, "2025-02");
        jdbcTemplate.update("INSERT INTO pending_orders (username, total_price, applied_year_month) VALUES (?, ?, ?)",
                "second-user", 5500.00, "2025-03");
    }

    @Test
    void processesPendingOrdersAndPersistsResults() throws Exception {
        AtomicLong orderIdSequence = new AtomicLong(1000L);
        when(thirdPartyOrderClient.createOrder(any())).thenAnswer(invocation ->
                ThirdPartyOrderResult.created(orderIdSequence.getAndIncrement(), Instant.now())
        );

        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters());

        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Map<String, Object>> processedRows = jdbcTemplate.queryForList(
                "SELECT pending_order_id, third_party_order_id, status FROM processed_orders ORDER BY pending_order_id"
        );

        assertThat(processedRows).hasSize(2);
        assertThat(processedRows)
                .extracting(row -> row.get("status"))
                .containsExactly("CREATED", "CREATED");
    }

    @Test
    void marksAlreadyProcessedOrdersWhenThirdPartyReportsConflict() throws Exception {
        when(thirdPartyOrderClient.createOrder(any())).thenAnswer(invocation -> {
            PendingOrder pendingOrder = invocation.getArgument(0);
            if ("tester".equals(pendingOrder.username())) {
                return ThirdPartyOrderResult.alreadyExists();
            }
            return ThirdPartyOrderResult.created(2000L, Instant.now());
        });

        JobExecution execution = jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 1)
                .toJobParameters());

        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Map<String, Object>> processedRows = jdbcTemplate.queryForList(
                "SELECT pending_order_id, third_party_order_id, status FROM processed_orders ORDER BY pending_order_id"
        );

        assertThat(processedRows).hasSize(2);
        assertThat(processedRows.get(0).get("status")).isEqualTo("ALREADY_EXISTS");
        assertThat(processedRows.get(1).get("status")).isEqualTo("CREATED");
        assertThat(processedRows.get(0).get("third_party_order_id")).isNull();
        assertThat(((Number) processedRows.get(1).get("third_party_order_id")).longValue()).isEqualTo(2000L);
    }
}
