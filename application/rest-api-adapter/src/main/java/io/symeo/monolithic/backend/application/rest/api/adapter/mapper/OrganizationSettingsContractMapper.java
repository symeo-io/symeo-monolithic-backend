package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeliverySettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.OrganizationSettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.OrganizationSettingsResponseContract;

import java.util.List;
import java.util.UUID;

public interface OrganizationSettingsContractMapper {

    static OrganizationSettingsResponseContract errorToContract(final SymeoException symeoException) {
        final OrganizationSettingsResponseContract organizationSettingsResponseContract =
                new OrganizationSettingsResponseContract();
        organizationSettingsResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return organizationSettingsResponseContract;
    }

    static OrganizationSettingsResponseContract domainToContract(final OrganizationSettings organizationSettings) {
        final OrganizationSettingsResponseContract organizationSettingsResponseContract =
                new OrganizationSettingsResponseContract();
        final OrganizationSettingsContract organizationSettingsContract = new OrganizationSettingsContract();
        final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
        final DeployDetectionSettingsContract deployDetectionSettingsContract = new DeployDetectionSettingsContract();
        deployDetectionSettingsContract.setTagRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex());
        deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex());
        deployDetectionSettingsContract.setBranchRegexesToExclude(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes());
        deliverySettingsContract.setDeployDetection(deployDetectionSettingsContract);
        organizationSettingsContract.setDelivery(deliverySettingsContract);
        organizationSettingsContract.setId(organizationSettings.getId());
        organizationSettingsResponseContract.setSettings(organizationSettingsContract);
        return organizationSettingsResponseContract;
    }

    static OrganizationSettings contractToDomain(final OrganizationSettingsContract organizationSettingsContract,
                                                 UUID organizationId) {
        return OrganizationSettings.builder()
                .id(organizationSettingsContract.getId())
                .organizationId(organizationId)
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .tagRegex(organizationSettingsContract.getDelivery().getDeployDetection().getTagRegex())
                                                .pullRequestMergedOnBranchRegex(organizationSettingsContract.getDelivery().getDeployDetection().getPullRequestMergedOnBranchRegex())
                                                .excludeBranchRegexes(organizationSettingsContract.getDelivery().getDeployDetection().getBranchRegexesToExclude())
                                                .build()
                                )
                                .build()
                )
                .build();
    }


}
