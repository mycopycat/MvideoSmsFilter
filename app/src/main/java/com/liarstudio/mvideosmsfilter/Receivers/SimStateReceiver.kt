package com.liarstudio.mvideosmsfilter.Receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.liarstudio.mvideosmsfilter.MessageSender


class SimStateReceiver : BroadcastReceiver() {
    companion object {
        const val SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    }
    var listener: SimStateChangedListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        when(manager.simState) {
            TelephonyManager.SIM_STATE_ABSENT -> {
                listener!!.onSimStateChanged(true)}
            TelephonyManager.SIM_STATE_READY-> {
                val sender = MessageSender(context)
                listener!!.onSimStateChanged(sender.managers.size < 2)
                }
        }


    }
}
