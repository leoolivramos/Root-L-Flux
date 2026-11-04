package com.rootl.flux.domain.exception;

public class DuplicateEventException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateEventException(String idempotencyKey) {
        super("O evento com a chave de idempotência '" + idempotencyKey + "' já foi recebido e processado anteriormente");
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
