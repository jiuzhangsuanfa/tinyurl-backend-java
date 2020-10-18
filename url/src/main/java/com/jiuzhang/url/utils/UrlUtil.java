package com.jiuzhang.url.utils;

/**
 * @auther: WZ
 * @Date: 2020/9/26 12:43
 * @Description:
 */
public class UrlUtil {

    public static boolean isLongUrl(String url) {
        return !url.startsWith("http://localhost") && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static boolean isShortUrl(String url) {
        return url.startsWith("http://localhost:8080");
    }
}
