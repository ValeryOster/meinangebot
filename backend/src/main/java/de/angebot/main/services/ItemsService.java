package de.angebot.main.services;

import de.angebot.main.controller.json.Item;
import de.angebot.main.enities.selected.SelectedItem;
import de.angebot.main.repositories.discounters.*;
import de.angebot.main.repositories.selected.SelectedItemsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemsService {

    private final PennyRepo pennyRepo;
    private final AldiRepo aldiRepo;
    private final NettoRepo nettoRepo;
    private final EdekaRepo edekaRepo;
    private final SelectedItemsRepo itemsRepo;

    public ItemsService(AldiRepo aldiRepo, NettoRepo nettoRepo, PennyRepo pennyRepo,
                        EdekaRepo edekaRepo, SelectedItemsRepo itemsRepo) {
        this.aldiRepo = aldiRepo;
        this.nettoRepo = nettoRepo;
        this.pennyRepo = pennyRepo;
        this.edekaRepo = edekaRepo;
        this.itemsRepo = itemsRepo;
    }

    public List<Object> getAllSelectedItemForWeek(Long userId) {
        List<Object> itemsList = new ArrayList<>();
        for (SelectedItem item : itemsRepo.findCurrentOffersByUserId(userId)) {
            // Normalize: lowercase, remove trailing dot (e.g. "LIDL." → "lidl", "PENNY." → "penny")
            String name = item.getDiscounterName().toLowerCase().replace(".", "").trim();
            switch (name) {
                case "aldi":
                    aldiRepo.findById(item.getItemId()).ifPresent(itemsList::add);
                    break;
                case "netto":
                    nettoRepo.findById(item.getItemId()).ifPresent(itemsList::add);
                    break;
                case "penny":
                    pennyRepo.findById(item.getItemId()).ifPresent(itemsList::add);
                    break;
                case "edeka":
                    edekaRepo.findById(item.getItemId()).ifPresent(itemsList::add);
                    break;
                default:
                    log.warn("Unknown discounterName '{}' for SelectedItem id={}", item.getDiscounterName(), item.getId());
                    break;
            }
        }
        return itemsList;
    }

    /** Ersetzt die aktuelle Auswahl des Benutzers vollständig (alles oder nichts). */
    @Transactional
    public void saveSelectedItems(Long userId, List<Item> items) {
        itemsRepo.deleteAll(itemsRepo.findCurrentOffersByUserId(userId));
        if (items == null) {
            return;
        }
        for (Item item : items) {
            itemsRepo.save(toSelectedItem(item, userId));
        }
    }

    @Transactional
    public void deleteSelectedItems(Long userId, List<Item> items) {
        if (items == null) {
            return;
        }
        for (Item item : items) {
            itemsRepo.findSelectedItemByItemId(item.getId(), userId)
                    .ifPresentOrElse(
                            itemsRepo::delete,
                            () -> log.warn("SelectedItem not found for itemId={}, userId={}", item.getId(), userId));
        }
    }

    private SelectedItem toSelectedItem(Item item, Long userId) {
        SelectedItem selectedItem = new SelectedItem();
        selectedItem.setDiscounterName(item.getDiscounterName());
        selectedItem.setExpiryDate(item.getBisDate());
        selectedItem.setSaveDate(LocalDate.now());
        selectedItem.setItemId(item.getId());
        selectedItem.setUserId(userId);
        return selectedItem;
    }
}
