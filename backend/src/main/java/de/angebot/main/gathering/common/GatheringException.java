package de.angebot.main.gathering.common;

/** Wird geworfen, wenn eine Angebotsquelle nicht gelesen oder ausgewertet werden konnte. */
public class GatheringException extends RuntimeException {

    public GatheringException(String message) {
        super(message);
    }

    public GatheringException(String message, Throwable cause) {
        super(message, cause);
    }
}
