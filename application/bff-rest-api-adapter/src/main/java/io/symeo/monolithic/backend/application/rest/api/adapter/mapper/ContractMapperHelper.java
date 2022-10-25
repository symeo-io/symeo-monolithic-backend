package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import java.math.BigDecimal;

import static java.util.Objects.isNull;

public interface ContractMapperHelper {
    static BigDecimal floatToBigDecimal(final Float floatToConvert) {
        return isNull(floatToConvert) ? null : new BigDecimal(Float.toString(floatToConvert));
    }
}
