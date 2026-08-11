package io.beanguard.server.services.impl;

import io.beanguard.server.entities.UserEntity;
import io.beanguard.server.models.ParameterName;
import io.beanguard.server.models.Role;
import io.beanguard.server.repositories.UserRepository;
import io.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Log4j2
public class ServerInitializationService implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@beanguard.dev";

    private final ParameterService parameterService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean initialized = false;

        if (parameterService.getString(ParameterName.LICENCE_PRIVATE_KEY).isEmpty()) {
            log.info("Generating RSA key pair for LICENCE keys...");
            KeyPair pair = generateRsaKeyPair();
            parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY,
                    Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
            parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY,
                    Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
            initialized = true;
        }

        if (parameterService.getString(ParameterName.LICENCE_SECRET_KEY).isEmpty()) {
            log.info("Generating AES secret key for LICENCE...");
            parameterService.setString(ParameterName.LICENCE_SECRET_KEY,
                    Base64.getEncoder().encodeToString(generateAesKey().getEncoded()));
            initialized = true;
        }

        if (parameterService.getString(ParameterName.TOKEN_PRIVATE_KEY).isEmpty()) {
            log.info("Generating RSA key pair for TOKEN keys...");
            KeyPair pair = generateRsaKeyPair();
            parameterService.setString(ParameterName.TOKEN_PRIVATE_KEY,
                    Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
            parameterService.setString(ParameterName.TOKEN_PUBLIC_KEY,
                    Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
            initialized = true;
        }

        if (parameterService.getString(ParameterName.TOKEN_SECRET_KEY).isEmpty()) {
            log.info("Generating AES secret key for TOKEN...");
            parameterService.setString(ParameterName.TOKEN_SECRET_KEY,
                    Base64.getEncoder().encodeToString(generateAesKey().getEncoded()));
            initialized = true;
        }

        if (userRepository.countByRolesContaining(Role.ADMIN) == 0) {
            createInitialAdmin();
            initialized = true;
        }

        if (initialized) {
            log.info("Server initialization complete.");
        }

        validateStartupConfig();
    }

    private void createInitialAdmin() {
        String password = generateRandomPassword();

        UserEntity admin = new UserEntity();
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(Set.of(Role.ADMIN));
        userRepository.save(admin);

        log.warn("======================================================================");
        log.warn("No admin user found — created one for first login:");
        log.warn("  email:    {}", DEFAULT_ADMIN_EMAIL);
        log.warn("  password: {}", password);
        log.warn("This password is shown only once and is not stored anywhere in plain");
        log.warn("text. Log in and change it immediately.");
        log.warn("======================================================================");
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validateStartupConfig() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        // key uses underscore to match application.yml literally; Environment.getProperty() does not apply relaxed binding
        if (isProd && "true".equals(environment.getProperty("spring.jpa.show_sql"))) {
            throw new IllegalStateException(
                "Production profile active but spring.jpa.show_sql=true — set show_sql=false before deploying");
        }
        String mailHost = parameterService.getString(ParameterName.MAIL_HOST);
        if (mailHost.isBlank()) {
            log.warn("MAIL_HOST not configured — email notifications are disabled. Set it via the admin panel.");
        }
    }

    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private SecretKey generateAesKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }
}
