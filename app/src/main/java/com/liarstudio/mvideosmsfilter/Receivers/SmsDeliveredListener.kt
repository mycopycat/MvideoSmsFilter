package com.liarstudio.mvideosmsfilter.Receivers

/**
 * Created by mider on 18.02.2018.
 */
interface SmsDeliveredListener {
    fun onSmsDelivered(message: String, isOk: Boolean)
    fun onSmsDelivered(message: String) {onSmsDelivered(message, false)}
}