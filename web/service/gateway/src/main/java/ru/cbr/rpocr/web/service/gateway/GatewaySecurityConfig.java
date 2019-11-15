package ru.cbr.rpocr.web.service.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) throws Exception {
      /*  return http.authorizeExchange()
                //.pathMatchers("/about").permitAll()
                .anyExchange().authenticated()
                .and().oauth2Login()
                .and().build();*/
      http.authorizeExchange()
              .pathMatchers("/").permitAll()
              .and()
              .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
              .oauth2Login(withDefaults())
              .csrf().disable()
              //.formLogin(withDefaults())
              //.oauth2Client(withDefaults())
                ;
      return http.build();

        /*
        http
			.authorizeExchange(exchanges ->
				exchanges
					.pathMatchers("/", "/public/**").permitAll()
					.anyExchange().authenticated()
			)
			.oauth2Login(withDefaults())
			//.formLogin(withDefaults())
			//.oauth2Client(withDefaults());
		return http.build();
         */
    }
}

