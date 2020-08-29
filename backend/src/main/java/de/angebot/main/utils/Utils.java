package de.angebot.main.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Utils {

    private static final String IMAGE_DESTINATION_FOLDER = "C:/Users/Valera/IdeaProjects/angebote/src/main" +
            "/resources/bilder";

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

        for (int i = 0; i < myAr.length; i++) {
            if (StringUtils.isAllUpperCase(myAr[i])) {
                maker.append(" " + myAr[i]);
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

            String path = IMAGE_DESTINATION_FOLDER + "/" + storageName + "/" + endDate;
            //Create Directory if not exists
            String bild = Files.createDirectories(Paths.get(path)).toString() + "/" + strImageName;
            OutputStream os = new FileOutputStream(bild);

            //write bytes to the output stream
            while ((n = in.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            os.close();
            return path;
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

}
