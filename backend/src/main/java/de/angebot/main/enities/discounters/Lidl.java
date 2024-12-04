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
import java.util.Objects;

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
    private LocalDate vonDate;
    private LocalDate bisDate;
    private String kategorie;
    private String url;

    @Transient
    @JsonProperty
    private String discounterName = "LIDL";
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Lidl other = (Lidl) obj;
        return Objects.equals(this.url, other.url);
    }

    @Override
    public int hashCode() {
        return 31 * url.hashCode() + discounterName.hashCode();
    }
}
