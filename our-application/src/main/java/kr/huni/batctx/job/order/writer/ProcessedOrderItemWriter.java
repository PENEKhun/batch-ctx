package kr.huni.batctx.job.order.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Component;

import kr.huni.batctx.job.order.domain.ProcessedOrder;

@Component
public class ProcessedOrderItemWriter implements ItemWriter<ProcessedOrder> {

    private static final String INSERT_PROCESSED_SQL = """
            INSERT INTO processed_orders (pending_order_id, third_party_order_id, status, processed_at)
            VALUES (?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProcessedOrderItemWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends ProcessedOrder> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        List<ProcessedOrder> items = new ArrayList<>(chunk.getItems());
        jdbcTemplate.batchUpdate(INSERT_PROCESSED_SQL, items, items.size(), new ProcessedOrderStatementSetter());
    }

    private static class ProcessedOrderStatementSetter implements ParameterizedPreparedStatementSetter<ProcessedOrder> {
        @Override
        public void setValues(PreparedStatement ps, ProcessedOrder processedOrder) throws SQLException {
            ps.setLong(1, processedOrder.pendingOrderId());
            if (processedOrder.thirdPartyOrderId() != null) {
                ps.setLong(2, processedOrder.thirdPartyOrderId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, processedOrder.status().name());
            ps.setTimestamp(4, Timestamp.from(processedOrder.processedAt()));
        }
    }
}
