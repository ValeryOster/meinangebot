package de.angebot.main.enities.discounters;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.angebot.main.enities.AbstactEneties;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Globus extends AbstactEneties {

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
    private String discounterName = "Globus";
}

