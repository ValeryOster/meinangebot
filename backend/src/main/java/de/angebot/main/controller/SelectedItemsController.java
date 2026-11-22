package de.angebot.main.controller;

import de.angebot.main.controller.json.JsonSelectedItemsListAndUserId;
import de.angebot.main.security.services.UserDetailsImpl;
import de.angebot.main.services.ItemsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Auswahlliste ("Merkzettel") des angemeldeten Benutzers.
 * <p>
 * Der Benutzer wird immer aus dem JWT bestimmt. Ein im Request mitgeschicktes {@code userId}
 * wird ignoriert, damit niemand die Liste eines anderen Benutzers lesen oder ändern kann.
 */
@RestController
@RequestMapping("/selected")
public class SelectedItemsController {

    private final ItemsService itemsService;

    public SelectedItemsController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @GetMapping("/items")
    public List<Object> getSelectedItems(@AuthenticationPrincipal UserDetailsImpl user) {
        return itemsService.getAllSelectedItemForWeek(user.getId());
    }

    /** Alte Variante (POST mit userId im Body) – der Body wird nicht mehr ausgewertet. */
    @PostMapping("/items")
    public List<Object> getSelectedItemsLegacy(@AuthenticationPrincipal UserDetailsImpl user) {
        return getSelectedItems(user);
    }

    @PostMapping("/delete")
    public void deleteSelectedItem(@AuthenticationPrincipal UserDetailsImpl user,
                                   @RequestBody JsonSelectedItemsListAndUserId items) {
        itemsService.deleteSelectedItems(user.getId(), items.getAuswahlListe());
    }

    @PostMapping("/save")
    public boolean saveSelectedItem(@AuthenticationPrincipal UserDetailsImpl user,
                                    @RequestBody JsonSelectedItemsListAndUserId items) {
        itemsService.saveSelectedItems(user.getId(), items.getAuswahlListe());
        return true;
    }
}
