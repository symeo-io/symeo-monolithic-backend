package io.symeo.monolithic.backend.infrastructure.github.adapter.jwt;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface GithubJwtTokenProvider {

    String generateSignedJwtToken() throws SymeoException;
}
