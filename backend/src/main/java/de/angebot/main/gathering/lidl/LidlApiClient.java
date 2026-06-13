package de.angebot.main.gathering.lidl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lädt HTML-Seiten von lidl.de per HTTP.
 * Lidl stellt keine öffentliche REST-API für Filialangebote bereit;
 * Produktdaten liegen als eingebettetes JSON in Kampagnenseiten (SSR).
 */
@Slf4j
@Component
public class LidlApiClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final Pattern CAMPAIGN_PATH = Pattern.compile("href=\"(/c/[^\"]+/a\\d+)\"");

    private final HttpClient httpClient;
    private final String mainUrl;
    private final String userAgent;

    public LidlApiClient(@Value("${lidl.mainUrl:https://www.lidl.de}") String mainUrl,
                         @Value("${lidl.userAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                 + "(KHTML, like Gecko) Chrome/125.0 Safari/537.36}") String userAgent) {
        this.mainUrl = mainUrl.endsWith("/") ? mainUrl.substring(0, mainUrl.length() - 1) : mainUrl;
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String fetchHomepage() {
        return fetchHtml(mainUrl);
    }

    public String fetchCampaignPage(String campaignPath) {
        String path = campaignPath.startsWith("/") ? campaignPath : "/" + campaignPath;
        return fetchHtml(mainUrl + path);
    }

    public Set<String> discoverCampaignUrls(String homepageHtml) {
        Set<String> urls = new LinkedHashSet<>();
        if (!StringUtils.hasText(homepageHtml)) {
            return urls;
        }
        Matcher matcher = CAMPAIGN_PATH.matcher(homepageHtml);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        log.info("Lidl campaign discovery found {} campaign URLs.", urls.size());
        return urls;
    }

    public String toAbsoluteUrl(String path) {
        if (!StringUtils.hasText(path)) {
            return mainUrl;
        }
        if (path.startsWith("http")) {
            return path;
        }
        return mainUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private String fetchHtml(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.7")
                .GET()
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }
                log.error("Lidl HTML request failed for {} with status {} (attempt {}/{})",
                        url, response.statusCode(), attempt, MAX_ATTEMPTS);
            } catch (IOException e) {
                log.error("Lidl HTML request failed for {} (attempt {}/{}): {}",
                        url, attempt, MAX_ATTEMPTS, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Lidl HTML request interrupted for {}", url);
                return "";
            }
        }
        return "";
    }
}
