package com.liarstudio.mvideosmsfilter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidParser extends Parser {

    final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    final static String SMS_NAME = "M.Video";

    private Context context;

    public AndroidParser(Context context) {
        this.context = context;
    }

/*    @Override
    List<String> parseTemplate(String code) {

        Pattern pattern = Pattern.compile(code);

        List<String> coupons = new ArrayList<>();

        String[] columnNames = {"_id", "body", "address", "date"};
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(SMS_INBOX, columnNames, "address='" + SMS_NAME + "'", null, "date asc");//, null, null);


        while (cursor.moveToNext()) {
            String current = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            Matcher matcherCode = pattern.matcher(current);
            if (matcherCode.find())
                coupons.add(matcherCode.group() + "\r\n");
        }

        return coupons;
    }

*/

    @Override
    List<String> parseTemplate(RegexPattern... patterns) {

        List<String> coupons = new ArrayList<>();

        String[] columnNames = {"_id", "body", "address", "date"};
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(SMS_INBOX, columnNames, "address='" + SMS_NAME + "'", null, "date asc");//, null, null);

        while (cursor.moveToNext()) {
            String current = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String coupon = null;
            for (RegexPattern pattern : patterns) {
                coupon = parseTemplateCoupon(current, pattern);
                if (coupon != null)
                    coupons.add(coupon);
            }
        }
        return coupons;

    }
    String parseTemplateCoupon(String current, RegexPattern pattern)  {

        String[] extractedData = pattern.extract();


        Pattern patternCode = Pattern.compile(extractedData[0]);
        Matcher matcherCode = patternCode.matcher(current);

        if (pattern.getTask() == Task.SMS || pattern.getTask() == Task.Philips) {
            Pattern patternAmount = Pattern.compile(extractedData[1]);
            Pattern patternSum = Pattern.compile(extractedData[2]);
            Pattern patternCond = Pattern.compile(extractedData[3]);

            Matcher matcherAmount = patternAmount.matcher(current);
            Matcher matcherSum = patternSum.matcher(current);
            Matcher matcherCond = patternCond.matcher(current);

            if (matcherSum.find() && matcherCode.find() && matcherAmount.find() && matcherCond.find())
                return calculatePrice(matcherAmount.group()) + "/" +
                        calculatePrice(matcherSum.group()) + ": " +
                        matcherCode.group() + "\r\n";

        } else {
            if (matcherCode.find())
                return  matcherCode.group() + "\r\n";
        }
        return null;
    }
}