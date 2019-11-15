package ru.cbr.rpocr.web.lib.config.serviceinfo;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ServiceInfoRouteConfig {

    @Bean
    ServiceInfoHandler getServiceInfoHandler(CommonServiceConfig config) {
        return new ServiceInfoHandler(config);
    }

    @Bean
    RouterFunction<ServerResponse> getServiceInfoRouter(ServiceInfoHandler handler) {
        return route(GET("/")
                .and(accept(MediaType.APPLICATION_JSON)), handler::serviceConfiguration);
    }

    @AllArgsConstructor
    static class ServiceInfoHandler {

        private final CommonServiceConfig config;

        public Mono<ServerResponse> serviceConfiguration(ServerRequest request) {
            return ServerResponse.ok().body(Mono.just(config), CommonServiceConfig.class);
        }
    }
}
