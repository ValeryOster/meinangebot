package de.angebot.main.enities.user;

import de.angebot.main.enities.AbstactEneties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Userinfo extends AbstactEneties {
    private String firstName;
    private String lastName;
    private String timeZone;
    private LocalDate singupDate;
    private LocalDate lastSinginDate;
}
