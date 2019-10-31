package ru.cbr.rpocr.web.lib.common;

import org.springframework.web.bind.annotation.GetMapping;

public interface VersionController {

    @GetMapping("/")
    ServiceVersion hello();

    interface ServiceVersion {
        String getVersion();
    }

    class VersionControllerImpl implements VersionController {
        private final ServiceVersion serviceVersion;

        public VersionControllerImpl(ServiceVersion serviceVersion) {
            this.serviceVersion = serviceVersion;
        }

        public ServiceVersion hello() {
            return serviceVersion;
        }
    }
}
