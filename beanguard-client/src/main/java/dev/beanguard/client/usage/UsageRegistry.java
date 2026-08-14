package dev.beanguard.client.usage;

public interface UsageRegistry {
    /**
     * Metoda zwraca aktualna wartosc (uzycie) limitu.
     * @param limitName Nazwa limitu
     * @return Uzycie limitu
     */
    long getUsage(String limitName);

    /**
     * Metoda zwieksza uzycie danego limitu.
     * @param limitName Nazwa limitu
     */
    void incrementUsage(String limitName);

    /**
     * Metoda zmniejsza uzycie danego limitu.
     * @param limitName Nazwa limitu
     */
    void decrementUsage(String limitName);
}
