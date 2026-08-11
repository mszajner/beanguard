package io.beanguard.client.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ServerConfig {
    private String url;
    private String key;
    private String secret;
}