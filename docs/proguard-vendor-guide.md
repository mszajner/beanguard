# Ochrona kluczy kryptograficznych ProGuardem — instrukcja dla vendora

Ten dokument opisuje jak zintegrować `beanguard-client` z własną aplikacją i zabezpieczyć klucze kryptograficzne przez obfuskację ProGuardem — w Twoim projekcie.

## Dlaczego to jest konieczne

`beanguard-client` wymaga implementacji interfejsu `BeanGuardConfiguration` dostarczającej:

- **URL serwera BeanGuard** oraz **RSA public key** / **AES secret key** — z parametrów `LICENCE_PUBLIC_KEY` / `LICENCE_SECRET_KEY` w panelu admina (metoda `getServerConfig()`)
- **klucz i sekret licencji** vendora — jego własną tożsamość licencyjną (metoda `getLicenceKeys()`)

Wartości te muszą być **zakodowane w skompilowanym kodzie vendora** — nie mogą pochodzić z pliku konfiguracyjnego ani zmiennej środowiskowej. Jeśli atakujący uruchomi dekompilator na Twoim JARze, zobaczy je jako czytelne stringi — dlatego musisz przepuścić **swoją** aplikację przez ProGuard.

`beanguard-client` samo w sobie **nie jest obfuskowane** — to biblioteka open source (Apache 2.0), jej kod źródłowy jest publicznie dostępny na GitHubie, więc obfuskowanie własnych wnętrzności niczego by nie ukryło. Cała ochrona opisana w tym dokumencie dotyczy wyłącznie kluczy, które **Ty** osadzasz w swojej implementacji `BeanGuardConfiguration`.

> **Ważne:** Nawet po ekstrakcji kluczy atakujący może wyłącznie **odtworzyć** ważne licencje (replay attack) lub użyć cudzej licencji. **Nie może** tworzyć nowych licencji ani modyfikować ich treści — bo klucz prywatny RSA nigdy nie opuszcza bazy danych serwera BeanGuard.

---

## Krok 1: Implementacja BeanGuardConfiguration

Utwórz klasę implementującą `BeanGuardConfiguration` w swoim projekcie. Klucze pobierz z panelu administracyjnego serwera BeanGuard (zakładka *Ustawienia → Klucze kryptograficzne*), a klucz/sekret licencji — z panelu, w którym została ona wykupiona.

```java
package com.example.myapp.licence;

import dev.beanguard.client.config.BeanGuardConfiguration;
import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MyBeanGuardConfiguration implements BeanGuardConfiguration {

    @Override
    public ServerConfig getServerConfig() {
        return new ServerConfig(
            "https://api.beanguard.dev",
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...", // LICENCE_PUBLIC_KEY z panelu admina
            "dGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIEFFUy0yNTY=" // LICENCE_SECRET_KEY z panelu admina
        );
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        return Optional.of(new LicenceKeys(
            "11111111-1111-1111-1111-111111111111", // klucz licencji
            "my-licence-secret" // sekret licencji
        ));
    }

    @Override
    public Optional<String> loadLicence() {
        // odczytaj ostatnio zapisaną licencję, np. z pliku lub bazy Twojej aplikacji
        return Optional.empty();
    }

    @Override
    public void saveLicence(String licence) {
        // zapisz odebraną licencję, żeby przetrwała restart bez połączenia z serwerem
    }
}
```

Klasy nie umieszczaj w pakiecie `dev.beanguard.*` — to Twój kod, nie kod biblioteki.

> `UsageRegistry` (`dev.beanguard.client.usage.UsageRegistry`) to drugi interfejs, który możesz zaimplementować samodzielnie. W przeciwieństwie do `BeanGuardConfiguration` jego implementacja jest **opcjonalna** — dostarcz własną tylko jeśli chcesz trwale przechowywać liczniki użycia (np. w bazie danych) zamiast domyślnej implementacji w pamięci. Nie zawiera sekretów, więc nie wymaga ochrony ProGuardem opisanej w tym dokumencie.

---

## Krok 2: Dodanie ProGuard do projektu vendora

Dodaj `proguard-maven-plugin` do `pom.xml` swojej aplikacji. Zakładamy, że budujesz fat-JAR (Spring Boot executable JAR via `spring-boot-maven-plugin`).

> **Uwaga:** ProGuard i Spring Boot executable JAR nie są kompatybilne bezpośrednio. Poniższa konfiguracja obfuskuje **thin JAR** (przed przepakowaniem przez Spring Boot). Alternatywnie możesz wydzielić `BeanGuardConfiguration` do osobnego modułu Maven i obfuskować tylko ten moduł.

### Opcja A: Osobny moduł Maven (rekomendowana)

Wydziel implementację `BeanGuardConfiguration` do modułu `myapp-licence-keys`:

```
myapp/
├── myapp-app/          ← główna aplikacja Spring Boot
└── myapp-licence-keys/ ← tylko implementacja BeanGuardConfiguration, obfuskowany osobno
    └── pom.xml
```

W `myapp-licence-keys/pom.xml` dodaj:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.wvengen</groupId>
            <artifactId>proguard-maven-plugin</artifactId>
            <version>2.6.1</version>
            <dependencies>
                <dependency>
                    <groupId>com.guardsquare</groupId>
                    <artifactId>proguard-base</artifactId>
                    <version>7.5.0</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>proguard</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <injar>${project.build.finalName}.jar</injar>
                <outjar>${project.build.finalName}.jar</outjar>
                <outputDirectory>${project.build.directory}</outputDirectory>
                <obfuscate>true</obfuscate>
                <addMavenDescriptor>false</addMavenDescriptor>
                <libs>
                    <!-- JDK rt.jar / modules — ProGuard musi wiedzieć gdzie szukać klas JDK -->
                    <lib>${java.home}/jmods</lib>
                </libs>
                <options>
                    <option>-dontshrink</option>
                    <option>-dontoptimize</option>
                    <option>-keepattributes *Annotation*,Signature,Exceptions</option>
                    <option>-dontusemixedcaseclassnames</option>
                    <option>-dontwarn **</option>
                    <!--
                        Zachowaj adnotację @Component żeby Spring mógł wykryć bean przez component scan.
                        Sama nazwa klasy i metody zostaną zmienione przez ProGuard — to właśnie ta klasa
                        (Twoja implementacja) ma zostać obfuskowana, nie BeanGuardConfiguration.
                    -->
                    <option>-keepclassmembers class * implements dev.beanguard.client.config.BeanGuardConfiguration {
                        @org.springframework.stereotype.Component *;
                    }</option>
                    <!--
                        NIE dodawaj tutaj reguły -keep dla MyBeanGuardConfiguration ani dla
                        BeanGuardConfiguration/ServerConfig/LicenceKeys — nie musisz. `beanguard-client`
                        jest tu wyłącznie zewnętrzną zależnością (-libraryjars), a nie czymś, co ten
                        przebieg ProGuard przetwarza (-injars) — ProGuard w ogóle nie zmienia nazw klas
                        spoza -injars, więc Twój kod bezpiecznie kompiluje się i linkuje przeciwko
                        stabilnym, publicznym nazwom z beanguard-client bez żadnej dodatkowej reguły.
                    -->
                </options>
            </configuration>
        </plugin>
    </plugins>
</build>
```

W `myapp-app/pom.xml` dodaj zależność do obfuskowanego modułu:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>myapp-licence-keys</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Opcja B: Klucze jako tablica bajtów zamiast Stringa

Dekompilatory takie jak Fernflower lub CFR wyświetlają stringi w czytelnej formie nawet po obfuskacji nazw. Trudniejsza do odczytania jest tablica bajtów:

```java
@Override
public ServerConfig getServerConfig() {
    // Każdy bajt to jeden znak URL-a — dekompilator pokaże tablicę liczb, nie string
    byte[] encoded = {
        104, 116, 116, 112, 115, 58, 47, 47, 97, 112, 105, 46,
        98, 101, 97, 110, 103, 117, 97, 114, 100, 46, 100, 101, 118,
        // ... pozostałe bajty
    };
    return new ServerConfig(new String(encoded), "...", "...");
}
```

Wygeneruj tablicę bajtów z gotowego tekstu:

```bash
python3 -c "
payload = 'https://api.beanguard.dev'
print(', '.join(str(b) for b in payload.encode('utf-8')))
"
```

Tę technikę możesz łączyć z ProGuardem — daje dwie warstwy utrudnienia.

---

## Krok 3: Weryfikacja obfuskacji

Po zbudowaniu sprawdź że nazwa Twojej klasy jest zmieniona:

```bash
jar tf target/myapp-licence-keys-*.jar | grep "com/example"
```

Prawidłowy output — nazwa klasy zmieniona na jedną literę lub krótki ciąg:

```
com/example/myapp/licence/a.class
```

Nieprawidłowy output — klasa widoczna pod oryginalną nazwą:

```
com/example/myapp/licence/MyBeanGuardConfiguration.class
```

Sprawdź też czy Spring nadal wykrywa bean:

```bash
mvn spring-boot:run
```

W logach przy starcie powinien pojawić się komunikat z BeanGuard o poprawnym wczytaniu licencji.

---

## Krok 4: Konfiguracja

`BeanGuardConfiguration` **nie pochodzi z `application.yml`** — to jest zamierzone. URL serwera i klucze są w kodzie, w implementacji dostarczonej przez Twój projekt (Krok 1). `beanguard-client` nie czyta żadnych property'ów Springa dla tej konfiguracji — bean `BeanGuardServer` powstaje automatycznie, gdy tylko w kontekście Springa znajdzie się bean typu `BeanGuardConfiguration`.

---

## (Zaawansowane, opcjonalne) Obfuskacja beanguard-client razem z Twoją aplikacją

Jeśli obfuskujesz **całą** swoją aplikację w jednym przebiegu ProGuard/R8 (typowa praktyka dla komercyjnego software, niezależna od BeanGuarda) i chcesz w tym samym przebiegu przetworzyć też klasy `beanguard-client` — np. żeby ukryć sam fakt, że aplikacja używa biblioteki licencyjnej — możesz dołączyć jar `beanguard-client` do swoich `-injars`. Wtedy jednak musisz jawnie zachować kilka klas, na które Twój kod (i Spring) odwołuje się po nazwie:

```
# Punkt wejścia autokonfiguracji Spring Boot — referencjonowany po pełnej nazwie
# klasy w zasobie META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports.
# ProGuard nie przepisuje plików zasobów, więc ta nazwa klasy musi pozostać bez zmian.
-keep class dev.beanguard.client.BeanGuardClientAutoConfiguration { *; }

# Interfejs, który implementujesz (Krok 1) i typy, które konstruujesz bezpośrednio.
-keep public interface dev.beanguard.client.config.BeanGuardConfiguration { *; }
-keep public class dev.beanguard.client.config.ServerConfig { *; }
-keep public class dev.beanguard.client.config.LicenceKeys { *; }

# Opcjonalny interfejs rozszerzenia (Krok 1) — pomiń, jeśli go nie implementujesz.
-keep public interface dev.beanguard.client.usage.UsageRegistry { *; }

# Spring musi znaleźć te metody refleksyjnie po adnotacji.
-keepclassmembers class * {
    @org.springframework.scheduling.annotation.Scheduled *;
    @org.springframework.context.event.EventListener *;
}

# Adnotacje pointcutów AspectJ muszą przetrwać obfuskację, inaczej weaving AOP
# (walidacja licencji) przestaje działać po dołączeniu tych klas do przebiegu.
-keepattributes *Annotation*,Signature,Exceptions,InnerClasses,EnclosingMethod
```

`LicenceRegistry` i `LicenceStatus` (`dev.beanguard.client.registries.*`) możesz swobodnie zostawić bez reguły `-keep` — zostaną spójnie przemianowane razem z resztą Twojego programu w tym samym przebiegu, bo cały Twój kod jest w tym samym `-injars`. Jeśli wolisz, żeby pozostały czytelne (np. do debugowania na produkcji), dodaj:

```
-keep public interface dev.beanguard.client.registries.LicenceRegistry { *; }
-keep public enum dev.beanguard.client.registries.LicenceStatus { *; }
```

> **Ważne:** Nie próbuj obfuskować klas `beanguard-client` w oderwaniu od reszty swojej aplikacji (osobnym przebiegiem tylko na `beanguard-client.jar`) — wtedy Twój skompilowany kod nadal odwołuje się do oryginalnych nazw klas, a w runtime dostanie inne (przemianowane) klasy pod tą samą ścieżką w classpath, co skończy się `NoSuchMethodError`/`ClassCastException`. Obfuskacja `beanguard-client` ma sens wyłącznie jako część jednego, całościowego przebiegu obejmującego też Twój kod.
>
> Dla większości vendorów to niepotrzebne — Opcja A z Kroku 2 (osobny moduł, `beanguard-client` jako zwykła zależność) jest prostsza i wystarczająca: chroni Twoje klucze, a `beanguard-client` i tak jest publicznym kodem open source, więc nie ma czego przed nikim ukrywać.

---

## Co ProGuard chroni, a czego nie

| Wektor ataku | Po obfuskacji |
|---|---|
| Dekompilator odczytuje nazwę klasy | Widzi `a.class` zamiast `MyBeanGuardConfiguration` |
| Dekompilator odczytuje nazwy metod | Widzi `a()`, `b()` zamiast `getServerConfig()` |
| Dekompilator odczytuje treść stringa | **Nadal widzi klucz** — chyba że użyjesz tablicy bajtów |
| Java agent hookuje metodę w runtime | Może odczytać klucz — poza zakresem ProGuard CE |
| Heap dump działającej JVM | Może znaleźć klucz w pamięci — poza zakresem ProGuard CE |

Ochrona przed pełną ekstrakcją kluczy (Java agenty, heap dump) wymaga narzędzi komercyjnych (DexGuard, Dotfuscator) lub modeli sprzętowych (HSM). Dla typowego use-case'u obfuskacja ProGuardem jest wystarczającym deterrentem.
