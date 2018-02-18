package com.liarstudio.mvideosmsfilter.Parsers

import java.util.Calendar

enum class SortOrder { DATE, SUM }

abstract class Parser {

    fun parse(order: SortOrder, date: Calendar, vararg task: Task): List<String> {

        //val patterns = arrayOfNulls<RegexPattern>(task.size)
        val patterns = Array<RegexPattern>(task.size,  { RegexPattern(task[it]) })
        /*for (i in task.indices) {
            patterns[i] = RegexPattern(task[i])
        }*/
        val coupons = parseTemplate(date, *patterns)
        //if (task.length == 1 &&  order == SortOrder.SUM &&
        //        (task[0] == Task.SMS || task[0] == Task.Philips))

        return if (order == SortOrder.SUM) {
            sort(coupons)
        } else coupons


    }


    internal abstract fun parseTemplate(date: Calendar, vararg patterns: RegexPattern): MutableList<String>


    fun sort(coupons: MutableList<String>): List<String> {
        for (i in coupons.indices) {
            for (j in i + 1 until coupons.size)
                if (extractPrice(coupons[i]) > extractPrice(coupons[j])) {
                    val tmp = coupons[i]
                    coupons[i] = coupons[j]
                    coupons[j] = tmp
                }
        }

        return coupons
    }


    protected fun calculatePrice(extracted: String): Long {
        val patternPrice = java.util.regex.Pattern.compile("[0-9]{3,5}")
        val matcherPrice = patternPrice.matcher(extracted)
        matcherPrice.find()

        val correct_number = matcherPrice.group().replace(" ", "")
        return java.lang.Long.parseLong(correct_number)
    }

    private fun extractPrice(coupon: String): Long {
        val number = coupon.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].replace(EOSP.toRegex(), "")
        return java.lang.Long.parseLong(number)
    }

    companion object {

        internal var EOS = "\r\n"
        internal var EOSP = "\\r\\n"
        fun extractConfirmation(message: String): String? {

            var i = message.indexOf(RegexPattern.CONFIRMATION_2420_PATTERN)
            if (i >= 0) {
                i += RegexPattern.CONFIRMATION_2420_PATTERN.length
                var confirmation_string = ""
                while (i < message.length && message[i] != ' ') {
                    confirmation_string += message[i]
                    i++
                }
                return confirmation_string
            }
            return null
        }
    }

}
