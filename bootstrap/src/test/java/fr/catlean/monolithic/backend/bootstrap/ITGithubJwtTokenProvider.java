package fr.catlean.monolithic.backend.bootstrap;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;

public class ITGithubJwtTokenProvider implements GithubJwtTokenProvider {

    private String tokenSub;

    @Override
    public String generateSignedJwtToken() throws CatleanException {
        return tokenSub;
    }

    public void setGithubTokenStub(final String tokenStub) {
        this.tokenSub = tokenStub;
    }
}
