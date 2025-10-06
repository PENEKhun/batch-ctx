package kr.huni.batctx.job.order.domain;

import java.math.BigDecimal;

public record PendingOrder(
        Long id,
        String username,
        BigDecimal totalPrice,
        String appliedYearMonth
) {
}
