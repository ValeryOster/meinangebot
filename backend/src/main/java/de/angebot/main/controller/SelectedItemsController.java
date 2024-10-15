package de.angebot.main.controller;

import de.angebot.main.controller.json.JsonSelectedItemsListAndUserId;
import de.angebot.main.services.ItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/selected")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SelectedItemsController {

    private final ItemsService itemsService;

    @Autowired
    public SelectedItemsController(ItemsService itemsService) {this.itemsService = itemsService;}

    @PostMapping(path = "/itmes")
    public List<Object> getAllSelectedItems(@RequestBody Long userId) {
        if (userId > 0) {
            return itemsService.getAllSelectedItemForWeek(userId);
        }
        return Collections.emptyList();
    }

    @PostMapping(path = "/delete")
    public void deleteSelectedItem(@RequestBody JsonSelectedItemsListAndUserId item) {
        itemsService.deleteSelectedItem(item);
    }

    @PostMapping(path = "/save")
    public Boolean saveSelectedItem(@RequestBody JsonSelectedItemsListAndUserId items) {
        return itemsService.saveSelectedItems(items);
    }
}
