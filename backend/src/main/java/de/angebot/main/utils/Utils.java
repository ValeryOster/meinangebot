package de.angebot.main.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.regex.Pattern;

/** Hilfsfunktionen für die Sammler: Bilder speichern, Datumswerte, Textaufbereitung. */
@Slf4j
@Component
public class Utils {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final int TIMEOUT_MS = 15_000;

    private static String imageDestinationFolder;

    @Value("${main.bilder}")
    public void setImageDestinationFolder(String folder) {
        Utils.imageDestinationFolder = folder;
    }

    /**
     * Trennt einen Titel in Hersteller (führende Wörter in GROSSBUCHSTABEN) und Produktname.
     * Beispiel: "ALMETTE Frischkäsezubereitung" → ["ALMETTE", "Frischkäsezubereitung"].
     */
    public static List<String> splittToNameOrMaker(String mixed) {
        String name = "";
        StringBuilder maker = new StringBuilder();
        for (String word : mixed.split(" ")) {
            if (StringUtils.isAllUpperCase(word)) {
                maker.append(" ").append(word);
            } else {
                name = mixed.substring(maker.length());
                break;
            }
        }
        return List.of(maker.toString().trim(), name.trim());
    }

    /**
     * Lädt ein Bild herunter und speichert es unter {@code <main.bilder>/<storageName>/<endDate>/<imageName>}.
     *
     * @return Pfad für die Oberfläche, z. B. "/penny/2026-11-28/bild.png", oder "" bei Fehler
     */
    public static String downloadImage(String imageUrl, String storageName, LocalDate endDate, String imageName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }
        String name = StringUtils.isBlank(imageName) ? imageUrl.substring(imageUrl.lastIndexOf('/') + 1) : imageName;
        String fileName = safeFileName(name);
        try {
            URLConnection connection = URI.create(imageUrl).toURL().openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            Path folder = Files.createDirectories(imageFolder(storageName, endDate));
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return webPath(storageName, endDate, fileName);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Bild {} konnte nicht geladen werden: {}", imageUrl, e.getMessage());
            return "";
        }
    }

    /**
     * Speichert bereits geladene Bilddaten im Bilderordner.
     *
     * @return Pfad für die Oberfläche, z. B. "/netto/2026-11-28/bild.webp", oder "" bei Fehler
     */
    public static String saveImage(byte[] data, String storageName, LocalDate endDate, String imageName) {
        if (data == null || data.length == 0 || StringUtils.isBlank(imageName)) {
            return "";
        }
        String fileName = safeFileName(imageName);
        try {
            Path folder = Files.createDirectories(imageFolder(storageName, endDate));
            Files.write(folder.resolve(fileName), data);
            return webPath(storageName, endDate, fileName);
        } catch (IOException e) {
            log.warn("Bild {} konnte nicht gespeichert werden: {}", fileName, e.getMessage());
            return "";
        }
    }

    public static LocalDate getNextMonday() {
        return LocalDate.now(ZONE).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getNextSaturday() {
        return LocalDate.now(ZONE).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    /** Prüft, ob {@code partWord} (ein regulärer Ausdruck) als ganzes Wort in {@code fullString} vorkommt. */
    public static boolean isContainExactWord(String fullString, String partWord) {
        return Pattern.compile("\\b" + partWord + "\\b").matcher(fullString).find();
    }

    /** Nur Buchstaben, Ziffern und Punkt – verhindert Pfadangaben wie "../" im Dateinamen. */
    static String safeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9.]+", "").replaceAll("\\.{2,}", ".");
    }

    private static Path imageFolder(String storageName, LocalDate endDate) {
        return Paths.get(imageDestinationFolder, storageName, String.valueOf(endDate));
    }

    private static String webPath(String storageName, LocalDate endDate, String fileName) {
        return "/" + storageName + "/" + endDate + "/" + fileName;
    }
}
