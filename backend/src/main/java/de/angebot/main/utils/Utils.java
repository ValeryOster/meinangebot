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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utils {

    private static String IMAGE_DESTINATION_FOLDER;

    public static void saveHtmlToDisk(Document document, String pfad) {
        if (pfad == null || pfad.isEmpty()) {
            pfad = "c:/Users/Valera/Desktop/Dokument.html";
        }
        final File f = new File(pfad);
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
        return Arrays.asList(maker.toString().trim(), name.trim());
    }

    public static String downloadImageNextSaturdayWithOutName(String strImageURL, String storageName) {
        return downloadImage(strImageURL, storageName, getNextSaturday(), "");
    }

    public static String downloadImage(String strImageURL, String storageName, LocalDate endDate, String imageName) {

        if (strImageURL == null || strImageURL.isEmpty()) {
            return "";
        }

        //get file name from image path
        if (imageName == null || imageName.isEmpty()) {
            imageName = strImageURL.substring(strImageURL.lastIndexOf("/") + 1);
        }
        //delete all special character,
        imageName = imageName.replaceAll("[^a-zA-Z0-9]+","");
        try {
            //open the stream from URL
            URL urlImage = new URL(strImageURL);
            InputStream in = urlImage.openStream();

            byte[] buffer = new byte[4096];
            int n = -1;
            String path = IMAGE_DESTINATION_FOLDER + "/" + storageName + "/" + endDate;
            //Create Directory if not exists
            String image = Files.createDirectories(Paths.get(path)).toString() + "/" + imageName;
            OutputStream os = new FileOutputStream(image);

            //write bytes to the output stream
            while ((n = in.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            os.close();

            return  "/" + storageName + "/" + endDate + "/" + imageName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static LocalDate getDate(int var) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return LocalDate.now(ZoneId.of("Europe/Paris")).minusDays(today - var);
    }

    public static LocalDate getLastMonday() {
        return LocalDate.now(ZoneId.of("Europe/Paris")).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getNextMonday() {
        return LocalDate.now(ZoneId.of("Europe/Paris")).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getNextSaturday() {
        return LocalDate.now(ZoneId.of("Europe/Paris")).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    public static LocalDate getDateFromString(String str) {
        if (str.toLowerCase().contains("montag")) {
            return getSuitableDay(DayOfWeek.MONDAY);
        } else if (str.toLowerCase().contains("dienstag")) {
            return getSuitableDay(DayOfWeek.TUESDAY);
        } else if (str.toLowerCase().contains("mittwoch")) {
            return getSuitableDay(DayOfWeek.WEDNESDAY);
        }else if (str.toLowerCase().contains("donnerstag")) {
            return getSuitableDay(DayOfWeek.THURSDAY);
        }else if (str.toLowerCase().contains("freitag")) {
            return getSuitableDay(DayOfWeek.FRIDAY);
        }else if (str.toLowerCase().contains("samstag")) {
            return getNextSaturday();
        }
        return getNextMonday();
    }

    public static LocalDate getSuitableDay(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now(ZoneId.of("Europe/Paris")).with(dayOfWeek);
        if (LocalDate.now().isAfter(date)) {
            return date;
        }
        return LocalDate.now(ZoneId.of("Europe/Paris")).with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    public static boolean isContainExactWord(String fullString, String partWord){
        String pattern = "\\b"+partWord+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(fullString);
        return m.find();
    }

    public static String makeAllLetterCapitalize(String replace) {
        StringBuilder builder = new StringBuilder();
        String[] s = replace.split("\\s");
        for (String value : s) {
            builder.append(StringUtils.capitalize(value + " "));
        }
        return builder.toString();
    }


    @Value("${main.bilder}")
    public void setFoder(String folder) {
        Utils.IMAGE_DESTINATION_FOLDER = folder;
    }

    @Value("${spring.web.resources.static-locations}")
    public void setImageDestinationFolder(String folder) {
        System.out.println(false);
    }
}
