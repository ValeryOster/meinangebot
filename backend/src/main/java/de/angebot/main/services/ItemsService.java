package de.angebot.main.services;

import de.angebot.main.controller.json.Item;
import de.angebot.main.controller.json.JsonSelectedItemsListAndUserId;
import de.angebot.main.enities.selected.SelectedItem;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.repositories.discounters.LidlRepo;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.repositories.selected.SelectedItemsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemsService {

    private final PennyRepo pennyRepo;
    private final LidlRepo lidlRepo;
    private final AldiRepo aldiRepo;
    private final NettoRepo nettoRepo;
    private final SelectedItemsRepo itemsRepo;

    @Autowired
    public ItemsService(AldiRepo aldiRepo, NettoRepo nettoRepo, LidlRepo lidlRepo, PennyRepo pennyRepo,
                        SelectedItemsRepo itemsRepo) {
        this.aldiRepo = aldiRepo;
        this.nettoRepo = nettoRepo;
        this.lidlRepo = lidlRepo;
        this.pennyRepo = pennyRepo;
        this.itemsRepo = itemsRepo;
    }

    public List<Object> getAllSelectedItemForWeek(Long userId) {
        List<Object> itemsList = new ArrayList<>();
        for (SelectedItem item : itemsRepo.findCurrentOffersByUserId(userId)) {
            switch (item.getDiscounterName().toLowerCase()) {
                case "aldi":
                    itemsList.add(aldiRepo.findById(item.getItemId()).get());
                    break;
                case "lidl.":
                    itemsList.add(lidlRepo.findById(item.getItemId()).get());
                    break;
                case "netto":
                    itemsList.add(nettoRepo.findById(item.getItemId()).get());
                    break;
                case "penny.":
                    itemsList.add(pennyRepo.findById(item.getItemId()).get());
                    break;

                default:
                    break;
            }
        }
        return itemsList;
    }

    public void saveSelectedItems(JsonSelectedItemsListAndUserId selectedItems) {
        for (Item item : selectedItems.getAuswahlListe()) {
            SelectedItem selectedItem = mapJsonSelectedItemToSelectedItem(item, selectedItems);
            try {
                itemsRepo.save(selectedItem);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }

    private SelectedItem mapJsonSelectedItemToSelectedItem(Item item, JsonSelectedItemsListAndUserId selectedItems) {
        SelectedItem selectedItem = new SelectedItem();
        new SelectedItem();

        selectedItem.setDiscounterName(item.getDiscounterName());
        selectedItem.setExpiryDate(item.getBisDate());
        selectedItem.setSaveDate(LocalDate.now());
        selectedItem.setItemId(item.getId());
        selectedItem.setUserId(selectedItems.getUserId());
        return selectedItem;
    }

    public void deleteSelectedItem(JsonSelectedItemsListAndUserId selectedItems) {
        for (Item item : selectedItems.getAuswahlListe()) {
            SelectedItem deleteItem = itemsRepo.findSelectedItemByItemId(item.getId(), selectedItems.getUserId());
            try {
                itemsRepo.delete(deleteItem);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
