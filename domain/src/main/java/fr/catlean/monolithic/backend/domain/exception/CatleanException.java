package fr.catlean.monolithic.backend.domain.exception;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CatleanException extends Exception {

    String code;
    String message;
}
