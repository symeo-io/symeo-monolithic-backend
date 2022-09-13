package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeliverySettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.OrganizationSettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.OrganizationSettingsResponseContract;

import java.util.List;

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
        deliverySettingsContract.setDeployDetection(deployDetectionSettingsContract);
        organizationSettingsContract.setDelivery(deliverySettingsContract);
        organizationSettingsResponseContract.setSettings(organizationSettingsContract);
        return organizationSettingsResponseContract;
    }


}
