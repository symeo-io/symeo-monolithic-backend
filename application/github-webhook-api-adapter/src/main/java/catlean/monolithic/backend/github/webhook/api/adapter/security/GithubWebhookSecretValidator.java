package catlean.monolithic.backend.github.webhook.api.adapter.security;

import org.apache.commons.codec.digest.HmacUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GithubWebhookSecretValidator {


    public static void validateWebhook(final byte[] githubWebhookBodyBytes, final String webhookSecret,
                                       final String github256Signature) {
        final String currentSha256Signature = "sha256=" + hmacWithJava(githubWebhookBodyBytes, webhookSecret);
        if (!currentSha256Signature.equals(github256Signature)) {
            throw new RuntimeException("Invalid sha256 signature");
        }

    }

    private static String hmacWithJava(final byte[] data, final String key) {
        return new HmacUtils("HmacSHA256", key).hmacHex(data);
    }
}
