package de.angebot.main.gathering.common;

/**
 * Basisklasse aller Sammler (ein Sammler je Discounter).
 * Implementierungen werden als Spring-Beans mit dem Namen des Discounters registriert ("penny", "aldi", ...).
 */
public abstract class Gathering {

    /**
     * Sammelt die Angebote und speichert sie.
     *
     * @return Anzahl neu gespeicherter Angebote
     * @throws GatheringException wenn die Quelle nicht gelesen werden konnte
     */
    public abstract int startGathering();

    /** Deaktivierte Sammler werden weder per Zeitplan noch manuell gestartet. */
    public boolean isEnabled() {
        return true;
    }

    public abstract String getDiscountName();
}
