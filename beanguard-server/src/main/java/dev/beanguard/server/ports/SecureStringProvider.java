package dev.beanguard.server.ports;

public interface SecureStringProvider {
    String generate(int length);
}
