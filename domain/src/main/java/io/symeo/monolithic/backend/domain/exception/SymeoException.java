package io.symeo.monolithic.backend.domain.exception;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SymeoException extends Exception {

    String code;
    String message;


    public static SymeoException getSymeoException(String message, String code) {
        return SymeoException.builder()
                .message(message)
                .code(code)
                .build();
    }
}
