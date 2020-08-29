package de.angebot.main.enities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Setter@Getter@NoArgsConstructor
public class Penny {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String produktName;
    private String produktMaker;
    private String produktPrise;
    private String produktRegularPrise;
    private String imageLink;
    private LocalDate vonDate;
    private LocalDate bisDate;

}
