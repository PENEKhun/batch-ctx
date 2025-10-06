package kr.huni.batctx.job.order.client;

import java.time.Instant;

import kr.huni.batctx.job.order.domain.OrderProcessingStatus;

public record ThirdPartyOrderResult(
        Long thirdPartyOrderId,
        Instant processedAt,
        OrderProcessingStatus status
) {

    public static ThirdPartyOrderResult created(Long thirdPartyOrderId, Instant createdAt) {
        return new ThirdPartyOrderResult(thirdPartyOrderId, createdAt != null ? createdAt : Instant.now(), OrderProcessingStatus.CREATED);
    }

    public static ThirdPartyOrderResult alreadyExists() {
        return new ThirdPartyOrderResult(null, Instant.now(), OrderProcessingStatus.ALREADY_EXISTS);
    }

}
