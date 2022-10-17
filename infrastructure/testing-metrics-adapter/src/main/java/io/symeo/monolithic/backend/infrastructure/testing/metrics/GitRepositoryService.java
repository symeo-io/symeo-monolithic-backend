package io.symeo.monolithic.backend.infrastructure.testing.metrics;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static io.symeo.monolithic.backend.domain.exception.SymeoException.getSymeoException;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.GIT_FAILED_TO_OPEN_REPOSITORY;

public class GitRepositoryService {
    public void checkoutRepositoryToCommit(String repositoryFilesPath, String commitSha) throws SymeoException {
        try {
            File gitDirectory = new File(repositoryFilesPath + "/.git");
            Git.open(gitDirectory).checkout().setName(commitSha).call();
        } catch (IOException | GitAPIException e) {
            throw getSymeoException("Failed to open git repository folder at " + repositoryFilesPath + " with commit " + commitSha, GIT_FAILED_TO_OPEN_REPOSITORY);
        }
    }
}
