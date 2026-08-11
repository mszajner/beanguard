package io.beanguard.server.ports;

public interface SecureStringProvider {
    String generate(int length);
}
