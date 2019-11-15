package ru.cbr.rpocr.web.service.crl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import ru.cbr.rpocr.web.lib.config.serviceinfo.CommonServiceConfig;

@AllArgsConstructor
@Slf4j
@Controller
class MessageController {

    private final SparkTaskExecutorService sparkTaskExecutorService;
    private final SparkLauncher sparkLauncher;
    private final ApplicationConfig.SparkConfig sparkConfig;

    @MessageMapping("/run")
    public Mono<Void> run(RSocketRequester requester) {
        log.debug(">>> run calculation: " +requester);
        return Mono.fromFuture(sparkTaskExecutorService.execute(getSparkTask()).getCompletableFuture());
    }

    private SparkTask getSparkTask() {
        SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec sparkSpec = new SparkTask.MainIndicatorsOfRubleExchangeRateTaskSpec(sparkLauncher);
        sparkSpec.sparkMaster(sparkConfig.getMaster())
                .priceIndexesUrl("/mnt/spark/volume/price_index_by_month.json")
                .currencyRateUrl("/mnt/spark/volume/currency_rate.json")
                .exportImportTurnoverUrl("/mnt/spark/volume/export_import.json");

        return sparkSpec.build();
    }

    @MessageMapping("/api/version")
    public Mono<CommonServiceConfig.ServiceApi> getApiVersion() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(this::getServiceApi);
    }

    private Mono<CommonServiceConfig.ServiceApi> getServiceApi(Authentication auth) {
        log.debug(">>> auth: " + auth.getName());
        return Mono.just(new CommonServiceConfig.ServiceApi("rsocket-calc-rate-launcher-app 1.0"));
    }
}
