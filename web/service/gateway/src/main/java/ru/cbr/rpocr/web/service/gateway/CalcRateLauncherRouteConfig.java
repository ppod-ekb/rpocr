package ru.cbr.rpocr.web.service.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CalcRateLauncherRouteConfig {

    @Bean
    RouterFunction<ServerResponse> getSparkLauncherRouter(CalcRateLauncherHandler handler) {
        return route(POST("/calc-rate/run")
                .and(accept(MediaType.APPLICATION_JSON)), handler::runCalculationOnSparkCluster);
    }


}
