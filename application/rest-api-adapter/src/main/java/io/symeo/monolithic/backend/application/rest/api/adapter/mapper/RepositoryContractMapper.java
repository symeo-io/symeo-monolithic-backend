package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.frontend.contract.api.model.GetRepositoriesResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.RepositoryResponseContract;

import java.util.List;

public interface RepositoryContractMapper {

    static GetRepositoriesResponseContract domainToGetRepositoriesResponseContract(final List<Repository> repositories) {
        final GetRepositoriesResponseContract getRepositoriesResponseContract = new GetRepositoriesResponseContract();
        getRepositoriesResponseContract.setRepositories(repositories.stream().map(RepositoryContractMapper::repositoryToContract).toList());
        return getRepositoriesResponseContract;
    }

    private static RepositoryResponseContract repositoryToContract(final Repository repository) {
        final RepositoryResponseContract repositoryResponseContract = new RepositoryResponseContract();
        repositoryResponseContract.setId(repository.getId());
        repositoryResponseContract.setName(repository.getName());
        return repositoryResponseContract;
    }

    static GetRepositoriesResponseContract domainToKo(final SymeoException symeoException) {
        final GetRepositoriesResponseContract getRepositoriesResponseContract = new GetRepositoriesResponseContract();
        getRepositoriesResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return getRepositoriesResponseContract;
    }
}