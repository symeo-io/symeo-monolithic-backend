package io.symeo.monolithic.backend.domain.exception;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SymeoExceptionTest {

    private static final Faker faker = new Faker();

    @Test
    void should_return_stack_trace_in_to_string_given_a_root_exception() {
        // Given
        final String code = faker.rickAndMorty().character();
        final String message = faker.rickAndMorty().location();
        final SymeoException symeoException = SymeoException.builder()
                .rootException(new IOException())
                .code(code)
                .message(message)
                .build();

        // When
        final String symeoExceptionToString = symeoException.toString();

        // Then
        assertThat(symeoExceptionToString)
                .contains(String.format("code='%s'", code))
                .contains(String.format("message='%s'", message))
                .contains("rootException=java.io.IOException\n" +
                        "\tat io.symeo.monolithic.backend.domain.exception.SymeoExceptionTest" +
                        ".should_return_stack_trace_in_to_string_given_a_root_exception(SymeoExceptionTest.java:19)");
    }
}
