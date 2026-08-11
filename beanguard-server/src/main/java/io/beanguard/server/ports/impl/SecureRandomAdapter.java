package io.beanguard.server.ports.impl;

import io.beanguard.server.ports.SecureStringProvider;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SecureRandomAdapter implements SecureStringProvider {
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(int length) {
        return IntStream.range(0, length)
                .map(i -> secureRandom.nextInt(ALPHANUMERIC_CHARS.length()))
                .mapToObj(ALPHANUMERIC_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
