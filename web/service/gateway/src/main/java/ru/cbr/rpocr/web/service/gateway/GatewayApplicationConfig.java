package ru.cbr.rpocr.web.service.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class GatewayApplicationConfig {

    @Bean
    WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations,
                        ServerOAuth2AuthorizedClientRepository authorizedClients) {


        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);

        return WebClient.builder()
                .filter(oauth)
                .build();
    }

    @Bean
    CalcRateLauncherRsocket getSparkLauncherRequester(RSocketRequester.Builder requesterBuilder) {
        return new CalcRateLauncherRsocket(requesterBuilder);
    }

    @Bean
    OauthToken oauthToken(ServerOAuth2AuthorizedClientRepository authorizedClients
                          ,ReactiveClientRegistrationRepository clientRegistrations
    ) {
        log.debug(">>> auth: " + authorizedClients);
        return new OauthToken(authorizedClients, clientRegistrations, "ppod");
    }


    @AllArgsConstructor
    static class OauthToken {
        private final ServerOAuth2AuthorizedClientRepository authorizedClients;
        private final ReactiveClientRegistrationRepository clientRegistrations;
        private final String clientRegistrationId;

        public Mono<String> getToken(final ServerRequest serverRequest) {
            return ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication())
                    .map(auth -> Tuples.of(clientRegistrationId, auth, serverRequest.exchange()))
                    .flatMap(this::getClient)
                    .flatMap(this::extractToken);
        }

        private Mono<OAuth2AuthorizedClient> getClient(Tuple3<String, Authentication, ServerWebExchange> tuple) {
            return authorizedClients.loadAuthorizedClient(tuple.getT1(), tuple.getT2(), tuple.getT3());
        }

        private Mono<String> extractToken(OAuth2AuthorizedClient client) {
            return Mono.just(client.getAccessToken().getTokenValue());
        }
    }
}
