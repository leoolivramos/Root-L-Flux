package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.util.Objects;

public record RetryPolicy(
        int maxAttempts,
        long initialDelayMs,
        double multiplier,
        long maxDelayMs
) implements Serializable {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("O número máximo de tentativas deve ser no mínimo 1");
        }
        if (initialDelayMs < 0) {
            throw new IllegalArgumentException("O tempo de espera inicial (initialDelayMs) não pode ser negativo");
        }
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("O multiplicador de backoff deve ser no mínimo 1.0");
        }
        if (maxDelayMs < initialDelayMs) {
            throw new IllegalArgumentException("O tempo de espera máximo (maxDelayMs) não pode ser menor que o inicial");
        }
    }

    public static RetryPolicy padrao() {
        return new RetryPolicy(3, 1000L, 2.0, 10000L);
    }

    public static RetryPolicy semRetries() {
        return new RetryPolicy(1, 0L, 1.0, 0L);
    }
}
