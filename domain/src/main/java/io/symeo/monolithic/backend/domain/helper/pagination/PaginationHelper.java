package io.symeo.monolithic.backend.domain.helper.pagination;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.PAGINATION_MAXIMUM_SIZE_EXCEEDED;

@Slf4j
public class PaginationHelper {

    private static final int MAXIMUM_PAGE_SIZE = 5000;
    private static final List<String> SORTING_DIRECTIONS = List.of("asc", "desc");

    public static void validatePagination(final Integer pageIndex, final Integer pageSize) throws SymeoException {
        if (pageSize > MAXIMUM_PAGE_SIZE) {
            final String message = String.format("Page size %s over maximum size %s authorized",
                    pageSize, MAXIMUM_PAGE_SIZE);
            LOGGER.warn(message);
            throw SymeoException.builder()
                    .message(message)
                    .code(PAGINATION_MAXIMUM_SIZE_EXCEEDED)
                    .build();
        }
    }

    public static Pagination buildPagination(final int pageIndex, final int pageSize) {
        return Pagination.builder()
                .start(pageIndex * pageSize)
                .end(pageSize)
                .build();
    }

    public static int calculateTotalNumberOfPage(final Integer pageSize, final int count) {
        return (count / pageSize) + 1;
    }

    public static void validateSortingInputs(final String sortingDirection,
                                             final String sortingParameter,
                                             final List<String> availableSortingParameters)
            throws SymeoException {
        validateSortingInput(availableSortingParameters, sortingParameter, "Invalid sorting parameter %s not in " +
                "available parameters %s", SymeoExceptionCode.INVALID_SORTING_PARAMETER);
        validateSortingInput(SORTING_DIRECTIONS, sortingDirection, "Invalid sorting direction %s not in available" +
                " directions %s", SymeoExceptionCode.INVALID_SORTING_DIRECTION);
    }

    private static void validateSortingInput(List<String> availableInputs, String input,
                                             String format, String invalidSortingDirection) throws SymeoException {
        if (!availableInputs.contains(input)) {
            final String message = String.format(format,
                    input, availableInputs);
            LOGGER.warn(message);
            throw SymeoException.builder()
                    .code(invalidSortingDirection)
                    .message(message)
                    .build();
        }
    }
}
