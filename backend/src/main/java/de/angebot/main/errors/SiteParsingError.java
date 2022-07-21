package de.angebot.main.errors;

public class SiteParsingError extends RuntimeException {

    public SiteParsingError(String message) {
        super(message);
    }
}
