package de.angebot.main.controller.json;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JsonSelectedItemsListAndUserId {
    private List<Item> auswahlListe;
    /** Wird vom Server ignoriert – der Benutzer kommt aus dem JWT. Nur noch aus Kompatibilitätsgründen vorhanden. */
    private Long userId;
}
