package de.angebot.main.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Component
public class Utils {

    private static String IMAGE_DESTINATION_FOLDER;

    public static void saveHtmlToDisk(Document document) {
        final File f = new File("c:/Users/Valera/Desktop/Dokument.html");
        try {
            FileUtils.writeStringToFile(f, document.outerHtml(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> splittToNameOrMaker(String mixed) {
        String name = "";
        StringBuilder maker = new StringBuilder();
        String[] myAr = mixed.split(" ");

        for (String s : myAr) {
            if (StringUtils.isAllUpperCase(s)) {
                maker.append(" " + s);
            } else {
                name = mixed.substring(maker.length());
                break;
            }
        }
        return Arrays.asList(maker.toString()
                .trim(), name.trim());
    }

    public static String downloadImage(String strImageURL, String storageName, LocalDate endDate) {

        //get file name from image path
        String strImageName = strImageURL.substring(strImageURL.lastIndexOf("/") + 1);

        try {
            //open the stream from URL
            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n = -1;

            String path = IMAGE_DESTINATION_FOLDER + "\\" + storageName + "\\" + endDate;
            //Create Directory if not exists
            String image = Files.createDirectories(Paths.get(path))
                    .toString() + "\\" + strImageName;
            OutputStream os = new FileOutputStream(image);

            //write bytes to the output stream
            while ((n = in.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            os.close();

            String pathDB = "/" + storageName + "/" + endDate + "/" + strImageName;

            return pathDB;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static LocalDate getDate(int var) {
        int today = Calendar.getInstance()
                .get(Calendar.DAY_OF_WEEK);
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .minusDays(today - var);
    }

    public static LocalDate getLastMonday() {
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    public static LocalDate getNextMonday() {
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getNextSaturday() {
        return LocalDate.now(ZoneId.of("Europe/Paris"))
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    @Value("${main.bilder}")
    public void setFoder(String folder) {
        Utils.IMAGE_DESTINATION_FOLDER = folder;
    }
}
