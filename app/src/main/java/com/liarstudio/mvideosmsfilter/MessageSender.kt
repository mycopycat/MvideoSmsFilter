package com.liarstudio.mvideosmsfilter

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager

import java.util.ArrayList


class MessageSender(internal var context: Context) {
    var managers: MutableList<SmsManager>
    var subscriptionManager: SubscriptionManager
    var subInfoList: List<SubscriptionInfo>

    init {
        managers = ArrayList()
        subscriptionManager = SubscriptionManager.from(context)
        subInfoList = subscriptionManager.activeSubscriptionInfoList
        for (i in subInfoList.indices) {
            val simId = subInfoList[i].subscriptionId
            managers.add(SmsManager.getSmsManagerForSubscriptionId(simId))
        }
    }

    fun sendOne(number: String, message: String, sendPI: PendingIntent, managerPosition: Int) {
        managers[managerPosition].sendTextMessage(number, null, message, sendPI, null)
    }

}
