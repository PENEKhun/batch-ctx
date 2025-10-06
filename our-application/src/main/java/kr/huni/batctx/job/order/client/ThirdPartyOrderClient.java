package kr.huni.batctx.job.order.client;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import kr.huni.batctx.job.order.domain.OrderProcessingStatus;
import kr.huni.batctx.job.order.domain.PendingOrder;

@Component
public class ThirdPartyOrderClient {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyOrderClient.class);

    private final RestClient restClient;

    public ThirdPartyOrderClient(RestClient.Builder builder,
                                 @Value("${thirdparty.order-api.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ThirdPartyOrderResult createOrder(PendingOrder pendingOrder) {
        try {
            ThirdPartyCreateOrderRequest request = new ThirdPartyCreateOrderRequest(
                    pendingOrder.username(),
                    pendingOrder.totalPrice(),
                    pendingOrder.appliedYearMonth()
            );

            ThirdPartyCreateOrderResponse response = restClient.post()
                    .uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ThirdPartyCreateOrderResponse.class);

            if (response == null) {
                throw new IllegalStateException("Received empty response while creating third-party order");
            }

            return ThirdPartyOrderResult.created(response.orderId(), response.createdAt());
        } catch (HttpClientErrorException ex) {
            throw new IllegalStateException("외부 API 오류!! 무슨 에러인진 모름!!!");
        } catch (RestClientException ex) {
            log.error("Unexpected error while calling third-party order API", ex);
            throw new IllegalStateException("Unexpected error while calling third-party order API", ex);
        }
    }

    private record ThirdPartyCreateOrderRequest(String username,
                                                java.math.BigDecimal totalPrice,
                                                String appliedYearMonth) {
    }

    private record ThirdPartyCreateOrderResponse(Long orderId,
                                                 String username,
                                                 java.math.BigDecimal totalPrice,
                                                 String appliedYearMonth,
                                                 Instant createdAt) {
    }
}
