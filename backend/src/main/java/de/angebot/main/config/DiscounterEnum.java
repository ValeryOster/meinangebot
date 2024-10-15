package de.angebot.main.config;

public enum DiscounterEnum {
    PENNY("Penny"),
    LIDL("Lidl"),
    ALDI("Aldi"),
    NETTO("Netto"),
    EDEKA("Edeka");

    private final String discounter;

    DiscounterEnum(String discounter) {
        this.discounter = discounter;
    }

    public String getDiscounter() {
        return discounter;
    }
}
