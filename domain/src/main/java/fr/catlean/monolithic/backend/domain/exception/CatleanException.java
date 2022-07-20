package fr.catlean.monolithic.backend.domain.exception;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CatleanException extends Exception {

    String code;
    String message;


    public static CatleanException getCatleanException(String message, String code) {
        return CatleanException.builder()
                .message(message)
                .code(code)
                .build();
    }
}
