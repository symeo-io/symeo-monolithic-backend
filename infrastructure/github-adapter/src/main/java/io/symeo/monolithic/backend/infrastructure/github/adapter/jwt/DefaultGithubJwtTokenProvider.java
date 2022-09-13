package io.symeo.monolithic.backend.infrastructure.github.adapter.jwt;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.GITHUB_APP_JWT_GENERATION;

@AllArgsConstructor
@Slf4j
public class DefaultGithubJwtTokenProvider implements GithubJwtTokenProvider {

    private final String privateKeyCertificatePath;
    private final String githubAppId;

    @Override
    public String generateSignedJwtToken() throws SymeoException {
        try {
            final RSAPrivateKey rsaPrivateKey = getPrivateKey();
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

            final long now = Instant.now().getEpochSecond();
            JwtBuilder builder = Jwts.builder().claim("iss", githubAppId)
                    .claim("exp", now + 10 * 60)
                    .claim("iat", now - 60)
                    .signWith(rsaPrivateKey, signatureAlgorithm);
            return builder.compact();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            final String message = "Error while generating JWT token for Github App";
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(GITHUB_APP_JWT_GENERATION)
                    .message(message)
                    .build();
        }

    }

    private RSAPrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        File file = new File(privateKeyCertificatePath);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);

        byte[] keyBytes = new byte[(int) file.length()];
        dis.readFully(keyBytes);
        dis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }
}
