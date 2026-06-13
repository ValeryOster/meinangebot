package de.angebot.main.gathering.lidl;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LidlImportResult {

    int parsed;
    int inserted;
    int updated;
    int skipped;
    int failed;
    int duplicates;
    int campaignsProcessed;
    int leafletsProcessed;
    int campaignParsed;
    int leafletParsed;
    long durationMs;

    public static LidlImportResult empty() {
        return LidlImportResult.builder().build();
    }

    public LidlImportResult add(LidlImportResult other) {
        return LidlImportResult.builder()
                .parsed(parsed + other.parsed)
                .inserted(inserted + other.inserted)
                .updated(updated + other.updated)
                .skipped(skipped + other.skipped)
                .failed(failed + other.failed)
                .duplicates(duplicates + other.duplicates)
                .campaignsProcessed(campaignsProcessed + other.campaignsProcessed)
                .leafletsProcessed(leafletsProcessed + other.leafletsProcessed)
                .campaignParsed(campaignParsed + other.campaignParsed)
                .leafletParsed(leafletParsed + other.leafletParsed)
                .durationMs(durationMs + other.durationMs)
                .build();
    }
}
