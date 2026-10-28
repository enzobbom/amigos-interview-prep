package com.amigoscode.shortener;

public class Main {
    public static void main(String[] args) {
        UrlShortener urlShortener = new UrlShortener();

        String longUrl = "https://example.com/articles/how-to-learn-java";

        String shortCode = urlShortener.shorten(longUrl);
    }
}
