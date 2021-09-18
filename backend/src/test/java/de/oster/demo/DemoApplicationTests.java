package de.oster.demo;

import de.angebot.main.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DemoApplicationTests {
    public static void main(String[] args) {
        int axe = "Schweine-Hinterhaxe".toLowerCase().indexOf("axe");
        System.out.println(axe);

        System.out.println(isContainExactWord("Schweine-Hinterhaxe Axel".toLowerCase(), "axe"));
    }

   private void test(){
       List<String> list = Arrays.asList("montag", "dienstag", "mittwoch", "donnerstag", "freitag", "samstag");
       for (String str : list) {
           System.out.println(Utils.getDateFromString(str));
       }
    }

    private static boolean isContainExactWord(String fullString, String partWord){
        String pattern = "\\b"+partWord+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(fullString);
        return m.find();
    }
}

