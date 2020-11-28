package de.angebot.main.common;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ItemsCategory {
    FOOD("Lebensmittel"),
    STUFF("Sachen"),
    ELECTRONICS("Elektronik");

    private String kategorie;

    ItemsCategory(String kategorie) {
        this.kategorie = kategorie;
    }

    public String getKategorie() {
        return kategorie;
    }

    public static List<String> getKategorieList() {
        return Stream.of(ItemsCategory.values())
                .map(ItemsCategory::getKategorie)
                .collect(Collectors.toList());
    }
}
