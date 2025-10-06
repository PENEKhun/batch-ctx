package kr.huni.thirdparty.order.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrderAlreadyExistsException extends RuntimeException {

    public OrderAlreadyExistsException(String username, String appliedYearMonth) {
        super("이미 신청된 사용자입니다: username=" + username + ", appliedYearMonth=" + appliedYearMonth);
    }
}
