package de.angebot.main.services;

import com.fasterxml.jackson.databind.JsonNode;
import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.enities.offers.Offer;
import de.angebot.main.gathering.lidl.LidlApiClient;
import de.angebot.main.gathering.lidl.LidlHtmlParser;
import de.angebot.main.gathering.lidl.LidlImportResult;
import de.angebot.main.gathering.lidl.LidlLeafletClient;
import de.angebot.main.gathering.lidl.LidlLeafletDescriptor;
import de.angebot.main.gathering.lidl.LidlLeafletOffer;
import de.angebot.main.gathering.lidl.LidlLeafletParser;
import de.angebot.main.gathering.lidl.LidlRawOffer;
import de.angebot.main.repositories.discounters.LidlRepo;
import de.angebot.main.repositories.offers.OfferRepository;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Orchestriert den Lidl-Import aus Kampagnenseiten und digitalen Prospekten.
 */
@Slf4j
@Service
public class LidlImportService {

    static final String RETAILER = "lidl";

    private final LidlApiClient lidlApiClient;
    private final LidlHtmlParser lidlHtmlParser;
    private final LidlLeafletClient lidlLeafletClient;
    private final LidlLeafletParser lidlLeafletParser;
    private final OfferRepository offerRepository;
    private final LidlRepo lidlRepo;

    public LidlImportService(LidlApiClient lidlApiClient,
                             LidlHtmlParser lidlHtmlParser,
                             LidlLeafletClient lidlLeafletClient,
                             LidlLeafletParser lidlLeafletParser,
                             OfferRepository offerRepository,
                             LidlRepo lidlRepo) {
        this.lidlApiClient = lidlApiClient;
        this.lidlHtmlParser = lidlHtmlParser;
        this.lidlLeafletClient = lidlLeafletClient;
        this.lidlLeafletParser = lidlLeafletParser;
        this.offerRepository = offerRepository;
        this.lidlRepo = lidlRepo;
    }

    @Transactional
    public LidlImportResult importCurrentOffers() {
        long startMs = System.currentTimeMillis();
        log.info("Lidl import started (campaign + leaflet).");

        LidlImportResult campaignResult = importCampaignOffers();
        LidlImportResult leafletResult = importLeafletOffers();

        LidlImportResult merged = campaignResult.add(leafletResult);
        LidlImportResult result = LidlImportResult.builder()
                .parsed(merged.getParsed())
                .inserted(merged.getInserted())
                .updated(merged.getUpdated())
                .skipped(merged.getSkipped())
                .failed(merged.getFailed())
                .duplicates(merged.getDuplicates())
                .campaignsProcessed(merged.getCampaignsProcessed())
                .leafletsProcessed(merged.getLeafletsProcessed())
                .campaignParsed(merged.getCampaignParsed())
                .leafletParsed(merged.getLeafletParsed())
                .durationMs(System.currentTimeMillis() - startMs)
                .build();

        log.info("Lidl import finished in {} ms: campaignParsed={}, leafletParsed={}, "
                        + "inserted={}, updated={}, skipped={}, duplicates={}, failed={}, "
                        + "campaigns={}, leaflets={}",
                result.getDurationMs(), result.getCampaignParsed(), result.getLeafletParsed(),
                result.getInserted(), result.getUpdated(), result.getSkipped(),
                result.getDuplicates(), result.getFailed(),
                result.getCampaignsProcessed(), result.getLeafletsProcessed());
        return result;
    }

    private LidlImportResult importCampaignOffers() {
        String homepage = lidlApiClient.fetchHomepage();
        Set<String> campaignPaths = lidlApiClient.discoverCampaignUrls(homepage);

        if (campaignPaths.isEmpty()) {
            log.warn("Lidl campaign import: no campaign URLs discovered.");
            return LidlImportResult.builder().build();
        }

        Map<String, LidlRawOffer> uniqueOffers = new LinkedHashMap<>();
        int campaignsProcessed = 0;

        for (String campaignPath : campaignPaths) {
            String html = lidlApiClient.fetchCampaignPage(campaignPath);
            List<LidlRawOffer> parsed = lidlHtmlParser.parseCampaignPage(html, campaignPath);
            parsed.forEach(offer -> uniqueOffers.putIfAbsent(offer.getProductId(), offer));
            campaignsProcessed++;
        }

        SaveStats stats = saveOffers(uniqueOffers.values());
        log.info("Lidl campaign import: parsed={}, inserted={}, updated={}, skipped={}, duplicates={}, failed={}",
                uniqueOffers.size(), stats.inserted, stats.updated, stats.skipped, stats.duplicates, stats.failed);

        return LidlImportResult.builder()
                .parsed(uniqueOffers.size())
                .inserted(stats.inserted)
                .updated(stats.updated)
                .skipped(stats.skipped)
                .failed(stats.failed)
                .duplicates(stats.duplicates)
                .campaignsProcessed(campaignsProcessed)
                .campaignParsed(uniqueOffers.size())
                .build();
    }

    private LidlImportResult importLeafletOffers() {
        List<LidlLeafletDescriptor> flyers = lidlLeafletClient.discoverFlyers();
        if (flyers.isEmpty()) {
            log.warn("Lidl leaflet import: no flyers discovered.");
            return LidlImportResult.builder().build();
        }

        Map<String, LidlRawOffer> uniqueOffers = new LinkedHashMap<>();
        int leafletsProcessed = 0;

        for (LidlLeafletDescriptor descriptor : flyers) {
            JsonNode flyerResponse = lidlLeafletClient.fetchFlyer(descriptor.getFlyerIdentifier());
            List<LidlLeafletOffer> leafletOffers = lidlLeafletParser.parseFlyer(flyerResponse, descriptor);
            leafletOffers.stream()
                    .map(lidlLeafletParser::toRawOffer)
                    .forEach(offer -> uniqueOffers.putIfAbsent(offer.getProductId(), offer));
            leafletsProcessed++;
        }

        SaveStats stats = saveOffers(uniqueOffers.values());
        log.info("Lidl leaflet import: parsed={}, inserted={}, updated={}, skipped={}, duplicates={}, failed={}",
                uniqueOffers.size(), stats.inserted, stats.updated, stats.skipped, stats.duplicates, stats.failed);

        return LidlImportResult.builder()
                .parsed(uniqueOffers.size())
                .inserted(stats.inserted)
                .updated(stats.updated)
                .skipped(stats.skipped)
                .failed(stats.failed)
                .duplicates(stats.duplicates)
                .leafletsProcessed(leafletsProcessed)
                .leafletParsed(uniqueOffers.size())
                .build();
    }

    private SaveStats saveOffers(Iterable<LidlRawOffer> rawOffers) {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        int duplicates = 0;

        for (LidlRawOffer rawOffer : rawOffers) {
            try {
                SaveOutcome outcome = saveOffer(rawOffer);
                switch (outcome) {
                    case INSERTED -> inserted++;
                    case UPDATED -> updated++;
                    case SKIPPED -> skipped++;
                    case DUPLICATE -> {
                        skipped++;
                        duplicates++;
                    }
                }
            } catch (RuntimeException e) {
                failed++;
                log.error("Lidl offer {} could not be saved: {}", rawOffer.getProductId(), e.getMessage());
            }
        }

        return new SaveStats(inserted, updated, skipped, failed, duplicates);
    }

    /**
     * Saves the offer to the canonical {@code offers} table and additionally to the legacy
     * {@code lidl} table so that the existing {@code HomeController /home/lidl} endpoint
     * and the {@code SelectedItemsService} (which resolves items by legacy Long id) continue
     * to work without changes.
     *
     * <p><b>Technical debt:</b> The legacy discounter tables (lidl, penny, …) and the new
     * {@code offers} table store overlapping data.  The long-term migration path is:
     * <ol>
     *   <li>Change {@code HomeController#getLidl()} to query {@code OfferRepository} directly.</li>
     *   <li>Migrate {@code SelectedItem.itemId} (Long FK to legacy table) to reference
     *       {@code Offer.externalId} (String).</li>
     *   <li>Remove {@link #saveLegacyOffer(Offer)} once the migration is complete.</li>
     * </ol>
     */
    private SaveOutcome saveOffer(LidlRawOffer rawOffer) {
        Offer normalized = normalize(rawOffer);
        Optional<Offer> existing = offerRepository.findById(normalized.getExternalId());
        if (existing.isPresent()) {
            Offer current = existing.get();
            boolean duplicate = isCrossSourceDuplicate(current, normalized);
            boolean changed = duplicate ? mergeCrossSource(current, normalized) : mergeOffer(current, normalized);
            if (changed) {
                offerRepository.save(current);
            }
            saveLegacyOffer(current);
            if (duplicate) {
                return SaveOutcome.DUPLICATE;
            }
            return changed ? SaveOutcome.UPDATED : SaveOutcome.SKIPPED;
        }

        offerRepository.save(normalized);
        saveLegacyOffer(normalized);
        return SaveOutcome.INSERTED;
    }

    Offer normalize(LidlRawOffer rawOffer) {
        String externalId = buildExternalId(rawOffer);
        Offer offer = new Offer();
        offer.setExternalId(externalId);
        offer.setRetailer(RETAILER);
        offer.setTitle(rawOffer.getTitle());
        offer.setDescription(rawOffer.getDescription());
        offer.setCurrentPrice(rawOffer.getCurrentPrice());
        offer.setOriginalPrice(rawOffer.getOriginalPrice());
        offer.setCurrency("EUR");
        offer.setImageUrl(rawOffer.getImageUrl());
        offer.setValidFrom(rawOffer.getValidFrom());
        offer.setValidTo(rawOffer.getValidTo());
        offer.setCategory(rawOffer.getCategory());
        offer.setSourceUrl(resolveSourceUrl(rawOffer));
        offer.setBrand(rawOffer.getBrand());
        offer.setEan(rawOffer.getEan());
        offer.setDiscountPercent(rawOffer.getDiscountPercent());
        offer.setActionWeek(rawOffer.getActionWeek());
        offer.setStoreBranch(rawOffer.getStoreBranch());
        offer.setOfferSource(rawOffer.getOfferSource() != null ? rawOffer.getOfferSource().name() : null);
        offer.setLeafletId(rawOffer.getLeafletId());
        offer.setLeafletPage(rawOffer.getLeafletPage());
        offer.setRawJson(rawOffer.getRawJson());
        return offer;
    }

    String buildExternalId(LidlRawOffer rawOffer) {
        if (rawOffer.getValidFrom() != null) {
            return RETAILER + "-" + rawOffer.getProductId() + "-" + rawOffer.getValidFrom();
        }
        String fallbackSeed = firstNonBlank(rawOffer.getLeafletId(), rawOffer.getCanonicalPath(),
                rawOffer.getCampaignUrl());
        if (StringUtils.hasText(fallbackSeed)) {
            return RETAILER + "-" + rawOffer.getProductId() + "-" + Integer.toHexString(fallbackSeed.hashCode());
        }
        return RETAILER + "-" + rawOffer.getProductId();
    }

    private String resolveSourceUrl(LidlRawOffer rawOffer) {
        if (StringUtils.hasText(rawOffer.getCampaignUrl()) && rawOffer.getCampaignUrl().startsWith("http")) {
            return rawOffer.getCampaignUrl();
        }
        return lidlApiClient.toAbsoluteUrl(rawOffer.getCanonicalPath());
    }

    private boolean isCrossSourceDuplicate(Offer existing, Offer incoming) {
        return StringUtils.hasText(existing.getOfferSource())
                && StringUtils.hasText(incoming.getOfferSource())
                && !existing.getOfferSource().equals(incoming.getOfferSource());
    }

    private boolean mergeCrossSource(Offer target, Offer source) {
        boolean changed = false;
        changed |= appendOfferSource(target, source.getOfferSource());
        changed |= fillIfBlank(target.getTitle(), source.getTitle(), target::setTitle);
        changed |= fillIfBlank(target.getDescription(), source.getDescription(), target::setDescription);
        changed |= setIfNull(target.getCurrentPrice(), source.getCurrentPrice(), target::setCurrentPrice);
        changed |= setIfNull(target.getOriginalPrice(), source.getOriginalPrice(), target::setOriginalPrice);
        changed |= setIfNull(target.getDiscountPercent(), source.getDiscountPercent(), target::setDiscountPercent);
        changed |= fillIfBlank(target.getImageUrl(), source.getImageUrl(), target::setImageUrl);
        changed |= setIfNull(target.getValidFrom(), source.getValidFrom(), target::setValidFrom);
        changed |= setIfNull(target.getValidTo(), source.getValidTo(), target::setValidTo);
        changed |= fillIfBlank(target.getCategory(), source.getCategory(), target::setCategory);
        changed |= fillIfBlank(target.getSourceUrl(), source.getSourceUrl(), target::setSourceUrl);
        changed |= fillIfBlank(target.getBrand(), source.getBrand(), target::setBrand);
        changed |= fillIfBlank(target.getEan(), source.getEan(), target::setEan);
        changed |= fillIfBlank(target.getActionWeek(), source.getActionWeek(), target::setActionWeek);
        changed |= fillIfBlank(target.getStoreBranch(), source.getStoreBranch(), target::setStoreBranch);
        changed |= fillIfBlank(target.getRawJson(), source.getRawJson(), target::setRawJson);
        changed |= fillIfBlank(target.getLeafletId(), source.getLeafletId(), target::setLeafletId);
        changed |= setIfNull(target.getLeafletPage(), source.getLeafletPage(), target::setLeafletPage);
        return changed;
    }

    private boolean mergeOffer(Offer target, Offer source) {
        boolean changed = false;
        changed |= replaceText(target.getTitle(), source.getTitle(), target::setTitle);
        changed |= replaceText(target.getDescription(), source.getDescription(), target::setDescription);
        changed |= replaceIfDifferent(target.getCurrentPrice(), source.getCurrentPrice(), target::setCurrentPrice);
        changed |= replaceIfDifferent(target.getOriginalPrice(), source.getOriginalPrice(), target::setOriginalPrice);
        changed |= replaceIfDifferent(target.getDiscountPercent(), source.getDiscountPercent(), target::setDiscountPercent);
        changed |= replaceText(target.getImageUrl(), source.getImageUrl(), target::setImageUrl);
        changed |= replaceIfDifferent(target.getValidFrom(), source.getValidFrom(), target::setValidFrom);
        changed |= replaceIfDifferent(target.getValidTo(), source.getValidTo(), target::setValidTo);
        changed |= replaceText(target.getCategory(), source.getCategory(), target::setCategory);
        changed |= replaceText(target.getSourceUrl(), source.getSourceUrl(), target::setSourceUrl);
        changed |= replaceText(target.getBrand(), source.getBrand(), target::setBrand);
        changed |= replaceText(target.getEan(), source.getEan(), target::setEan);
        changed |= replaceText(target.getActionWeek(), source.getActionWeek(), target::setActionWeek);
        changed |= replaceText(target.getStoreBranch(), source.getStoreBranch(), target::setStoreBranch);
        changed |= replaceText(target.getRawJson(), source.getRawJson(), target::setRawJson);
        changed |= replaceText(target.getLeafletId(), source.getLeafletId(), target::setLeafletId);
        changed |= replaceIfDifferent(target.getLeafletPage(), source.getLeafletPage(), target::setLeafletPage);
        changed |= appendOfferSource(target, source.getOfferSource());
        return changed;
    }

    private void saveLegacyOffer(Offer offer) {
        String url = offer.getSourceUrl();
        if (!StringUtils.hasText(url)) {
            return;
        }
        Optional<Lidl> existing = lidlRepo.findFirstByUrl(url);
        Lidl lidl = existing.orElseGet(Lidl::new);
        if (StringUtils.hasText(offer.getTitle())) {
            lidl.setProduktName(offer.getTitle());
        }
        if (StringUtils.hasText(offer.getBrand())) {
            lidl.setProduktMaker(offer.getBrand());
        }
        if (offer.getCurrentPrice() != null) {
            lidl.setProduktPrise(toPriceString(offer.getCurrentPrice()));
        }
        if (offer.getOriginalPrice() != null) {
            lidl.setProduktRegularPrise(toPriceString(offer.getOriginalPrice()));
        }
        if (StringUtils.hasText(offer.getDescription())) {
            lidl.setProduktDescription(offer.getDescription());
        }
        String existingImageLink = lidl.getImageLink();
        if (!StringUtils.hasText(existingImageLink) || existingImageLink.startsWith("http")) {
            String imageLink = downloadLegacyImage(offer);
            if (StringUtils.hasText(imageLink)) {
                lidl.setImageLink(imageLink);
            }
        }
        if (offer.getValidFrom() != null) {
            lidl.setVonDate(offer.getValidFrom());
        }
        if (offer.getValidTo() != null) {
            lidl.setBisDate(offer.getValidTo());
        }
        if (StringUtils.hasText(offer.getCategory())) {
            lidl.setKategorie(offer.getCategory());
        }
        lidl.setUrl(url);
        lidlRepo.save(lidl);
    }

    private boolean appendOfferSource(Offer target, String source) {
        if (!StringUtils.hasText(source)) {
            return false;
        }
        String normalizedSource = source.trim();
        if (!StringUtils.hasText(target.getOfferSource())) {
            target.setOfferSource(normalizedSource);
            return true;
        }

        LinkedHashSet<String> sources = Arrays.stream(target.getOfferSource().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        boolean added = sources.add(normalizedSource);
        if (added) {
            target.setOfferSource(String.join(",", sources));
        }
        return added;
    }

    private boolean fillIfBlank(String currentValue, String newValue, Consumer<String> setter) {
        if (!StringUtils.hasText(newValue) || StringUtils.hasText(currentValue)) {
            return false;
        }
        setter.accept(newValue.trim());
        return true;
    }

    private <T> boolean setIfNull(T currentValue, T newValue, Consumer<T> setter) {
        if (newValue == null || currentValue != null) {
            return false;
        }
        setter.accept(newValue);
        return true;
    }

    private boolean replaceText(String currentValue, String newValue, Consumer<String> setter) {
        if (!StringUtils.hasText(newValue)) {
            return false;
        }
        String normalizedValue = newValue.trim();
        if (Objects.equals(currentValue, normalizedValue)) {
            return false;
        }
        setter.accept(normalizedValue);
        return true;
    }

    private <T> boolean replaceIfDifferent(T currentValue, T newValue, Consumer<T> setter) {
        if (newValue == null || Objects.equals(currentValue, newValue)) {
            return false;
        }
        setter.accept(newValue);
        return true;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String downloadLegacyImage(Offer offer) {
        if (!StringUtils.hasText(offer.getImageUrl())) {
            return "";
        }
        LocalDate endDate = offer.getValidTo() != null ? offer.getValidTo() : offer.getValidFrom();
        if (endDate == null) {
            return offer.getImageUrl();
        }
        return Utils.downloadImage(offer.getImageUrl(), "lidl", endDate, "");
    }

    private String toPriceString(BigDecimal price) {
        return price == null ? "" : price.toPlainString();
    }


    private enum SaveOutcome {
        INSERTED, UPDATED, SKIPPED, DUPLICATE
    }

    private record SaveStats(int inserted, int updated, int skipped, int failed, int duplicates) {
    }
}
