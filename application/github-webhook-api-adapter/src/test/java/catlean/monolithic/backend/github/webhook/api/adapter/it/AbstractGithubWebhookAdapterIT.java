package catlean.monolithic.backend.github.webhook.api.adapter.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ActiveProfiles({"it"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = GithubWebhookAdapterITApplication.class)
@EnableConfigurationProperties
public class AbstractGithubWebhookAdapterIT {

    @Autowired
    protected WebTestClient client;
    @Autowired
    protected ObjectMapper objectMapper;
}
