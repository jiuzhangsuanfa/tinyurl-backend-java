package com.jiuzhang.url.tinyurl;

import java.util.Random;

public class RandomTinyUrl {

    private static final String SOURCE = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";

    public static String generate(int length) {

        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(SOURCE.length());
            sb.append(SOURCE.charAt(number));
        }

        return sb.toString();
    }

}
