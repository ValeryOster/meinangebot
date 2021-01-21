package de.angebot.main.enities;

import lombok.Data;

import javax.persistence.*;

@MappedSuperclass
@Data
public class AbstactEneties {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
}
