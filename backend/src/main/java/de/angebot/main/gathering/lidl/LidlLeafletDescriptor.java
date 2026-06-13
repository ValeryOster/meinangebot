package de.angebot.main.gathering.lidl;

import lombok.Builder;
import lombok.Value;

/**
 * Metadaten eines Lidl-Prospekts aus der Schwarz-Leaflets-Overview-API.
 */
@Value
@Builder
public class LidlLeafletDescriptor {

    String flyerId;
    String flyerIdentifier;
    String name;
    String title;
    String category;
    String subcategory;
    String status;
    boolean active;
    String flyerJsonUrl;
}
