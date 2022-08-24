package io.symeo.monolithic.backend.domain.helper;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.helper.pagination.Pagination;
import io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.validatePagination;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.validateSortingInputs;
import static org.assertj.core.api.Assertions.assertThat;

public class PaginationHelperTest {

    private static final Faker faker = new Faker();

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

    @Test
    void should_validate_sorting_inputs() {
        // Given
        final String sortingParameter = faker.ancient().god();
        final List<String> availableSortingParameters = List.of(sortingParameter, faker.harryPotter().house());

        // When
        SymeoException symeoException = null;
        try {
            validateSortingInputs("asc", sortingParameter, availableSortingParameters);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNull();
    }

    @Test
    void should_invalidate_sorting_parameter() {
        // Given
        final String sortingParameter = faker.ancient().god();
        final List<String> availableSortingParameters = List.of(faker.rickAndMorty().character(),
                faker.harryPotter().house());

        // When
        SymeoException symeoException = null;
        try {
            validateSortingInputs("asc", sortingParameter, availableSortingParameters);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.INVALID_SORTING_PARAMETER);
        assertThat(symeoException.getMessage())
                .isEqualTo(String.format(
                        "Invalid sorting parameter %s not in available parameters %s", sortingParameter,
                        availableSortingParameters));
    }

    @Test
    void should_invalidate_direction_parameter() {
        // Given
        final String sortingParameter = faker.ancient().god();
        final List<String> availableSortingParameters = List.of(sortingParameter, faker.rickAndMorty().character(),
                faker.harryPotter().house());
        final String fakerSortingDirection = faker.ancient().hero();

        // When
        SymeoException symeoException = null;
        try {
            validateSortingInputs(fakerSortingDirection, sortingParameter, availableSortingParameters);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.INVALID_SORTING_DIRECTION);
        assertThat(symeoException.getMessage())
                .isEqualTo(String.format("Invalid sorting direction %s not in available" +
                        " directions %s", fakerSortingDirection, List.of("asc", "desc")));
    }


}
