package io.symeo.monolithic.backend.domain.model.account.settings;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class DeliverySettings {
    @NonNull
    DeployDetectionSettings deployDetectionSettings;
}
