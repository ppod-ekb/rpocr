package ru.cbr.rpocr.web.service.token.introspector;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceInfoIntegrationTest {

    @Value("${expected.service-info.json.response}")
    private String expectedServiceInfoJsonResponse;

    @Autowired
    WebTestClient rest;

    @Test
    public void withoutCredentialsThenServiceInfoIsUnauthorized() {
        this.rest
                .get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(expectedServiceInfoJsonResponse);
    }
}
