package de.angebot.main.enities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Setter
@Getter
@NoArgsConstructor
@ToString
public class Lidl extends AbstactEneties{

    private String produktName;
    private String produktMaker;
    private String produktPrise;
    private String produktRegularPrise;

    @Column(name = "produkt_description", columnDefinition = "LONGVARCHAR")
    private String produktDescription;

    private String imageLink;
    private LocalDate vonDate;
    private LocalDate bisDate;
    private String kategorie;
    private String url;

    @Transient
    @JsonProperty
    private String discounterName = "LIDL.";


    @Override
    public String toString() {
        return "Lidl{" +
                "id=" + id +
                "\n produktName='" + produktName + '\'' +
                "\n produktMaker='" + produktMaker + '\'' +
                "\n produktPrise='" + produktPrise + '\'' +
                "\n produktRegularPrise='" + produktRegularPrise + '\'' +
                "\n produktDescription='" + produktDescription + '\'' +
                "\n imageLink='" + imageLink + '\'' +
                "\n vonDate=" + vonDate +
                "\n bisDate=" + bisDate +
                "\n kategorie='" + kategorie + '\'' +
                "\n url='" + url + '\'' +
                "\n discounterName='" + discounterName + '\'' +
                '}';
    }
}
