package de.angebot.main.services;

import de.angebot.main.controller.json.Item;
import de.angebot.main.enities.selected.SelectedItem;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.repositories.discounters.EdekaRepo;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.repositories.selected.SelectedItemsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Die Auswahlliste wird immer für den übergebenen (angemeldeten) Benutzer gespeichert bzw. gelöscht. */
class ItemsServiceTest {

    private SelectedItemsRepo itemsRepo;
    private ItemsService service;

    @BeforeEach
    void setUp() {
        itemsRepo = mock(SelectedItemsRepo.class);
        service = new ItemsService(mock(AldiRepo.class), mock(NettoRepo.class), mock(PennyRepo.class),
                mock(EdekaRepo.class), itemsRepo);
    }

    @Test
    void saveReplacesOnlyTheListOfTheGivenUser() {
        SelectedItem old = new SelectedItem();
        when(itemsRepo.findCurrentOffersByUserId(7L)).thenReturn(List.of(old));

        service.saveSelectedItems(7L, List.of(item(42L, "Penny"), item(43L, "Netto")));

        verify(itemsRepo).deleteAll(List.of(old));
        ArgumentCaptor<SelectedItem> saved = ArgumentCaptor.forClass(SelectedItem.class);
        verify(itemsRepo, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(SelectedItem::getUserId).containsOnly(7L);
        assertThat(saved.getAllValues()).extracting(SelectedItem::getItemId).containsExactly(42L, 43L);
        assertThat(saved.getAllValues()).extracting(SelectedItem::getExpiryDate).containsOnly(LocalDate.of(2030, 1, 1));
    }

    @Test
    void saveWithoutItemsOnlyClearsTheList() {
        when(itemsRepo.findCurrentOffersByUserId(7L)).thenReturn(List.of());

        service.saveSelectedItems(7L, null);

        verify(itemsRepo).deleteAll(List.of());
        verify(itemsRepo, never()).save(any());
    }

    @Test
    void deleteLooksUpItemsOnlyForTheGivenUser() {
        SelectedItem own = new SelectedItem();
        when(itemsRepo.findSelectedItemByItemId(42L, 7L)).thenReturn(Optional.of(own));

        service.deleteSelectedItems(7L, List.of(item(42L, "Penny")));

        verify(itemsRepo).findSelectedItemByItemId(42L, 7L);
        verify(itemsRepo).delete(own);
    }

    private static Item item(Long id, String discounter) {
        Item item = new Item();
        item.setId(id);
        item.setDiscounterName(discounter);
        item.setBisDate(LocalDate.of(2030, 1, 1));
        return item;
    }
}
