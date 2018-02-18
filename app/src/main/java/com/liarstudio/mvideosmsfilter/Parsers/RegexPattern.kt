package com.liarstudio.mvideosmsfilter.Parsers

public enum class Task {
    SMS, Philips, Sorry, PickUp
}

public class RegexPattern(val task: Task) {
    private val code: String? = null

    public fun extract(): Array<String>? {
        when (task) {
            Task.SMS -> return arrayOf(SMS_COUPON_PATTERN, AMOUNT_PATTERN, SUM_PATTERN, "")
            Task.Philips -> return arrayOf(SMS_COUPON_PATTERN, AMOUNT_PATTERN, SUM_PATTERN, PHILIPS_COUPON_CONDITION)
            Task.Sorry -> return arrayOf(SORRY_COUPON_PATTERN)
            Task.PickUp -> return arrayOf(PICKUP_COUPON_PATTERN)
            else -> return null
        }
    }

    companion object {
        const val NUMBER_2420 = "2420"
        const val NUMBER_3443 = "3443"

        const val AMOUNT_PATTERN = "скидк[уа][ ]{1,2}[0-9]{1,3}00"
        const val SUM_PATTERN = "от [0-9]{3,5}"

        const val SMS_COUPON_PATTERN = "([0-9]{15})"
        const val SORRY_COUPON_PATTERN = "7[0-9]{14}"
        const val PICKUP_COUPON_PATTERN = "9[0-9]{14}"
        const val PHILIPS_COUPON_CONDITION = "Philips" //"(?=.*[0-9]{15})(?=.*Philips)";

        const val REFUSE_2420 = "Вы отказались"
        const val NOT_ACCEPTED_2420 = "Вы не подтвердили"
        const val CONFIRMATION_2420_PATTERN = "тправьте "
    }

}
