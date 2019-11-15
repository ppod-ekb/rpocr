package ru.cbr.rpocr.web.service.token.introspector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public interface TokenIntrospector {

    Mono<TokenIntrospectionResponse> introspect(String token);

    @Slf4j
    @AllArgsConstructor
    class GitHubTokenIntrospector implements TokenIntrospector {

        private final WebClient userInfoWebClient;

        public Mono<TokenIntrospectionResponse> introspect(String token) {

            log.debug("introspect token: " + token);

            return userInfoWebClient
                    .get()
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitHubTokenIntrospector.GitHubUser.class)
                    .map(GitHubTokenIntrospector.GitHubUserConverter::convertToTokenIntrospectionResponse);
        }

        static class GitHubUserConverter {

            public static TokenIntrospectionResponse convertToTokenIntrospectionResponse(GitHubTokenIntrospector.GitHubUser user) {
                TokenIntrospectionResponse r = new TokenIntrospectionResponse();
                r.setActive(true);
                r.setIss(null);
                r.setUsername(user.getId());
                r.setSub(user.getLogin());
                return r;
            }
        }

        static class GitHubUser {
            @Getter
            @Setter
            private String login;
            @Getter
            @Setter
            private String id;
        }
    }

    @Getter @Setter
    class TokenIntrospectionResponse {

        private boolean active;
        private String scope;
        private String client_id;
        private String username;
        private String token_type = "Bearer";
        private Long exp;
        private Long iat;
        private String iss;
        private String sub;
        private Long nbf;
        private String aud;
        private String jti;
    }
}
