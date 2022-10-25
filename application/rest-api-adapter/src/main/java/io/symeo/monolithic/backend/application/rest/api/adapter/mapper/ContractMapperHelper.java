package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import java.math.BigDecimal;

import static java.util.Objects.isNull;

public interface ContractMapperHelper {
    static BigDecimal floatToBigDecimal(final Float floatToConvert) {
        return isNull(floatToConvert) ? null : new BigDecimal(Float.toString(floatToConvert));
    }

    static BigDecimal longToBigDecimal(final Long longToConvert) {
        return isNull(longToConvert) ? null : new BigDecimal(Long.toString(longToConvert));
    }
}
