package com.liarstudio.mvideosmsfilter;

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
}