package de.angebot.main.repositories.selected;

import de.angebot.main.enities.selected.SelectedItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectedItemsRepo extends CrudRepository<SelectedItem, Long> {

    @Query(value = "SELECT a FROM SelectedItem a WHERE a.expiryDate >= CURRENT_DATE and a.userId= :user")
    List<SelectedItem> findCurrentOffersByUserId(@Param("user") Long userId);


    @Query(value = "SELECT a FROM SelectedItem a WHERE a.expiryDate >= CURRENT_DATE and a.userId= :user and " +
            "a.itemId= :itemId")
    SelectedItem findSelectedItemByItemId(@Param("itemId") Long itemId, @Param("user") Long userId);
}
