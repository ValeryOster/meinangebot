package de.angebot.main.enities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Aldi extends AbstactEneties {

    private String produktName;
    private String produktMaker;
    private String produktPrise;
    private String produktRegularPrise;
    private String produktDescription;
    private String imageLink;
    private LocalDate vonDate;
    private LocalDate bisDate;
    private String kategorie;
    private String url;

    @Transient
    @JsonProperty
    private String discounterName = "Aldi-Nord";
}
