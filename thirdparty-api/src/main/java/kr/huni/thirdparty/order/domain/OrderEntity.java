package kr.huni.thirdparty.order.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_orders_username_month", columnNames = {"username", "applied_year_month"})
})
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "applied_year_month", nullable = false, length = 7)
    private String appliedYearMonth;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderEntity() {
    }

    private OrderEntity(String username, BigDecimal totalPrice, String appliedYearMonth) {
        this.username = username;
        this.totalPrice = totalPrice;
        this.appliedYearMonth = appliedYearMonth;
    }

    public static OrderEntity of(String username, BigDecimal totalPrice, String appliedYearMonth) {
        return new OrderEntity(username, totalPrice, appliedYearMonth);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getAppliedYearMonth() {
        return appliedYearMonth;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
