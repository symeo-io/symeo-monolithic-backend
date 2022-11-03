package io.symeo.monolithic.backend.domain.bff.model.account.settings;

public enum DeployDetectionTypeDomainEnum {
    PULL_REQUEST("pull_request"),
    TAG("tag");

    public final String value;

    DeployDetectionTypeDomainEnum(String value) {
        this.value = value;
    }

    public static DeployDetectionTypeDomainEnum fromValue(String value) {
        return switch (value) {
            case "pull_request" -> DeployDetectionTypeDomainEnum.PULL_REQUEST;
            case "tag" -> DeployDetectionTypeDomainEnum.TAG;
            default -> null;
        };
    }
}
