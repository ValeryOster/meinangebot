package de.angebot.main.common;



import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ItemsCategoryTest {
    @Test
    void getKategorie() {
        List<String> collect = Stream.of(ItemsCategory.values())
                .map(ItemsCategory::getKategorie)
                .collect(Collectors.toList());

        for (String a : collect) {
            System.out.println(a);
        }
    }

    @Test
    void testAllINeed() {
        AtomicLong al = new AtomicLong();
        al.set(-1L);
        System.out.println(al.get());
    }
}
