package dev.beanguard.client.config;

import java.util.Optional;

public interface BeanGuardConfiguration {

    /**
     * Metoda ma zwracac konfigurację połączenia do serwera licencji BeanGuard.
     *
     * licencePublicKey: Wartość z parametru LICENCE_PUBLIC_KEY w panelu admina.
     * licenceSecretKey: Wartość z parametru LICENCE_SECRET_KEY w panelu admina.
     *
     * @return {"url":"https://api.beanguard.dev","licencePublicKey":"","licenceSecretKey":""}
     */
    ServerConfig getServerConfig();

    /**
     * Metoda zwraca klucz i sekret licencji lub pusty gdy nie zostala podana.
     *
     * key: klucz licencji
     * secret: klucz prywatny licencji
     *
     * @return {"key":"","secret":""}
     */
    Optional<LicenceKeys> getLicenceKeys();

    /**
     * Metoda zwraca ostatnio zapisaną licencję metoda #saveLicence.
     *
     * @return
     */
    Optional<String> loadLicence();

    /**
     * Metoda zapisuje odebrana licencje z serwera licencji BeanGuard.
     *
     * @param licence
     */
    void saveLicence(String licence);
}
