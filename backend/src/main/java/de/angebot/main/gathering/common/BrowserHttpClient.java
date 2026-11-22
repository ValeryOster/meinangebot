package de.angebot.main.gathering.common;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * HTTP-Client, der sich wie ein Desktop-Chrome verhält (HTTP/2 und die typischen Browser-Header).
 * <p>
 * Seiten hinter einem Bot-Schutz (z. B. Akamai bei Netto) lehnen einfache Anfragen per HTTP/1.1
 * mit HTTP 403 ab, akzeptieren aber HTTP/2 mit vollständigen Browser-Headern.
 */
@Component
public class BrowserHttpClient {

    static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final String ACCEPT_HTML =
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8";
    private static final String ACCEPT_IMAGE = "image/avif,image/webp,image/apng,image/*,*/*;q=0.8";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** Lädt eine HTML-Seite. */
    public String getHtml(String url, Map<String, String> cookies) {
        HttpRequest request = baseRequest(url, cookies)
                .header("Accept", ACCEPT_HTML)
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .build();
        return new String(send(request), StandardCharsets.UTF_8);
    }

    /** Lädt eine Datei (z. B. ein Produktbild). */
    public byte[] getBytes(String url, Map<String, String> cookies) {
        HttpRequest request = baseRequest(url, cookies)
                .header("Accept", ACCEPT_IMAGE)
                .header("Sec-Fetch-Dest", "image")
                .header("Sec-Fetch-Mode", "no-cors")
                .header("Sec-Fetch-Site", "same-origin")
                .build();
        return send(request);
    }

    private HttpRequest.Builder baseRequest(String url, Map<String, String> cookies) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "de-DE,de;q=0.9")
                .header("Accept-Encoding", "gzip, deflate")
                .GET();
        if (cookies != null && !cookies.isEmpty()) {
            builder.header("Cookie", cookies.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("; ")));
        }
        return builder;
    }

    private byte[] send(HttpRequest request) {
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new GatheringException(request.uri() + " nicht erreichbar: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatheringException("Abgebrochen: " + request.uri(), e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new GatheringException(request.uri() + " antwortet mit HTTP " + response.statusCode()
                    + (response.statusCode() == 403 ? " (Bot-Schutz)" : ""));
        }
        return decode(response);
    }

    private static byte[] decode(HttpResponse<byte[]> response) {
        String encoding = response.headers().firstValue("Content-Encoding").orElse("").toLowerCase(Locale.ROOT);
        byte[] body = response.body();
        try {
            if (encoding.contains("gzip")) {
                return readAll(new GZIPInputStream(new ByteArrayInputStream(body)));
            }
            if (encoding.contains("deflate")) {
                return readAll(new InflaterInputStream(new ByteArrayInputStream(body)));
            }
            return body;
        } catch (IOException e) {
            throw new GatheringException("Antwort von " + response.uri() + " konnte nicht entpackt werden ("
                    + encoding + "): " + e.getMessage(), e);
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        try (in) {
            return in.readAllBytes();
        }
    }
}
