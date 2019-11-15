package ru.cbr.rpocr.web.service.token.introspector;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Configuration
    @ConfigurationProperties("spring.security.oauth2.client.registration.ppod")
    @ToString
    @Getter @Setter
    static class Oauth2ClientConfig {
        private String clientId;
        private String clientSecret;
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService(Oauth2ClientConfig config) {
        UserDetails userDetails = User.withUsername(config.getClientId())
                .password("{noop}"+config.getClientSecret())
                .roles("USER")
                .build();

        return new MapReactiveUserDetailsService(userDetails);
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
      http
          .authorizeExchange().pathMatchers("/").permitAll()
              .and()
              .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
              .csrf().disable()
              .httpBasic(withDefaults())
              //.oauth2Login(withDefaults())
              //.formLogin(withDefaults())
              //.oauth2Client(withDefaults())
            ;
      return http.build();
    }
}

