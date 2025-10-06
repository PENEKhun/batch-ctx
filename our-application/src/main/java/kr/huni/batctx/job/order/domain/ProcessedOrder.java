package kr.huni.batctx.job.order.domain;

import java.time.Instant;

public record ProcessedOrder(
        Long pendingOrderId,
        Long thirdPartyOrderId,
        OrderProcessingStatus status,
        Instant processedAt
) {
}
