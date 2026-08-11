package io.beanguard.client.usage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UsageRegistryDefault implements UsageRegistry {
    private final ConcurrentMap<String, Long> usages = new ConcurrentHashMap<>();

    @Override
    public long getUsage(String limitName) {
        return usages.getOrDefault(limitName, 0L);
    }

    @Override
    public void incrementUsage(String limitName) {
        Long currentValue = usages.getOrDefault(limitName, 0L);
        usages.put(limitName, currentValue + 1L);
    }

    @Override
    public void decrementUsage(String limitName) {
        Long currentValue = usages.getOrDefault(limitName, 0L);
        usages.put(limitName, currentValue - 1L);
    }
}
