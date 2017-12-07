package com.liarstudio.mvideosmsfilter;

/**
 * Created by Mihail on 03.12.2017.
 */
enum Task { SMS, Philips, Sorry, PickUp }

public class RegexPattern {
    public static String AMOUNT_PATTERN = "скидку [0-9]{1,2}000";
    public static String SUM_PATTERN = "от [0-9]{4,5}";

    public static String SMS_COUPON_PATTERN = "([0-9]{15})";
    public static String SORRY_COUPON_PATTERN = "7[0-9]{14}";
    public static String PICKUP_COUPON_PATTERN = "9[0-9]{14}";
    public static String PHILIPS_COUPON_CONDITION = "Philips"; //"(?=.*[0-9]{15})(?=.*Philips)";

    public Task getTask() {
        return task;
    }

    private Task task;
    private String code;
    public RegexPattern(Task task) {
        this.task = task;
    }

    public String[] extract() {
        switch (task) {
            case SMS:
                return new String[]{SMS_COUPON_PATTERN, AMOUNT_PATTERN, SUM_PATTERN, ""};
            case Philips:
                return new String[]{SMS_COUPON_PATTERN, AMOUNT_PATTERN, SUM_PATTERN, PHILIPS_COUPON_CONDITION};
            case Sorry:
                return new String[]{SORRY_COUPON_PATTERN};
            case PickUp:
                return new String[]{PICKUP_COUPON_PATTERN};
            default:
                return null;
        }
    }

}
