package com.rootl.flux.infrastructure.adapter.in.rest;

import com.rootl.flux.application.port.out.MetricsPort;
import com.rootl.flux.infrastructure.adapter.in.messaging.kafka.DlqMessageStore;
import com.rootl.flux.infrastructure.adapter.in.messaging.kafka.DlqMessageStore.DlqItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dlq")
@Tag(name = "Dead Letter Queue (DLQ)", description = "Endpoints para monitoramento, inspeção e reprocessamento de mensagens que falharam no Kafka")
public class DlqController {

    private final DlqMessageStore dlqMessageStore;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String eventsRawTopic;
    private final MetricsPort metricsPort;

    public DlqController(
            DlqMessageStore dlqMessageStore,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${flux.kafka.topics.events-raw:root-l-flux.events.raw}") String eventsRawTopic,
            @Autowired(required = false) MetricsPort metricsPort) {
        this.dlqMessageStore = dlqMessageStore;
        this.kafkaTemplate = kafkaTemplate;
        this.eventsRawTopic = eventsRawTopic;
        this.metricsPort = metricsPort;
    }

    @GetMapping
    @Operation(summary = "Listar mensagens na Dead Letter Queue", description = "Retorna todas as mensagens roteadas para a DLQ após esgotamento de tentativas")
    @ApiResponse(responseCode = "200", description = "Lista de mensagens da DLQ recuperada com sucesso")
    public ResponseEntity<List<DlqItem>> listDlq() {
        return ResponseEntity.ok(dlqMessageStore.getAll());
    }

    @DeleteMapping
    @Operation(summary = "Limpar mensagens da DLQ em memória", description = "Remove as mensagens retidas no buffer de monitoramento da DLQ")
    @ApiResponse(responseCode = "204", description = "Buffer da DLQ limpo com sucesso")
    public ResponseEntity<Void> clearDlq() {
        dlqMessageStore.clear();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reprocessar")
    @Operation(summary = "Reenviar mensagem da DLQ para o tópico principal", description = "Reencaminha o payload da DLQ de volta para a fila original de eventos")
    @ApiResponse(responseCode = "200", description = "Mensagem reenviada com sucesso para reprocessamento")
    @ApiResponse(responseCode = "404", description = "Mensagem não localizada na DLQ")
    public ResponseEntity<String> replay(@PathVariable("id") String id) {
        return dlqMessageStore.getAll().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .map(item -> {
                    kafkaTemplate.send(eventsRawTopic, item.key(), item.payload());
                    if (metricsPort != null) {
                        metricsPort.recordDlqReprocessed("SUCESSO");
                    }
                    return ResponseEntity.ok("Mensagem com chave '" + item.key() + "' reenviada ao tópico '" + eventsRawTopic + "'");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
