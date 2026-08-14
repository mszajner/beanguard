package dev.beanguard.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "beanguard.demo")
public class DemoProperties {

    private Server server = new Server();
    private String storagePath = "./beanguard-demo-data";

    @Getter
    @Setter
    public static class Server {
        private String url;
        private String publicKey;
        private String secretKey;
    }
}
