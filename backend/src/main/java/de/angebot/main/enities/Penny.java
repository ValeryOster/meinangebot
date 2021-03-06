package de.angebot.main.enities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Setter@Getter@NoArgsConstructor
public class Penny extends AbstactEneties {

    private String produktName;
    private String produktMaker;
    private String produktPrise;
    private String produktRegularPrise;
    private String imageLink;
    private LocalDate vonDate;
    private LocalDate bisDate;
    private String kategorie;
    private String url;

    @Transient
    @JsonProperty
    private String discounterName = "PENNY.";


}
