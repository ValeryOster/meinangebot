package de.angebot.main.config;

public enum DiscounterEnum {
    PENNY("PENNY"),
    LIDL("LIDL"),
    ALDI("ALDI"),
    NETTO("NETTO"),
    EDEKA("EDEKA");

    private final String discounter;

    DiscounterEnum(String discounter) {
        this.discounter = discounter;
    }

    public String getDiscounter() {
        return discounter;
    }
}
