package dev.beanguard.server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.services.ParameterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ParameterService parameterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String ip = resolveIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket(parseRpm()));
        if (bucket.tryConsume(1)) {
            return true;
        }
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Too many requests\"}");
        return false;
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private int parseRpm() {
        try {
            return Integer.parseInt(parameterService.getString(ParameterName.RATE_LIMIT_OPEN_RPM));
        } catch (NumberFormatException e) {
            return Integer.parseInt(ParameterName.RATE_LIMIT_OPEN_RPM.getDefaultValue());
        }
    }

    public void resetBuckets() {
        buckets.clear();
    }

    private Bucket newBucket(int rpm) {
        Bandwidth limit = Bandwidth.classic(rpm, Refill.greedy(rpm, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
