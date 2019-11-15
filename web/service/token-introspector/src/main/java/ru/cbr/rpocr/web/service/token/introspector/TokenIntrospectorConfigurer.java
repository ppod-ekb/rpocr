package ru.cbr.rpocr.web.service.token.introspector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class TokenIntrospectorConfigurer {

    @Value("${spring.security.oauth2.client.provider.ppod.user-info-uri}")
    private String userInfoUri;

    @Bean
    public TokenIntrospector getGitHubTokenIntrospector() {
        Assert.notNull(userInfoUri, "userInfoUri can't be null, perhaps you forget to set 'user-info-uri' property");
        return new TokenIntrospector.GitHubTokenIntrospector(WebClient.create(userInfoUri));
    }
}
