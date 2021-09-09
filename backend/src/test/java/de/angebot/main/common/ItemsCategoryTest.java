package de.angebot.main.common;


import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ItemsCategoryTest {
//    @Test
    void getKategorie() {
        List<String> collect = Stream.of(ItemsCategory.values())
                .map(ItemsCategory::getKategorie)
                .collect(Collectors.toList());

        for (String a : collect) {
            System.out.println(a);
        }
    }

//   @Test
    void testAllINeed() {
       System.out.println(LocalDate.now(ZoneId.of("Europe/Paris"))
               .with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY)));

   }
}
