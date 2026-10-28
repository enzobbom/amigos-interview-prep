package com.amigoscode.shortener;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class UrlShortener {
    private static final String BASE62 =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int SHORT_CODE_LENGTH = 8;

    private static final BigInteger BASE62_LIMIT =
            BigInteger.valueOf(62).pow(SHORT_CODE_LENGTH);

    private final Map<String, String> urlsMap = new HashMap<>();

    public String shorten(String longUrl) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid url");
        }

        String shortUrlCode;

        do {
            shortUrlCode = encodeLongUrl(longUrl);
        } while (urlsMap.containsKey(shortUrlCode));

        urlsMap.put(shortUrlCode, longUrl);

        return shortUrlCode;
    }

    public Optional<String> expand(String shortUrl) {
        if (!isValidShortCode(shortUrl)) {
            throw new IllegalArgumentException("Invalid short code");
        }

        return Optional.ofNullable(urlsMap.get(shortUrl));
    }

    // validation

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(url);

            return ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidShortCode(String shortCode) {
        return shortCode != null
                && shortCode.matches("[a-zA-Z0-9]{8}");
    }

    // encoding

    private String encodeLongUrl(String longUrl) {
        String input = Instant.now()
                + ":"
                + UUID.randomUUID()
                + ":"
                + longUrl;

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            byte[] digest = sha256.digest(
                    input.getBytes(StandardCharsets.UTF_8));

            BigInteger hash = new BigInteger(1, digest);
            BigInteger number = hash.mod(BASE62_LIMIT);

            String encoded = encodeBase62(number);

            return "0".repeat(SHORT_CODE_LENGTH - encoded.length())
                    + encoded;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String encodeBase62(BigInteger number) {
        if (number.equals(BigInteger.ZERO)) {
            return "0";
        }

        StringBuilder result = new StringBuilder();

        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] division =
                    number.divideAndRemainder(BigInteger.valueOf(62));

            result.append(BASE62.charAt(division[1].intValue()));
            number = division[0];
        }

        return result.reverse().toString();
    }
}
