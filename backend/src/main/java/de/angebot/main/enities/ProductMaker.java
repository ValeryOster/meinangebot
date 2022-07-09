package de.angebot.main.enities;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"makerName"})})
public class ProductMaker extends AbstactEneties{

    @Column(nullable = false, unique = true )
    private String makerName;

    private Boolean valid;
}
