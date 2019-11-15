package ru.cbr.rpocr.web.service.token.introspector;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
public class TokenIntrospectController {

    private final TokenIntrospector tokenIntrospector;

    @PostMapping(value = "/introspect", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TokenIntrospector.TokenIntrospectionResponse> introspect(ServerWebExchange exchange) {
        return exchange.getFormData()
                .map(params -> params.getFirst("token"))
                .flatMap(tokenIntrospector::introspect)
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e));
    }
}
