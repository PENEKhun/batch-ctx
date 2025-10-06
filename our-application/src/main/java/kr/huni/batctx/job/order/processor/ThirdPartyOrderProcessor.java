package kr.huni.batctx.job.order.processor;

import java.time.Instant;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import kr.huni.batctx.job.order.client.ThirdPartyOrderClient;
import kr.huni.batctx.job.order.client.ThirdPartyOrderResult;
import kr.huni.batctx.job.order.domain.PendingOrder;
import kr.huni.batctx.job.order.domain.ProcessedOrder;

@Component
public class ThirdPartyOrderProcessor implements ItemProcessor<PendingOrder, ProcessedOrder> {

    private final ThirdPartyOrderClient thirdPartyOrderClient;

    public ThirdPartyOrderProcessor(ThirdPartyOrderClient thirdPartyOrderClient) {
        this.thirdPartyOrderClient = thirdPartyOrderClient;
    }

    @Override
    public ProcessedOrder process(PendingOrder item) {
        ThirdPartyOrderResult result = thirdPartyOrderClient.createOrder(item);
        Instant processedAt = result.processedAt() != null ? result.processedAt() : Instant.now();
        return new ProcessedOrder(item.id(), result.thirdPartyOrderId(), result.status(), processedAt);
    }
}
