package com.liarstudio.mvideosmsfilter;

import android.content.ContentResolver;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    final Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    final static String ELDO_NAME = "ELDORADO";
    final static String MVIDEO_NAME = "M.Video";

    final static String ELDO_SUM_PATTERN = "скидка - [0-9]{1,2} 000";
    final static String MVIDEO_SUM_PATTERN = "скидку [0-9]{1,2} 000";

    final static String ELDO_COUPON_PATTERN = "[0-9]{13}";
    final static String MVIDEO_COUPON_PATTERN = "[0-9]{15}";

    private Context context;


    public Parser(Context context) {
        this.context = context;
    }

    public List<String> parse(Shop shopToParse, SortOrder order) {


        String shopName = shopToParse == Shop.MVIDEO ?
                MVIDEO_NAME : ELDO_NAME;
        String[] columnNames = {"_id", "body", "address", "date"};
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(SMS_INBOX, columnNames, "address='" + shopName + "'", null, "date desc");//, null, null);

        Pattern patternSum = Pattern.compile(shopToParse == Shop.MVIDEO ?
                                                MVIDEO_SUM_PATTERN : ELDO_SUM_PATTERN);
        Pattern patternCode = Pattern.compile(shopToParse == Shop.MVIDEO ?
                MVIDEO_COUPON_PATTERN : ELDO_COUPON_PATTERN);

        Matcher matcherSum;
        Matcher matcherCode;


        List<String> coupons = new ArrayList<>();
        while (cursor.moveToNext()) {
            String current = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            matcherSum = patternSum.matcher(current);
            matcherCode = patternCode.matcher(current);
            if (matcherSum.find() && matcherCode.find())
                coupons.add(calculatePrice(matcherSum.group()) + ": " + matcherCode.group() + "\r\n");
        }
        if (!coupons.isEmpty() && order == SortOrder.SUM)
            return sortBySum(coupons);
        return coupons;
    }


    public List<String> sortBySum(List<String> coupons) {

        for (int i = 0; i < coupons.size(); i++) {
            for (int j = i + 1; j < coupons.size(); j++)
                if (extractPrice(coupons.get(i)) > extractPrice(coupons.get(j))) {
                    String tmp = coupons.get(i);
                    coupons.set(i, coupons.get(j));
                    coupons.set(j, tmp);
                }}

        return coupons;
    }


    private Integer calculatePrice(String coupon)
    {
        Pattern patternPrice = Pattern.compile("[0-9]{1,2} 0{3}");
        Matcher matcherPrice = patternPrice.matcher(coupon);
        matcherPrice.find();

        String correct_number = matcherPrice.group().replace(" ", "");
        return Integer.parseInt(correct_number);
    }

    private Integer extractPrice(String coupon) {
        String number = coupon.split(":")[0];
        return Integer.parseInt(number);
    }



}
