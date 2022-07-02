package de.angebot.main.controller.json;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JsonSelectedItemsListAndUserId {
    private List<Item> auswahlListe;
    private Long userId;
}
