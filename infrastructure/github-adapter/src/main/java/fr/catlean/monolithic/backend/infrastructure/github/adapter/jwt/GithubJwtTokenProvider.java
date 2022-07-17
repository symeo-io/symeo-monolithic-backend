package fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

public interface GithubJwtTokenProvider {

    String generateSignedJwtToken() throws CatleanException;
}
