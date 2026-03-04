package de.dkb.api.notificationhub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements jakarta.servlet.Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Logger accessLogger =
            LoggerFactory.getLogger("ACCESS_LOGGER");

    public static final String TRACE_ID = "traceId";
    public static final String METHOD = "method";
    public static final String PATH = "path";
    public static final String CLIENT_IP = "clientIp";

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        String traceId = httpRequest.getHeader("X-Trace-Id");

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(TRACE_ID, traceId);
        MDC.put(METHOD, httpRequest.getMethod());
        MDC.put(PATH, httpRequest.getRequestURI());
        MDC.put(CLIENT_IP, httpRequest.getRemoteAddr());

        httpResponse.setHeader("X-Trace-Id", traceId);

        try {
            log.info("Request received");
            chain.doFilter(request, response);
        } finally {

            long duration = System.currentTimeMillis() - startTime;

            accessLogger.info("status={} durationMs={}",
                    httpResponse.getStatus(),
                    duration);

            log.info("Request completed with status={} durationMs={}",
                    httpResponse.getStatus(),
                    duration);

            MDC.clear();
        }
    }
}