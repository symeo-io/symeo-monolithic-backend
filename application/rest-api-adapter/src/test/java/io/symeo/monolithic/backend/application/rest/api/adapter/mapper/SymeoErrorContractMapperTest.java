package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class SymeoErrorContractMapperTest {

    @Test
    void should_go_through_error_mapping_and_raise_exception() {
        // Given
        final SymeoException functionalSymeoException =
                SymeoException.builder()
                        .code("F.FUNCTIONAL_EXCEPTION")
                        .message("Test message for functional Symeo exception")
                        .build();
        final SymeoException technicalSymeoException =
                SymeoException.builder()
                        .code("T.TECHNICAL_EXCEPTION")
                        .message("Test message for technical Symeo exception")
                        .build();
        final Supplier<String> supplierForException = () -> new String("Hello World");

        // When
        final ResponseEntity<String> functionalExceptionMapped =
                SymeoErrorContractMapper.mapSymeoExceptionToContract(supplierForException, functionalSymeoException);
        final ResponseEntity<String> technicalExceptionMapped =
                SymeoErrorContractMapper.mapSymeoExceptionToContract(supplierForException, technicalSymeoException);

        // Then
        assertThat(functionalExceptionMapped.getStatusCodeValue()).isEqualTo(400);
        assertThat(functionalExceptionMapped.getBody()).isEqualTo(supplierForException.get());
        assertThat(technicalExceptionMapped.getStatusCodeValue()).isEqualTo(500);
        assertThat(technicalExceptionMapped.getBody()).isEqualTo(supplierForException.get());

    }
}
