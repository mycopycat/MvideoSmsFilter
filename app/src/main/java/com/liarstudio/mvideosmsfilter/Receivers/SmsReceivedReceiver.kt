package com.liarstudio.mvideosmsfilter.Receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.liarstudio.mvideosmsfilter.Parsers.Parser
import com.liarstudio.mvideosmsfilter.Parsers.RegexPattern

class SmsReceivedReceiver : BroadcastReceiver() {

    companion object {
        const val RECEIVER_ACTION = "android.provider.Telephony.SMS_RECEIVED"
    }
    var listener : SmsReceivedListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action!!.equals(RECEIVER_ACTION, ignoreCase = true)) {

            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            var body = ""
            var phone = ""
            for (msg in smsMessages) {
                body += msg.messageBody
                phone = msg.originatingAddress

                if (phone != RegexPattern.NUMBER_2420)
                    return
            }
            if (phone == RegexPattern.NUMBER_2420) {
                if (body.contains(RegexPattern.REFUSE_2420) || body.contains(RegexPattern.NOT_ACCEPTED_2420))
                    return

                //buildReceiveSmsDialog(body);
                listener!!.onSmsReceived(Parser.extractConfirmation(body))
                abortBroadcast()
            }

        }

    }
}
