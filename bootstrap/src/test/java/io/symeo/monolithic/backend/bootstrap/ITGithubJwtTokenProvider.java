package io.symeo.monolithic.backend.bootstrap;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;

public class ITGithubJwtTokenProvider implements GithubJwtTokenProvider {

    private String tokenSub;

    @Override
    public String generateSignedJwtToken() throws SymeoException {
        return tokenSub;
    }

    public void setGithubTokenStub(final String tokenStub) {
        this.tokenSub = tokenStub;
    }
}
