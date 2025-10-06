package kr.huni.batctx.job.order.reader;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.stereotype.Component;

import kr.huni.batctx.job.order.domain.PendingOrder;

@Component
public class PendingOrderItemReader extends JdbcCursorItemReader<PendingOrder> {

    private static final String SELECT_PENDING_ORDERS_SQL = """
            SELECT po.id, po.username, po.total_price, po.applied_year_month
            FROM pending_orders po
            ORDER BY po.id
            """;

    public PendingOrderItemReader(DataSource dataSource) {
        setDataSource(dataSource);
        setSql(SELECT_PENDING_ORDERS_SQL);
        setRowMapper((rs, rowNum) -> new PendingOrder(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getBigDecimal("total_price"),
                rs.getString("applied_year_month")
        ));
        setVerifyCursorPosition(false);
        setFetchSize(50);
        setName("pendingOrderItemReader");
    }
}
