package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetRepositoriesResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.RepositoryResponseContract;

import java.util.List;

public interface RepositoryResponseMapper {

    static GetRepositoriesResponseContract domainToGetRepositoriesResponseContract(final List<Repository> repositories) {
        final GetRepositoriesResponseContract getRepositoriesResponseContract = new GetRepositoriesResponseContract();
        getRepositoriesResponseContract.setRepositories(repositories.stream().map(RepositoryResponseMapper::repositoryToContract).toList());
        return getRepositoriesResponseContract;
    }

    private static RepositoryResponseContract repositoryToContract(final Repository repository) {
        final RepositoryResponseContract repositoryResponseContract = new RepositoryResponseContract();
        repositoryResponseContract.setId(repository.getId());
        repositoryResponseContract.setName(repository.getName());
        return repositoryResponseContract;
    }

    static GetRepositoriesResponseContract domainToKo(final CatleanException catleanException) {
        final GetRepositoriesResponseContract getRepositoriesResponseContract = new GetRepositoriesResponseContract();
        getRepositoriesResponseContract.setErrors(List.of(CatleanErrorResponseMapper.catleanExceptionToContract(catleanException)));
        return getRepositoriesResponseContract;
    }
}
