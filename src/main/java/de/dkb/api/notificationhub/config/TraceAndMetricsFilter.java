package de.dkb.api.notificationhub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class TraceAndMetricsFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(TraceAndMetricsFilter.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String RESPONSE_TIME_HEADER = "X-Response-Time-ms";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.nanoTime();

        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
                .filter(id -> !id.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put(TRACE_ID_KEY, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            response.setHeader(TRACE_ID_HEADER, traceId);
            response.setHeader(RESPONSE_TIME_HEADER, String.valueOf(durationMs));

            log.info(
                    "event=RequestCompleted method={} path={} status={} durationMs={} traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    traceId
            );

            MDC.clear();
        }
    }
}