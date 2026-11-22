package de.angebot.main.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @Test
    void splittToNameOrMaker_splitsLeadingUppercaseWordsAsMaker() {
        assertThat(Utils.splittToNameOrMaker("ALMETTE Frischkäsezubereitung"))
                .containsExactly("ALMETTE", "Frischkäsezubereitung");
        assertThat(Utils.splittToNameOrMaker("RIO MARE Thunfisch in Öl"))
                .containsExactly("RIO MARE", "Thunfisch in Öl");
        assertThat(Utils.splittToNameOrMaker("Brokkoli")).containsExactly("", "Brokkoli");
    }

    @Test
    void safeFileName_removesPathCharacters() {
        assertThat(Utils.safeFileName("../../etc/passwd")).isEqualTo(".etcpasswd");
        assertThat(Utils.safeFileName("40141-450x450-Kiwi.webp")).isEqualTo("40141450x450Kiwi.webp");
    }

    @Test
    void isContainExactWord_matchesWholeWordsOnly() {
        assertThat(Utils.isContainExactWord("RIO MARE THUNFISCH", "MARE")).isTrue();
        assertThat(Utils.isContainExactWord("MAREDO STEAK", "MARE")).isFalse();
    }
}
