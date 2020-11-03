package com.jiuzhang.url.utils;

public class UrlUtil {

    public static boolean isValidLongUrl(String url) {
        return !url.startsWith("http://localhost") && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static boolean isValidShortUrl(String url) {
        return url.startsWith("http://localhost:8080");
    }
}
