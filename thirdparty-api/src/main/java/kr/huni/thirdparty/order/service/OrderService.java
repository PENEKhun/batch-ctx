package kr.huni.thirdparty.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.huni.thirdparty.order.domain.OrderEntity;
import kr.huni.thirdparty.order.domain.OrderRepository;
import kr.huni.thirdparty.order.support.OrderAlreadyExistsException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderEntity createOrder(String username, BigDecimal totalPrice, String appliedYearMonth) {
        if (orderRepository.existsByUsernameAndAppliedYearMonth(username, appliedYearMonth)) {
            throw new OrderAlreadyExistsException(username, appliedYearMonth);
        }

        OrderEntity order = OrderEntity.of(username, totalPrice, appliedYearMonth);
        return orderRepository.save(order);
    }
}
