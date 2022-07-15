package fr.catlean.monolithic.backend.infrastructure.github.adapter.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.http.cient.CatleanHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;

public class GithubHttpClient {

    private final CatleanHttpClient catleanHttpClient;
    private final ObjectMapper objectMapper;
    private final String githubApiBaseUrl = "https://api.github.com/";
    private final String authorizationHeaderKey = "Authorization";
    private final String authorizationHeaderTokenValue = "token ";
    private final String token;

    public GithubHttpClient(CatleanHttpClient catleanHttpClient, ObjectMapper objectMapper, String token) {
        this.catleanHttpClient = catleanHttpClient;
        this.objectMapper = objectMapper;
        this.token = token;
    }

    public GithubRepositoryDTO[] getRepositoriesForOrganizationName(
            String organizationName, Integer page, Integer size) {
        final String uri =
                githubApiBaseUrl
                        + "orgs/"
                        + organizationName
                        + "/repos?sort=name&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return this.catleanHttpClient.get(
                uri,
                GithubRepositoryDTO[].class,
                objectMapper,
                Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token));
    }

    public <T> byte[] dtoToBytes(T t) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(t);
    }

    public <T> T bytesToDto(byte[] bytes, Class<T> tClass) throws IOException {
        return objectMapper.readValue(bytes, tClass);
    }

    public GithubPullRequestDTO[] getPullRequestsForRepositoryAndOrganization(String organizationName,
                                                                              String repositoryName, Integer page,
                                                                              Integer size) {
        final String uri =
                githubApiBaseUrl
                        + "repos/"
                        + organizationName
                        + "/" +
                        repositoryName
                        + "/pulls?sort=updated&direction=desc&state=all&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return this.catleanHttpClient.get(
                uri,
                GithubPullRequestDTO[].class,
                objectMapper,
                Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token));
    }

    public GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(final String organizationName,
                                                                          final String repositoryName,
                                                                          final Integer number) {
        final String uri =
                githubApiBaseUrl
                        + "repos/"
                        + organizationName
                        + "/" +
                        repositoryName
                        + "/pulls/"
                        + number;
        return this.catleanHttpClient.get(
                uri,
                GithubPullRequestDTO.class,
                objectMapper,
                Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token)
        );
    }


    private RSAPrivateKey getPrivateKey(String filename) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);

        byte[] keyBytes = new byte[(int) file.length()];
        dis.readFully(keyBytes);
        dis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }


    private String createJWTAndSign(String issuer, String subject, String server, String deviceid, String appversion,
                                           String os, String userType, String clientid, String pin, RSAPrivateKey privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {


        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

        JwtBuilder builder = Jwts.builder().claim("issuer", issuer)
                .claim("subject", subject)
                .claim("server", server)
                .claim("device_id", deviceid)
                .claim("app_version", appversion)
                .claim("os", os)
                .claim("user_type", userType)
                .claim("client_id", clientid)
                .claim("pin", pin)
                .signWith(signatureAlgorithm, privateKey);

        return builder.compact();

    }

}
