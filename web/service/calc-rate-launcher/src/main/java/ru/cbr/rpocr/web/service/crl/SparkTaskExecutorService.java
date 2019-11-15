package ru.cbr.rpocr.web.service.crl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@AllArgsConstructor
@Service
public class SparkTaskExecutorService {

    private final ExecutorService executorService;
    private final SparkLauncher sparkLauncher;

    public SparkTaskExecution<Void> execute(SparkTask sparkTask) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Future future = executorService.submit(() -> {
            sparkTask.run();
            completableFuture.complete(null);
        });

        return new SparkTaskExecution<Void>(future, completableFuture);
    }

    @AllArgsConstructor
    @Getter
    static class SparkTaskExecution<T> {

        private final Future<T> future;
        private final CompletableFuture<T> completableFuture;
    }
}
