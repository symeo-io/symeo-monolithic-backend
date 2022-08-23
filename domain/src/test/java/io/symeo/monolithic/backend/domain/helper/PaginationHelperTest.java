package io.symeo.monolithic.backend.domain.helper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.helper.pagination.Pagination;
import io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper;
import org.junit.jupiter.api.Test;

import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.validatePagination;
import static org.assertj.core.api.Assertions.assertThat;

public class PaginationHelperTest {

    @Test
    void should_throw_an_exception_for_invalid_maximum_page_size_request() {
        // Given
        final int pageSize = 1000000000;

        // When
        SymeoException symeoException = null;
        try {
            validatePagination(0, pageSize);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.PAGINATION_MAXIMUM_SIZE_EXCEEDED);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Page size %s over maximum size %s authorized",
                pageSize, 5000));
    }

    @Test
    void should_return_pagination() {
        // Given
        final int pageSize = 95;
        final int pageIndex = 111;

        // When
        final Pagination pagination = PaginationHelper.buildPagination(pageIndex, pageSize);

        // Then
        assertThat(pagination.getStart()).isEqualTo(pageIndex * pageSize);
        assertThat(pagination.getEnd()).isEqualTo(pageSize);
    }
}
