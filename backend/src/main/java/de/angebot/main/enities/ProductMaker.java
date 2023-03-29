package de.angebot.main.enities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"makerName"})})
public class ProductMaker extends AbstactEneties{

    @Column(nullable = false, unique = true )
    private String makerName;

    private Boolean valid;
}
