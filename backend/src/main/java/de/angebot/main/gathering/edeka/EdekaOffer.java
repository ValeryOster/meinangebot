package de.angebot.main.gathering.edeka;

import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.gathering.common.GatheringException;
import org.springframework.stereotype.Component;

/**
 * Edeka-Sammler – derzeit deaktiviert.
 * <p>
 * Die frühere Quelle (https://www.edeka.de/api/offers, per Selenium/Chrome gelesen) antwortet seit 2026
 * mit HTTP 403, auch für normale Browser. Selenium wurde deshalb aus dem Projekt entfernt.
 * Die Tabelle {@code edeka} und bereits gesammelte Daten bleiben erhalten.
 * Für eine Wiederaufnahme wird eine neue Quelle benötigt (z. B. die Prospektseite eines Marktes).
 */
@Component("edeka")
public class EdekaOffer extends Gathering {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public int startGathering() {
        throw new GatheringException("Edeka ist deaktiviert: die Edeka-API liefert HTTP 403.");
    }

    @Override
    public String getDiscountName() {
        return "Edeka";
    }
}
