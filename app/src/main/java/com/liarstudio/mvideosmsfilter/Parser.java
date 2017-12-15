package com.liarstudio.mvideosmsfilter;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;

public abstract  class Parser {

    static String EOS = "\r\n";
    static String EOSP = "\\r\\n";

    public List<String> parse(SortOrder order, Calendar date, Task... task) {



        RegexPattern[] patterns = new RegexPattern[task.length];
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = new RegexPattern(task[i]);
        }
        List<String> coupons = parseTemplate(date, patterns);
        //if (task.length == 1 &&  order == SortOrder.SUM &&
        //        (task[0] == Task.SMS || task[0] == Task.Philips))

        if (order== SortOrder.SUM) {
            return sort(coupons);
        }
        return coupons;

        /*switch (task) {
            case SMS:
                coupons = parseTemplate(
                        RegexPattern.SMS_COUPON_PATTERN,
                        RegexPattern.AMOUNT_PATTERN,
                        RegexPattern.SUM_PATTERN);
                if (!coupons.isEmpty() && order == SortOrder.SUM)
                    return sort(coupons);
                break;
            case Philips:
                coupons = parseTemplate(
                        RegexPattern.SMS_COUPON_PATTERN,
                        RegexPattern.AMOUNT_PATTERN,
                        RegexPattern.SUM_PATTERN,
                        RegexPattern.PHILIPS_COUPON_CONDITION
                );
                if (!coupons.isEmpty() && order == SortOrder.SUM)
                    return sort(coupons);
                break;
            case PickUp:
                coupons = parseTemplate(RegexPattern.PICKUP_COUPON_PATTERN);
                break;
            default:
                coupons = parseTemplate(RegexPattern.SORRY_COUPON_PATTERN);
                break;
        }
        */
    }




    abstract List<String> parseTemplate(Calendar date, RegexPattern... patterns);
    /*abstract List<String> parseTemplate(String code, String amount, String sum,
                                        String condition);
*/



    public List<String> sort(List<String> coupons) {
        for (int i = 0; i < coupons.size(); i++) {
            for (int j = i + 1; j < coupons.size(); j++)
                if (extractPrice(coupons.get(i)) > extractPrice(coupons.get(j))) {
                    String tmp = coupons.get(i);
                    coupons.set(i, coupons.get(j));
                    coupons.set(j, tmp);
                }}

        return coupons;
    }


    protected Long calculatePrice(String extracted)
    {
        java.util.regex.Pattern patternPrice = java.util.regex.Pattern.compile("[0-9]{4,5}");
        Matcher matcherPrice = patternPrice.matcher(extracted);
        matcherPrice.find();

        String correct_number = matcherPrice.group().replace(" ", "");
        return Long.parseLong(correct_number);
    }

    private Long extractPrice(String coupon) {
        String number = coupon.split("/")[0].replaceAll(EOSP, "");
        return Long.parseLong(number);
    }
    public static String extractConfirmation(String message) {

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
