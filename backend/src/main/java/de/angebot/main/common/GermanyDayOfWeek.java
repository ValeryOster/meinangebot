package de.angebot.main.common;

public enum GermanyDayOfWeek {
    MONTAG(1), DIENSTAG(2), MITTWOCH(3), DONNERSTAG(4), FREITAG(5), SAMSTAG(6), SONNTAG(7);

    private int dayOfWeek;
    GermanyDayOfWeek(int i) {
        this.dayOfWeek = i;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }
}
