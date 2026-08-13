# beanguard-demo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a new `beanguard-demo` Maven module — a runnable Spring Boot MVC + Thymeleaf app that showcases `beanguard-client`: generate a demo licence via a form, view its details, refresh it, and deep-link into `beanguard-shop` to extend it.

**Architecture:** Single Spring Boot app with one `@Controller`. A `DemoLicenceKeyStore` interface (extends `beanguard-client`'s `BeanGuardConfiguration`) has two Spring-profile-gated implementations — file-backed and in-memory — so the module demonstrates both persistence strategies for the licence's own key/secret. The controller drives `LicenceRegistry`/`BeanGuardServer` from `beanguard-client` for licence fetch/refresh, and makes one direct HTTP call (not wrapped by the SDK) to `beanguard-server`'s shop-token endpoint for the "extend licence" deep link.

**Tech Stack:** Java 21, Spring Boot 4.1.0 (inherited parent), Spring MVC, Thymeleaf, Jakarta Validation, Lombok, `beanguard-client`/`beanguard-api` (reactor SNAPSHOT), Spring's `RestClient`.

**Spec:** `docs/superpowers/specs/2026-08-13-beanguard-demo-design.md`

## Global Constraints

- New module `beanguard-demo` added to root `pom.xml`'s `<modules>`, package root `io.beanguard.demo`.
- `<parent>` = `dev.beanguard:beanguard:0.1.3-SNAPSHOT` (matches other modules), `relativePath` = `../pom.xml`.
- No `Dockerfile`, no CI/publish wiring — this module is a runnable example only.
- No automated test suite (Spock or otherwise) — explicitly decided against; every task is verified by compiling and actually running the app (curl / log inspection / manual browser walkthrough), not by `mvn test`.
- No external CDN/JS framework in the view — plain embedded `<style>` only, so the demo runs without internet access.
- Default active Spring profile: `file`. Alternate: `memory`. Never both active at once (each registers a `BeanGuardConfiguration` bean; two active at once breaks `@ConditionalOnBean` wiring and would violate the anti-tamper duplicate-registration guard in `BeanGuardClientAutoConfiguration`).

---

### Task 1: Module scaffold — pom.xml, main class, config, boot check

**Files:**
- Modify: `pom.xml:21-25` (root, add module entry)
- Create: `beanguard-demo/pom.xml`
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java`
- Create: `beanguard-demo/src/main/resources/application.yml`

**Interfaces:**
- Consumes: nothing yet (first task).
- Produces: a booting Spring Boot app on port 8090, package root `io.beanguard.demo`, with `DemoProperties.class` registered for `@EnableConfigurationProperties` (the class itself is created in Task 2 — this task only wires the annotation reference, which will fail to compile until Task 2 lands, so **Task 1's steps create `DemoApplication.java` without the `@EnableConfigurationProperties` annotation**; Task 2 adds it when `DemoProperties` exists). This keeps Task 1 compilable and runnable in isolation.

- [ ] **Step 1: Add the module to the root pom.xml**

Edit `pom.xml`, in the `<modules>` block:

```xml
<modules>
    <module>beanguard-api</module>
    <module>beanguard-client</module>
    <module>beanguard-server</module>
    <module>beanguard-demo</module>
</modules>
```

- [ ] **Step 2: Create `beanguard-demo/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>dev.beanguard</groupId>
        <artifactId>beanguard</artifactId>
        <version>0.1.3-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>beanguard-demo</artifactId>
    <name>BeanGuard Demo</name>
    <description>Runnable example Spring Boot app showing beanguard-client capabilities.</description>

    <dependencies>
        <dependency>
            <groupId>dev.beanguard</groupId>
            <artifactId>beanguard-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 3: Create the main application class**

`beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java`:

```java
package io.beanguard.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

(Task 2 adds `@EnableConfigurationProperties(DemoProperties.class)` once `DemoProperties` exists.)

- [ ] **Step 4: Create `application.yml`**

`beanguard-demo/src/main/resources/application.yml`:

```yaml
server:
  port: 8090

spring:
  application:
    name: beanguard-demo
  profiles:
    active: file

beanguard:
  demo:
    server:
      url: http://localhost:8080
      public-key: ${BEANGUARD_SERVER_PUBLIC_KEY:}
      secret-key: ${BEANGUARD_SERVER_SECRET_KEY:}
    storage-path: ./beanguard-demo-data
```

- [ ] **Step 5: Build the module**

Run: `mvn -pl beanguard-demo -am clean install -DskipTests`
Expected: `BUILD SUCCESS`, reactor builds `beanguard-api`, `beanguard-client`, `beanguard-demo` in order.

- [ ] **Step 6: Boot check**

Run as a single command so the background PID is capturable without relying on shell job control:

```bash
mvn -pl beanguard-demo spring-boot:run > /tmp/beanguard-demo-boot.log 2>&1 &
PID=$!
sleep 12
grep -E "Started DemoApplication|ERROR" /tmp/beanguard-demo-boot.log
kill $PID
```

Expected: a line containing `Started DemoApplication` and no `ERROR` lines.

- [ ] **Step 7: Commit**

```bash
git add pom.xml beanguard-demo/pom.xml beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java beanguard-demo/src/main/resources/application.yml
git commit -m "Scaffold beanguard-demo module"
```

---

### Task 2: Licence persistence layer — DemoProperties, DemoLicenceKeyStore, two profiles

**Files:**
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/config/DemoProperties.java`
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/licence/DemoLicenceKeyStore.java`
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/licence/FileBeanGuardConfiguration.java`
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/licence/InMemoryBeanGuardConfiguration.java`
- Modify: `beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java` (add `@EnableConfigurationProperties`)

**Interfaces:**
- Consumes: `io.beanguard.client.config.BeanGuardConfiguration` (methods `getServerConfig()`, `getLicenceKeys()`, `loadLicence()`, `saveLicence(String)`), `io.beanguard.client.config.ServerConfig` (ctor `ServerConfig(String url, String key, String secret)`), `io.beanguard.client.config.LicenceKeys` (ctor `LicenceKeys(String key, String secret)`, getters `getKey()`/`getSecret()`).
- Produces: `io.beanguard.demo.config.DemoProperties` with `getServer().getUrl()/getPublicKey()/getSecretKey()` and `getStoragePath()`. `io.beanguard.demo.licence.DemoLicenceKeyStore` interface with `void storeLicenceKeys(LicenceKeys keys)` (used by Task 3's controller). Exactly one of `FileBeanGuardConfiguration`/`InMemoryBeanGuardConfiguration` is an active Spring bean of type `DemoLicenceKeyStore` depending on `spring.profiles.active`.

- [ ] **Step 1: Create `DemoProperties`**

`beanguard-demo/src/main/java/io/beanguard/demo/config/DemoProperties.java`:

```java
package io.beanguard.demo.config;

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
```

- [ ] **Step 2: Register it on the main class**

Edit `beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java`:

```java
package io.beanguard.demo;

import io.beanguard.demo.config.DemoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DemoProperties.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

- [ ] **Step 3: Create the `DemoLicenceKeyStore` interface**

`beanguard-demo/src/main/java/io/beanguard/demo/licence/DemoLicenceKeyStore.java`:

```java
package io.beanguard.demo.licence;

import io.beanguard.client.config.BeanGuardConfiguration;
import io.beanguard.client.config.LicenceKeys;

public interface DemoLicenceKeyStore extends BeanGuardConfiguration {

    void storeLicenceKeys(LicenceKeys keys);
}
```

- [ ] **Step 4: Create the file-backed implementation**

`beanguard-demo/src/main/java/io/beanguard/demo/licence/FileBeanGuardConfiguration.java`:

```java
package io.beanguard.demo.licence;

import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.config.ServerConfig;
import io.beanguard.demo.config.DemoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@Profile("file")
@Component
public class FileBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private final Path keysFile;
    private final Path licenceFile;

    public FileBeanGuardConfiguration(DemoProperties properties) {
        this.properties = properties;
        Path storagePath = Path.of(properties.getStoragePath());
        this.keysFile = storagePath.resolve("licence-keys.properties");
        this.licenceFile = storagePath.resolve("licence.raw");
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ServerConfig getServerConfig() {
        DemoProperties.Server server = properties.getServer();
        return new ServerConfig(server.getUrl(), server.getPublicKey(), server.getSecretKey());
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        if (!Files.exists(keysFile)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(keysFile, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String key = props.getProperty("key");
        String secret = props.getProperty("secret");
        if (key == null || secret == null) {
            return Optional.empty();
        }
        return Optional.of(new LicenceKeys(key, secret));
    }

    @Override
    public void storeLicenceKeys(LicenceKeys keys) {
        Properties props = new Properties();
        props.setProperty("key", keys.getKey());
        props.setProperty("secret", keys.getSecret());
        try (var writer = Files.newBufferedWriter(keysFile, StandardCharsets.UTF_8)) {
            props.store(writer, "BeanGuard demo licence keys");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<String> loadLicence() {
        // beanguard-client does not currently call this hook (verified by
        // grep across beanguard-client's sources); implemented honestly in
        // case a future SDK version wires it up for offline fallback.
        if (!Files.exists(licenceFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(licenceFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void saveLicence(String licence) {
        try {
            Files.writeString(licenceFile, licence, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- [ ] **Step 5: Create the in-memory implementation**

`beanguard-demo/src/main/java/io/beanguard/demo/licence/InMemoryBeanGuardConfiguration.java`:

```java
package io.beanguard.demo.licence;

import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.config.ServerConfig;
import io.beanguard.demo.config.DemoProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("memory")
@Component
public class InMemoryBeanGuardConfiguration implements DemoLicenceKeyStore {

    private final DemoProperties properties;
    private volatile LicenceKeys licenceKeys;
    private volatile String rawLicence;

    public InMemoryBeanGuardConfiguration(DemoProperties properties) {
        this.properties = properties;
    }

    @Override
    public ServerConfig getServerConfig() {
        DemoProperties.Server server = properties.getServer();
        return new ServerConfig(server.getUrl(), server.getPublicKey(), server.getSecretKey());
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        return Optional.ofNullable(licenceKeys);
    }

    @Override
    public void storeLicenceKeys(LicenceKeys keys) {
        this.licenceKeys = keys;
    }

    @Override
    public Optional<String> loadLicence() {
        // Not currently called by beanguard-client; the memory profile loses
        // this on restart by design (that's the point of the profile).
        return Optional.ofNullable(rawLicence);
    }

    @Override
    public void saveLicence(String licence) {
        this.rawLicence = licence;
    }
}
```

- [ ] **Step 6: Compile**

Run: `mvn -pl beanguard-demo -am compile`
Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Boot check under the `file` profile**

```bash
mvn -pl beanguard-demo spring-boot:run > /tmp/beanguard-demo-file.log 2>&1 &
PID=$!
sleep 12
grep -E "Started DemoApplication|ERROR|Exception" /tmp/beanguard-demo-file.log
ls beanguard-demo-data
kill $PID
```

Expected: `Started DemoApplication`, no `ERROR`/`Exception` lines, and `beanguard-demo-data/` exists. This works with no live `beanguard-server` — `LicenceRegistryDefault.refreshLicence()` runs on startup, finds no persisted keys (`getLicenceKeys()` → `Optional.empty()`), and `BeanGuardServer.getLicence()` returns `Optional.empty()` immediately without making an HTTP call. After confirming, clean up: `rm -rf beanguard-demo-data`.

- [ ] **Step 8: Boot check under the `memory` profile**

```bash
mvn -pl beanguard-demo spring-boot:run -Dspring-boot.run.profiles=memory > /tmp/beanguard-demo-memory.log 2>&1 &
PID=$!
sleep 12
grep -E "Started DemoApplication|ERROR|Exception" /tmp/beanguard-demo-memory.log
kill $PID
```

Expected: same clean startup, and no `beanguard-demo-data` directory created this time.

- [ ] **Step 9: Commit**

```bash
git add beanguard-demo/src/main/java/io/beanguard/demo/config beanguard-demo/src/main/java/io/beanguard/demo/licence beanguard-demo/src/main/java/io/beanguard/demo/DemoApplication.java
git commit -m "Add file- and memory-backed licence key persistence for beanguard-demo"
```

---

### Task 3: Web layer, README, and full end-to-end verification

**Files:**
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/web/DemoLicenceRequestForm.java`
- Create: `beanguard-demo/src/main/java/io/beanguard/demo/web/DemoController.java`
- Create: `beanguard-demo/src/main/resources/templates/index.html`
- Create: `beanguard-demo/README.md`

**Interfaces:**
- Consumes: `io.beanguard.client.registries.LicenceRegistry` (`getStatus()` → `LicenceStatus`, `getLicence()` → `Licence` or null, `refreshLicence()`), `io.beanguard.client.registries.LicenceStatus` enum (`NOT_LOADED, MISSING_KEY, WRONG_KEY, WRONG_SECRET, EXPIRED, LOADED`), `io.beanguard.client.server.BeanGuardServer` (`createDemoLicence(LicenceDemoCreateRequest) throws BeanGuardServerException` → `Licence`), `io.beanguard.client.server.BeanGuardServerException` (checked), `io.beanguard.api.models.licence.Licence` (getters: `getKey()` UUID, `getSecret()`, `getCompanyName()`, `getStreet()`, `getPostCode()`, `getCity()`, `getVatId()`, `getEmail()`, `getPhoneNumber()`, `getClaims()` `Map<String,String>`, `getType()`, `getNetAmount()`, `getExpiration()`), `io.beanguard.api.models.licence.LicenceDemoCreateRequest` (ctor `(String email, String vatId)`), `io.beanguard.api.models.shop.LicenceTokenResponse` (`getShopUrl()`), `io.beanguard.api.validators.PolishNIP` annotation, `io.beanguard.demo.licence.DemoLicenceKeyStore.storeLicenceKeys(LicenceKeys)` (Task 2), `io.beanguard.demo.config.DemoProperties.getServer().getUrl()` (Task 2).
- Produces: a fully working demo app — nothing downstream consumes this task's output within this plan.

- [ ] **Step 1: Create the form DTO**

`beanguard-demo/src/main/java/io/beanguard/demo/web/DemoLicenceRequestForm.java`:

```java
package io.beanguard.demo.web;

import io.beanguard.api.validators.PolishNIP;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoLicenceRequestForm {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @PolishNIP
    private String vatId;
}
```

- [ ] **Step 2: Create the controller**

`beanguard-demo/src/main/java/io/beanguard/demo/web/DemoController.java`:

```java
package io.beanguard.demo.web;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.api.models.licence.LicenceDemoCreateRequest;
import io.beanguard.api.models.shop.LicenceTokenResponse;
import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.registries.LicenceRegistry;
import io.beanguard.client.registries.LicenceStatus;
import io.beanguard.client.server.BeanGuardServer;
import io.beanguard.client.server.BeanGuardServerException;
import io.beanguard.demo.config.DemoProperties;
import io.beanguard.demo.licence.DemoLicenceKeyStore;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
public class DemoController {

    private final LicenceRegistry licenceRegistry;
    private final BeanGuardServer beanGuardServer;
    private final DemoLicenceKeyStore licenceKeyStore;
    private final DemoProperties properties;
    private final RestClient restClient = RestClient.create();

    public DemoController(LicenceRegistry licenceRegistry,
                           BeanGuardServer beanGuardServer,
                           DemoLicenceKeyStore licenceKeyStore,
                           DemoProperties properties) {
        this.licenceRegistry = licenceRegistry;
        this.beanGuardServer = beanGuardServer;
        this.licenceKeyStore = licenceKeyStore;
        this.properties = properties;
    }

    @GetMapping("/")
    public String index(Model model) {
        LicenceStatus status = licenceRegistry.getStatus();
        model.addAttribute("status", status);
        if (status == LicenceStatus.LOADED) {
            model.addAttribute("licence", licenceRegistry.getLicence());
        } else if (!model.containsAttribute("form")) {
            model.addAttribute("form", new DemoLicenceRequestForm());
        }
        return "index";
    }

    @PostMapping("/demo-licence")
    public String generateDemoLicence(@Valid @ModelAttribute("form") DemoLicenceRequestForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/";
        }
        try {
            Licence licence = beanGuardServer.createDemoLicence(
                    new LicenceDemoCreateRequest(form.getEmail(), form.getVatId()));
            licenceKeyStore.storeLicenceKeys(new LicenceKeys(licence.getKey().toString(), licence.getSecret()));
            licenceRegistry.refreshLicence();
        } catch (BeanGuardServerException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się wygenerować licencji demo: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/refresh")
    public String refresh() {
        licenceRegistry.refreshLicence();
        return "redirect:/";
    }

    @GetMapping("/extend")
    public String extend(RedirectAttributes redirectAttributes) {
        Licence licence = licenceRegistry.getLicence();
        if (licence == null) {
            redirectAttributes.addFlashAttribute("error", "Brak wczytanej licencji do przedłużenia.");
            return "redirect:/";
        }
        try {
            String credentials = licence.getKey() + ":" + licence.getSecret();
            String authorization = "KeySecret " + Base64.getEncoder().encodeToString(credentials.getBytes());
            LicenceTokenResponse response = restClient.post()
                    .uri(properties.getServer().getUrl() + "/api/open/licences/token")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(LicenceTokenResponse.class);
            return "redirect:" + response.getShopUrl();
        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Nie udało się połączyć ze sklepem: " + e.getMessage());
            return "redirect:/";
        }
    }
}
```

- [ ] **Step 3: Create the view**

`beanguard-demo/src/main/resources/templates/index.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="pl">
<head>
    <meta charset="UTF-8"/>
    <title>BeanGuard Demo</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 640px; margin: 3rem auto; padding: 0 1rem; color: #1f2933; background: #f8fafc; }
        h1 { font-size: 1.5rem; }
        .card { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; margin-top: 1rem; }
        .status { font-size: 0.9rem; color: #64748b; margin-bottom: 1rem; }
        .error { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 6px; padding: 0.75rem 1rem; margin-bottom: 1rem; }
        label { display: block; font-weight: 600; margin-top: 1rem; }
        input[type=text], input[type=email] { width: 100%; padding: 0.5rem; margin-top: 0.25rem; border: 1px solid #cbd5e1; border-radius: 4px; box-sizing: border-box; }
        .field-error { color: #b91c1c; font-size: 0.85rem; margin-top: 0.25rem; }
        button, .btn { display: inline-block; margin-top: 1.5rem; margin-right: 0.75rem; padding: 0.6rem 1.2rem; border: none; border-radius: 6px; background: #2563eb; color: #fff; font-weight: 600; cursor: pointer; text-decoration: none; }
        button.secondary { background: #475569; }
        dl { display: grid; grid-template-columns: 10rem 1fr; row-gap: 0.5rem; column-gap: 1rem; margin: 0; }
        dt { font-weight: 600; color: #475569; }
        dd { margin: 0; }
        .claims-list { list-style: none; padding: 0; margin: 0; }
    </style>
</head>
<body>
<h1>BeanGuard Demo</h1>

<div class="error" th:if="${error}" th:text="${error}"></div>

<div class="card" th:if="${licence == null}">
    <p class="status" th:switch="${status.name()}">
        <span th:case="'MISSING_KEY'">Brak wczytanej licencji — klucz nie został jeszcze skonfigurowany.</span>
        <span th:case="'EXPIRED'">Licencja demo wygasła — wygeneruj nową.</span>
        <span th:case="'WRONG_KEY'">Nieprawidłowy klucz licencji.</span>
        <span th:case="'WRONG_SECRET'">Nieprawidłowy sekret licencji.</span>
        <span th:case="*">Brak wczytanej licencji.</span>
    </p>
    <form th:action="@{/demo-licence}" th:object="${form}" method="post">
        <label for="email">Email</label>
        <input type="email" id="email" th:field="*{email}"/>
        <div class="field-error" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></div>

        <label for="vatId">NIP</label>
        <input type="text" id="vatId" th:field="*{vatId}"/>
        <div class="field-error" th:if="${#fields.hasErrors('vatId')}" th:errors="*{vatId}"></div>

        <button type="submit">Wygeneruj licencję demo</button>
    </form>
</div>

<div class="card" th:if="${licence != null}">
    <dl>
        <dt>Klucz</dt>
        <dd th:text="${licence.key}"></dd>
        <dt>Firma</dt>
        <dd th:text="${licence.companyName}"></dd>
        <dt>Adres</dt>
        <dd th:text="${licence.street} + ', ' + ${licence.postCode} + ' ' + ${licence.city}"></dd>
        <dt>NIP</dt>
        <dd th:text="${licence.vatId}"></dd>
        <dt>Email</dt>
        <dd th:text="${licence.email}"></dd>
        <dt>Telefon</dt>
        <dd th:text="${licence.phoneNumber}"></dd>
        <dt>Typ</dt>
        <dd th:text="${licence.type}"></dd>
        <dt>Kwota netto</dt>
        <dd th:text="${licence.netAmount}"></dd>
        <dt>Ważna do</dt>
        <dd th:text="${licence.expiration}"></dd>
    </dl>

    <h3>Claims</h3>
    <ul class="claims-list">
        <li th:each="claim : ${licence.claims}" th:text="${claim.key} + ': ' + ${claim.value}"></li>
        <li th:if="${licence.claims == null or #maps.isEmpty(licence.claims)}">Brak dodatkowych claimów.</li>
    </ul>

    <a class="btn" th:href="@{/extend}" target="_blank">Przedłuż licencję</a>
    <form th:action="@{/refresh}" method="post" style="display: inline;">
        <button type="submit" class="secondary">Odśwież licencję</button>
    </form>
</div>
</body>
</html>
```

- [ ] **Step 4: Create the README**

`beanguard-demo/README.md`:

```markdown
# beanguard-demo

Runnable example Spring Boot app showing `beanguard-client` in action: fetching a
licence, displaying its details, refreshing it, and deep-linking into
`beanguard-shop` to extend it.

## Prerequisites

A running `beanguard-server` (see the root `CLAUDE.md` "Run server locally" section):

    cd beanguard-server
    docker-compose up -d
    mvn spring-boot:run

From the admin panel (Ustawienia → Klucze kryptograficzne), grab `LICENCE_PUBLIC_KEY`
and `LICENCE_SECRET_KEY`.

## Configuration

Set these before running (env vars, or edit `src/main/resources/application.yml`
directly):

- `BEANGUARD_SERVER_PUBLIC_KEY` — the server's `LICENCE_PUBLIC_KEY`
- `BEANGUARD_SERVER_SECRET_KEY` — the server's `LICENCE_SECRET_KEY`

`beanguard.demo.server.url` defaults to `http://localhost:8080`.

## Persistence profiles

- `file` (default) — the generated demo licence's key/secret persist to
  `./beanguard-demo-data/`, surviving app restarts.
- `memory` — kept in memory only; a restart clears the licence and the form
  reappears.

Switch with `--spring.profiles.active=memory` or by editing `application.yml`.

## Run

    mvn -pl beanguard-demo spring-boot:run

Then open http://localhost:8090.
```

- [ ] **Step 5: Build**

Run: `mvn -pl beanguard-demo -am clean install -DskipTests`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Full manual end-to-end verification**

This requires a live `beanguard-server`. If one isn't already running:

```bash
cd beanguard-server
docker-compose up -d
mvn spring-boot:run
```

Get `LICENCE_PUBLIC_KEY`/`LICENCE_SECRET_KEY` from the admin panel and export them:

```bash
export BEANGUARD_SERVER_PUBLIC_KEY=...
export BEANGUARD_SERVER_SECRET_KEY=...
```

Then, in a second terminal:

```bash
mvn -pl beanguard-demo spring-boot:run
```

Walk through in a browser at `http://localhost:8090`:
1. Confirm the form (email + NIP) is shown, not the details view.
2. Submit an invalid email and a malformed NIP — confirm inline field errors appear and the form is redisplayed with the previously-entered values.
3. Submit a valid email + valid Polish NIP — confirm redirect to `/` now shows the details view: key, company name, address, VAT ID, email, phone, type (`DEMO`), net amount, expiration date, and a claims list (or "Brak dodatkowych claimów.").
4. Click "Odśwież licencję" — confirm the page reloads without error and still shows details.
5. Click "Przedłuż licencję" — confirm it opens a **new tab** and lands on the `beanguard-shop` page (not an error page).
6. Stop the demo app (Ctrl+C), restart it (`mvn -pl beanguard-demo spring-boot:run`, still `file` profile) — confirm the details view appears immediately with no form, proving the file-persisted keys survived the restart.
7. Stop the demo app, restart with `mvn -pl beanguard-demo spring-boot:run -Dspring-boot.run.profiles=memory` — confirm the form appears again (in-memory keys were lost on restart, as designed).
8. Generate a new demo licence under the `memory` profile to confirm that profile's write path also works end-to-end.

- [ ] **Step 7: Commit**

```bash
git add beanguard-demo/src/main/java/io/beanguard/demo/web beanguard-demo/src/main/resources/templates beanguard-demo/README.md
git commit -m "Add web layer and README for beanguard-demo"
```
