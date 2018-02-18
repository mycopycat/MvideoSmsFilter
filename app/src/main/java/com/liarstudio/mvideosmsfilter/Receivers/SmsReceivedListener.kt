package com.liarstudio.mvideosmsfilter.Receivers

/**
 * Created by mider on 18.02.2018.
 */
interface SmsReceivedListener {
    fun onSmsReceived(message: String?)
}