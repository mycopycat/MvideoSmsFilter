package com.liarstudio.mvideosmsfilter.Receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

class SmsDeliveredReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SIM_NUMBER = "sim.number"
    }

    var listener: SmsDeliveredListener? = null


    override fun onReceive(context: Context, intent: Intent) {
        when (resultCode) {
            android.app.Activity.RESULT_OK -> listener!!.onSmsDelivered("Смс отправлено; " + intent.getStringExtra(EXTRA_SIM_NUMBER), true)
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> listener!!.onSmsDelivered("Ошибка! Generic failure; " + intent.getStringExtra(EXTRA_SIM_NUMBER))
            SmsManager.RESULT_ERROR_NO_SERVICE -> listener!!.onSmsDelivered("Ошибка! No service; " + intent.getStringExtra(EXTRA_SIM_NUMBER))
            SmsManager.RESULT_ERROR_NULL_PDU -> listener!!.onSmsDelivered("Ошибка! Null PDU; " + intent.getStringExtra(EXTRA_SIM_NUMBER))
            SmsManager.RESULT_ERROR_RADIO_OFF -> listener!!.onSmsDelivered("Ошибка! Radio off; " + intent.getStringExtra(EXTRA_SIM_NUMBER))
        }
    }
}
