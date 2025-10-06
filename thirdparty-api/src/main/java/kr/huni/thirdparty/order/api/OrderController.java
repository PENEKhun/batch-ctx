package kr.huni.thirdparty.order.api;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.huni.thirdparty.order.domain.OrderEntity;
import kr.huni.thirdparty.order.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
        OrderEntity order = orderService.createOrder(request.username(), request.totalPrice(), request.appliedYearMonth());
        log.info("createOrder : {}", request.username());
        return new CreateOrderResponse(
                order.getId(),
                order.getUsername(),
                order.getTotalPrice(),
                order.getAppliedYearMonth(),
                order.getCreatedAt()
        );
    }
}
