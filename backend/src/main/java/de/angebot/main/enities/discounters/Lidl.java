package de.angebot.main.enities.discounters;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.angebot.main.enities.AbstactEneties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
public class Lidl extends AbstactEneties {

    private String produktName;
    private String produktMaker;
    private String produktPrise;
    private String produktRegularPrise;

    @Column(name = "produkt_description")
    private String produktDescription;

    private String imageLink;
    private String imageMobileLink;
    private LocalDate vonDate;
    private LocalDate bisDate;
    private String kategorie;
    private String url;

    @Transient
    @JsonProperty
    private String discounterName = "LIDL.";
}
