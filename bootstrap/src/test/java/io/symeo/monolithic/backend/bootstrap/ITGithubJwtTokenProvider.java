package io.symeo.monolithic.backend.bootstrap;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;

public class ITGithubJwtTokenProvider implements GithubJwtTokenProvider {

    private String tokenStub;

    @Override
    public String generateSignedJwtToken() throws SymeoException {
        return tokenStub;
    }

    public void setGithubTokenStub(final String tokenStub) {
        this.tokenStub = tokenStub;
    }
}
