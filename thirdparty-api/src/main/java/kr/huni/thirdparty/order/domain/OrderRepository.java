package kr.huni.thirdparty.order.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByUsernameAndAppliedYearMonth(String username, String appliedYearMonth);

    Optional<OrderEntity> findByUsernameAndAppliedYearMonth(String username, String appliedYearMonth);
}
