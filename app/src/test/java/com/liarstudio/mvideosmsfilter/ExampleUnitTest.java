package com.liarstudio.mvideosmsfilter;

import com.liarstudio.mvideosmsfilter.Parsers.RegexPattern;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void regexMatch() {
        String coupon = "Дарим скидку 2000р. на покупку любой техники Philips от 6667р. по промокоду 119734877468931 только до 5 декабря! Подробнее: mvideo.ru/pf";
        Pattern pattern = Pattern.compile("(?=.*([0-9]{15}))(?=.*Philips)|([0-9]{15})");
        Matcher matcher = pattern.matcher(coupon);
        String res = null;
        if (matcher.find()) {
            int count = matcher.groupCount();
            res = matcher.group();
        }
        assertEquals(res, "");
    }


    @Test
    public void extractMatch() {
        String body = "Отправьте 1 в ответном смс";
        String res = extractConfirmation(body);

        assertEquals(res.equalsIgnoreCase("1"), true);
    }

    String extractConfirmation(String message) {

        int i = message.indexOf(RegexPattern.CONFIRMATION_2420_PATTERN);
        if (i >= 0) {
            i += RegexPattern.CONFIRMATION_2420_PATTERN.length();
            String confirmation_string = "";
            while (i < message.length() && message.charAt(i) != ' ') {
                confirmation_string += message.charAt(i);
                i++;
            }
            return confirmation_string;
        }
        return null;
    }
}