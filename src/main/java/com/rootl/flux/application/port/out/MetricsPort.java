package com.rootl.flux.application.port.out;

import java.time.Duration;

/**
 * Porta de saída para publicação e registro de métricas de negócio e operacionais.
 */
public interface MetricsPort {

    /**
     * Registra evento ingerido na plataforma.
     *
     * @param type tipo do evento
     * @param source origem geradora
     * @param status status da ingestão (ex: SUCESSO, ERRO)
     */
    void recordEventIngested(String type, String source, String status);

    /**
     * Registra evento rejeitado por chave de idempotência duplicada.
     *
     * @param type tipo do evento
     * @param source origem geradora
     */
    void recordDuplicateEvent(String type, String source);

    /**
     * Registra a conclusão da execução de um fluxo.
     *
     * @param flowId identificador do fluxo
     * @param status status final (SUCESSO, FALHA)
     * @param duration duração total da execução
     */
    void recordWorkflowExecution(String flowId, String status, Duration duration);

    /**
     * Registra a conclusão da execução de uma etapa individual.
     *
     * @param actionType tipo de ação da etapa
     * @param status status final da etapa
     * @param duration duração da etapa
     */
    void recordStepExecution(String actionType, String status, Duration duration);

    /**
     * Registra mensagem enviada ou recebida na Dead Letter Queue.
     *
     * @param reason motivo ou mensagem de erro
     */
    void recordDlqMessage(String reason);

    /**
     * Registra tentativa de reprocessamento a partir da DLQ.
     *
     * @param status status do reprocessamento
     */
    void recordDlqReprocessed(String status);
}
