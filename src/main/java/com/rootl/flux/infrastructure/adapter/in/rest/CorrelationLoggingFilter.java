package com.rootl.flux.infrastructure.adapter.in.rest;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro HTTP para auditoria, rastreamento distribuído e correlação de logs via MDC (Mapped Diagnostic Context).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";

    private final Tracer tracer;

    public CorrelationLoggingFilter(@Autowired(required = false) Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String traceId = null;
        String spanId = null;

        if (tracer != null && tracer.currentSpan() != null) {
            traceId = tracer.currentSpan().context().traceId();
            spanId = tracer.currentSpan().context().spanId();
        }

        if (traceId == null || traceId.isBlank()) {
            traceId = correlationId.replace("-", "");
            if (traceId.length() > 32) {
                traceId = traceId.substring(0, 32);
            }
        }
        if (spanId == null || spanId.isBlank()) {
            spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_SPAN_ID, spanId);

        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String uri = request.getRequestURI();

            // Evita poluição de logs para endpoints estáticos
            if (!uri.startsWith("/ui") && !uri.endsWith(".html") && !uri.endsWith(".css") && !uri.endsWith(".js") && !uri.endsWith(".ico")) {
                log.info("[Auditoria HTTP] {} {} - status: {} - duração: {}ms",
                        request.getMethod(), uri, response.getStatus(), duration);
            }

            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_SPAN_ID);
        }
    }
}
