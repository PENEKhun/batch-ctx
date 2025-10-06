package kr.huni.thirdparty.order.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequest(
        @NotBlank(message = "username은 필수입니다")
        String username,
        @DecimalMin(value = "0.0", inclusive = false, message = "totalPrice는 0보다 커야 합니다")
        BigDecimal totalPrice,
        @NotBlank(message = "appliedYearMonth는 필수입니다")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "appliedYearMonth는 yyyy-MM 형식이어야 합니다")
        String appliedYearMonth
) {
}
