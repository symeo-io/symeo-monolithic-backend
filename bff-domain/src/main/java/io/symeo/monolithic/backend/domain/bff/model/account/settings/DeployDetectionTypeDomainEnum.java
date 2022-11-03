package io.symeo.monolithic.backend.domain.bff.model.account.settings;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;

public enum DeployDetectionTypeDomainEnum {
    PULL_REQUEST("pull_request"),
    TAG("tag");

    public final String value;

    DeployDetectionTypeDomainEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeployDetectionTypeDomainEnum fromString(final String valueAsString) throws SymeoException {
        return switch (valueAsString) {
            case "pull_request" -> DeployDetectionTypeDomainEnum.PULL_REQUEST;
            case "tag" -> DeployDetectionTypeDomainEnum.TAG;
            default -> throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_EXCEPTION)
                    .message(String.format("Invalid deployment detection code %s in base", valueAsString))
                    .build();
        };
    }
}
