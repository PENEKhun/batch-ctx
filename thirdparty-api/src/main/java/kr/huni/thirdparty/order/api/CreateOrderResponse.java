package kr.huni.thirdparty.order.api;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateOrderResponse(
        Long orderId,
        String username,
        BigDecimal totalPrice,
        String appliedYearMonth,
        Instant createdAt
) {
}
