package de.angebot.main.controller.json;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Item {
    private String discounterName;
    private Long id;
    private String imageLink;
    private String kategorie;
    private String produktMaker;
    private String produktName;
    private String produktPrise;
    private String produktRegularPrise;
    private String url;
    private LocalDate bisDate;
    private LocalDate vonDate;
}
