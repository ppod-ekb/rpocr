package ru.cbr.rpocr.web.service.token.introspector;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.function.Consumer;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TokenIntrospectorServiceIntegrationTest {

    @Value("${expected.introspect-token.json.response}")
    private String expectedIntrospectTokenJsonResponse;

    @Autowired
    WebTestClient rest;

    @Autowired
    TestConfiguration.SecurityOauth2Config config;

    @Test
    public void basicWhenInvalidCredentialsThenUnauthorized() {
        this.rest
                .post()
                .uri("/introspect")
                .headers(invalidCredentials())
                .body(BodyInserters.fromFormData("token", config.getToken()))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().isEmpty();
    }

    @Test
    public void basicWhenCredentialsThenOk() {
        this.rest
                .post()
                .uri("/introspect")
                .headers(userCredentials())
                .body(BodyInserters.fromFormData("token", config.getToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(expectedIntrospectTokenJsonResponse)
                ;
    }


    private Consumer<HttpHeaders> invalidCredentials() {
        return httpHeaders -> httpHeaders.setBasicAuth("user", "invalidpassowrdstring");
    }

    private Consumer<HttpHeaders> userCredentials() {
        return httpHeaders -> httpHeaders.setBasicAuth("630edf9c9ebf675e980c", "c43d9bf9520a94529f283eb1ea90c3ae87208fed");
    }

}
