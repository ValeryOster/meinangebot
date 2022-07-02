package de.angebot.main.enities.services;

import de.angebot.main.enities.AbstactEneties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Map;


@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonGather extends AbstactEneties {

    private LocalDate gatherDate;
    private Long duration;
    private LocalDate expiryDate;
    @ElementCollection
    Map<String, String> discount;
}
