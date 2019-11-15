package ru.cbr.rpocr.web.service.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.cbr.rpocr.web.lib.config.serviceinfo.CommonServiceConfig;

import static org.springframework.security.rsocket.metadata.BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE;

@Slf4j
@AllArgsConstructor
@RestController
class CalcRateLauncherController {
    private final CalcRateLauncherRsocket rsocket;

    @GetMapping("/spark-task-launcher/api/version")
    public Mono<CommonServiceConfig.ServiceApi> sparkTaskLauncherApi(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {

        String token = authorizedClient.getAccessToken().getTokenValue();
        BearerTokenMetadata credentials = new BearerTokenMetadata(token);

        return rsocket.route("/api/version")
                .data("test-data")
                .metadata(credentials.getToken(), BEARER_AUTHENTICATION_MIME_TYPE)
                .retrieveMono(CommonServiceConfig.ServiceApi.class)
                .onErrorResume(this::error);
    }

    private Mono<CommonServiceConfig.ServiceApi> error(Throwable e) {
        return Mono.just(new CommonServiceConfig.ServiceApi(e.toString()));
    }
}
