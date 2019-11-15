package ru.cbr.rpocr.web.service.gateway;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
@AllArgsConstructor
class CalcRateLauncherRsocket {

   private final RSocketRequester requester;

    public CalcRateLauncherRsocket(RSocketRequester.Builder requsterBuilder) {
        Mono<RSocketRequester> requester = requsterBuilder
                .rsocketFactory(rsocket -> rsocket.keepAlive())
                .setupData("setup-data")
                .connectWebSocket(URI.create("ws://localhost:8089/rsocket"))
                .retryBackoff(100, Duration.ofSeconds(1))
                .doOnSubscribe(s -> log.debug(">>> rsocket connection established"))
                ;

        this.requester = requester.block();
    }

    public RSocketRequester.RequestSpec route(String route, Object... routeVars) {
        return requester.route(route, routeVars);
    }

    public RSocketRequester requester() {
       return requester;
   }

}
