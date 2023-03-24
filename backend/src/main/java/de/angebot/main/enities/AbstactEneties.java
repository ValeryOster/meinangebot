package de.angebot.main.enities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public class AbstactEneties {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
}
