package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.bff.contract.api.model.DeliverySettingsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.OrganizationSettingsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.OrganizationSettingsResponseContract;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.bff.contract.api.model.DeployDetectionSettingsContract.*;

public interface OrganizationSettingsContractMapper {

    static OrganizationSettingsResponseContract errorToContract(final SymeoException symeoException) {
        final OrganizationSettingsResponseContract organizationSettingsResponseContract =
                new OrganizationSettingsResponseContract();
        organizationSettingsResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return organizationSettingsResponseContract;
    }

    static OrganizationSettingsResponseContract domainToContract(final OrganizationSettings organizationSettings) throws SymeoException {
        final OrganizationSettingsResponseContract organizationSettingsResponseContract =
                new OrganizationSettingsResponseContract();
        final OrganizationSettingsContract organizationSettingsContract = new OrganizationSettingsContract();
        final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
        final DeployDetectionSettingsContract deployDetectionSettingsContract = new DeployDetectionSettingsContract();
        deployDetectionSettingsContract.setTagRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex());
        deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex());
        deployDetectionSettingsContract.setBranchRegexesToExclude(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes());
        deployDetectionSettingsContract.setDeployDetectionType(deployDetectionTypeDomainEnumToContractEnumMapper(
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType()
        ));
        deliverySettingsContract.setDeployDetection(deployDetectionSettingsContract);
        organizationSettingsContract.setDelivery(deliverySettingsContract);
        organizationSettingsContract.setId(organizationSettings.getId());
        organizationSettingsResponseContract.setSettings(organizationSettingsContract);
        return organizationSettingsResponseContract;
    }

    static OrganizationSettings contractToDomain(final OrganizationSettingsContract organizationSettingsContract,
                                                 UUID organizationId) throws SymeoException {
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
                                                .deployDetectionType(
                                                        deployDetectionTypeContractEnumToDomainEnumMapper(
                                                                organizationSettingsContract.getDelivery().getDeployDetection().getDeployDetectionType()
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }
    static DeployDetectionTypeDomainEnum deployDetectionTypeContractEnumToDomainEnumMapper(final DeployDetectionTypeEnum deployDetectionTypeContractEnum) throws SymeoException {
        return switch (deployDetectionTypeContractEnum) {
            case PULL_REQUEST -> DeployDetectionTypeDomainEnum.PULL_REQUEST;
            case TAG -> DeployDetectionTypeDomainEnum.TAG;
        };
    }
    static DeployDetectionTypeEnum deployDetectionTypeDomainEnumToContractEnumMapper(final DeployDetectionTypeDomainEnum deployDetectionTypeDomainEnum) {
        return switch (deployDetectionTypeDomainEnum) {
            case PULL_REQUEST -> DeployDetectionTypeEnum.PULL_REQUEST;
            case TAG -> DeployDetectionTypeEnum.TAG;
        };
    }


}
