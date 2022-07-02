package de.angebot.main.enities.selected;

import de.angebot.main.enities.AbstactEneties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedItem extends AbstactEneties {

    @Column(nullable = false, unique = true )
    @NotNull(message = "Name may not be null")
    private Long itemId;

    @NotEmpty(message = "Name may not be empty")
    private String discounterName;

    @NotNull(message = "Name may not be null")
    private Long userId;

    private LocalDate saveDate;
    private LocalDate expiryDate;
}
