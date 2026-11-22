package de.angebot.main.gathering.penny;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PennyOfferTest {

    @Test
    void textPrettyPrint_capitalizesWordsButNotConnectors() {
        assertThat(PennyOffer.textPrettyPrint("obst-und-gemuese")).isEqualTo("Obst und Gemuese");
        assertThat(PennyOffer.textPrettyPrint("food-highlights-fuer-alle")).isEqualTo("Food Highlights fuer Alle");
    }

    @Test
    void textPrettyPrint_handlesDoubleSeparatorsAndEmptyValues() {
        assertThat(PennyOffer.textPrettyPrint("obst--und  gemuese-")).isEqualTo("Obst und Gemuese");
        assertThat(PennyOffer.textPrettyPrint("")).isEmpty();
        assertThat(PennyOffer.textPrettyPrint(null)).isEmpty();
    }
}
