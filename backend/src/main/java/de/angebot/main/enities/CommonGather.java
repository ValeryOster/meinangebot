package de.angebot.main.enities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Data
public class CommonGather {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate gatherDate;

    @ElementCollection
    Map<String, Boolean> dicount;
}
