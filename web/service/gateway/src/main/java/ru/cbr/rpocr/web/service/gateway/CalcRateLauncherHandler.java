package ru.cbr.rpocr.web.service.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.security.rsocket.metadata.BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE;

@Slf4j
@AllArgsConstructor
@Component
public class CalcRateLauncherHandler {

    private final RSocketRequester.Builder requsterBuilder;
    private final GatewayApplicationConfig.OauthToken oauthToken;
    private final CalcRateLauncherRsocket rsocket;

    public Mono<ServerResponse> runCalculationOnSparkCluster(ServerRequest request) {

        log.debug(">>> handle /calc-rate/run");

        return ServerResponse.ok().body(
                oauthToken.getToken(request)
                            .map(BearerTokenMetadata::new)
                            .flatMap(this::run), String.class);
    }

    private Mono<String> run(BearerTokenMetadata credentials) {
        return rsocket.route("/run")
                .data("test-data")
                .metadata(credentials.getToken(), BEARER_AUTHENTICATION_MIME_TYPE)
                .retrieveMono(Void.class)
                //.send() // fire and forget
                .thenReturn("OK")
                .doOnSubscribe(c -> log.debug(">>> calc rate started"))
        ;
    }
}
