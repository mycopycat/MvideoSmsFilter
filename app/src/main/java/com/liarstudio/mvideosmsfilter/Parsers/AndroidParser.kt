package com.liarstudio.mvideosmsfilter.Parsers

import android.content.Context
import android.net.Uri

import java.util.ArrayList
import java.util.Calendar
import java.util.regex.Pattern

class AndroidParser(private val context: Context) : Parser() {

    internal val SMS_INBOX = Uri.parse("content://sms/inbox")


     override fun parseTemplate(date: Calendar, vararg patterns: RegexPattern): MutableList<String> {

        val coupons = ArrayList<String>()

        val cr = context.contentResolver

        val columnNames = arrayOf("_id", "body", "address", "date")
        val selection: String
        val selectionArgs: Array<String>
        val sortOrder = "date asc"
        if (date == null) {
            selection = "address=?"
            selectionArgs = arrayOf(SMS_NAME)
        } else {
            selection = "address=? AND date>?"
            selectionArgs = arrayOf(SMS_NAME, java.lang.Long.toString(date.timeInMillis))
        }

        val cursor = cr.query(SMS_INBOX, columnNames, selection, selectionArgs, sortOrder)//, null, null);

        while (cursor!!.moveToNext()) {
            val current = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            var coupon: String?
            for (pattern in patterns) {
                coupon = parseTemplateCoupon(current, pattern)
                if (coupon != null)
                    coupons.add(coupon)
            }
        }
        return coupons

    }

    internal fun parseTemplateCoupon(current: String, pattern: RegexPattern): String? {

        val extractedData = pattern.extract()


        val patternCode = Pattern.compile(extractedData!![0])
        val matcherCode = patternCode.matcher(current)

        if (pattern.task === Task.SMS || pattern.task === Task.Philips) {
            val patternAmount = Pattern.compile(extractedData[1])
            val patternSum = Pattern.compile(extractedData[2])
            val patternCond = Pattern.compile(extractedData[3])

            val matcherAmount = patternAmount.matcher(current)
            val matcherSum = patternSum.matcher(current)
            val matcherCond = patternCond.matcher(current)

            if (matcherSum.find() && matcherCode.find() && matcherAmount.find() && matcherCond.find())
                return calculatePrice(matcherAmount.group()).toString() + "/" +
                        calculatePrice(matcherSum.group()) + ": " +
                        matcherCode.group() + "\r\n"

        } else {
            if (matcherCode.find())
                return matcherCode.group() + "\r\n"
        }
        return null
    }

    companion object {
        internal val SMS_NAME = "M.Video"
    }
}