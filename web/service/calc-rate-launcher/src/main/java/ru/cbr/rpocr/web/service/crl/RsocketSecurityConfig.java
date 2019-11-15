package ru.cbr.rpocr.web.service.crl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.PayloadInterceptorOrder;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.rsocket.authentication.AuthenticationPayloadInterceptor;
import org.springframework.security.rsocket.authentication.BearerPayloadExchangeConverter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

@Slf4j
@Configuration
@EnableRSocketSecurity
public class RsocketSecurityConfig {

   @ToString  @Getter @Setter
    static class OpaqueTokenIntrospectionConfig {
        private String introspectionUri;
        private String introspectionClientId;
        private String introspectionClientSecret;
    }

    @Bean
    @ConfigurationProperties("rsocket.security.oauth2.introspection.opaque")
    public OpaqueTokenIntrospectionConfig getOpaqueTokenIntrospectionConfig() {
        return new OpaqueTokenIntrospectionConfig();
    }

    @Bean
    public ReactiveAuthenticationManager getReactiveAutheticationManager(
            ReactiveOpaqueTokenIntrospector introspector) {

        return new OpaqueTokenReactiveAuthenticationManager(introspector);

    }

    @Bean
    public ReactiveOpaqueTokenIntrospector getReactiveOpaqueTokenIntrospector(OpaqueTokenIntrospectionConfig config) {
        return new NimbusReactiveOpaqueTokenIntrospector(
                config.getIntrospectionUri(),
                config.getIntrospectionClientId(),
                config.getIntrospectionClientSecret());
    }

    @Bean
    public AuthenticationPayloadInterceptor getAuthenticationPayloadInterceptor(ReactiveAuthenticationManager manager) {
        AuthenticationPayloadInterceptor result = new AuthenticationPayloadInterceptor(manager);
        result.setAuthenticationConverter(new BearerPayloadExchangeConverter());
        result.setOrder(PayloadInterceptorOrder.AUTHENTICATION.getOrder());

        return result;
    }

    @Bean
    PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket,
                                                        AuthenticationPayloadInterceptor authenticationPayloadInterceptor) {
        rsocket
                .addPayloadInterceptor(authenticationPayloadInterceptor)
                .authorizePayload(authorize ->
                        authorize
                                .route("/calc-launcher/*").authenticated()
                                .route("/").permitAll()
                                .anyExchange().permitAll()
                                .anyRequest().authenticated()
                )
                ;
        return rsocket.build();
    }
}

