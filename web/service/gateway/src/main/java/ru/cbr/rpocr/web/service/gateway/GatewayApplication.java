package ru.cbr.rpocr.web.service.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.cbr.rpocr.web.lib.config.serviceinfo.EnableRpocrServiceInfo;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;


@EnableRpocrServiceInfo
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .main(GatewayApplication.class)
                .sources(GatewayApplication.class)
                .profiles("prod")
                .run(args);
    }

    @Slf4j
    @AllArgsConstructor
    @RestController
    static class TokenController {
        @GetMapping("/tokenInfo")
        public Mono<TokenInfo> servicesApiList(
                @AuthenticationPrincipal OAuth2User oauth2User,
                @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) {

            return Mono.just(new TokenInfo(oauth2User.getName(),
                                authorizedClient.getAccessToken().getTokenValue(),
                                authorizedClient.getAccessToken().getTokenType().getValue()));
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter @Setter
        static class TokenInfo {
            private String user;
            private String token;
            private String tokenType;
        }
    }


    @Slf4j
    @AllArgsConstructor
    @RestController
    static class ServiceApiController {

        @GetMapping("/me")
        public Mono<Map<String, String>> hello(Mono<Principal> principal) {
            return principal
                    .map(Principal::getName)
                    .map(username -> Collections.singletonMap("remoteUser", username));
        }


    }
}
