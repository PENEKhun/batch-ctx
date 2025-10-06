package kr.huni.batctx.job.order.processor;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

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

        // HACK
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            throw new IllegalStateException("10퍼센트 확률로 실패함... 실패한 username : %s".formatted(item.username()));
        }

        return new ProcessedOrder(item.id(), result.thirdPartyOrderId(), result.status(), processedAt);
    }
}
