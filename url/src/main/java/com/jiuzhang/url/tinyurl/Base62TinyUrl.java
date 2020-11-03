package com.jiuzhang.url.tinyurl;

public class Base62TinyUrl {

    private static final String SOURCE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static int toBase62(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 10;
        }

        return c - 'A' + 36;
    }

    public static long tinyUrlToId(String shortUrl) {
        long id = 0;
        for (int i = 0; i < shortUrl.length(); ++i) {
            id = id * 62 + toBase62(shortUrl.charAt(i));
        }

        return id;
    }

    public static  String generate(long id, int length) {
        String short_url = "";
        int index = -1;

        while (id > 0) {
            index = (int) id % 62;
            short_url = SOURCE.charAt(index) + short_url;
            id = id / 62;
        }

        while (short_url.length() < length) {
            short_url = "0" + short_url;
        }

        return short_url;
    }

}
