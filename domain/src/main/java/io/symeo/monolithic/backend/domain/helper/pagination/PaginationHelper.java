package io.symeo.monolithic.backend.domain.helper.pagination;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.extern.slf4j.Slf4j;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.PAGINATION_MAXIMUM_SIZE_EXCEEDED;

@Slf4j
public class PaginationHelper {

    private static final int MAXIMUM_PAGE_SIZE = 5000;

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

    public static int calculateTotalNumberOfPage(Integer pageSize, int count) {
        return (count / pageSize) + 1;
    }
}
