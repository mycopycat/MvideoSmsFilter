package com.liarstudio.mvideosmsfilter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mihail on 04.12.2017.
 */

public class MessageSender {
    PendingIntent sendPI;
    List<SmsManager> managers;

    Context context;

    public static SubscriptionManager subscriptionManager;
    public static List<SubscriptionInfo> subInfoList;

    public MessageSender(Context context) {
        this.context = context;
        setManagers();
    }

    public void setManagers(int[] simIds) {
        managers = new ArrayList<>();
        for (int id : simIds)
            managers.add(SmsManager.getSmsManagerForSubscriptionId(id));
    }

    public void setManagers(int simId) {
        managers = new ArrayList<>();
        managers.add(SmsManager.getSmsManagerForSubscriptionId(simId));
    }

    public void setManagers() {
        managers = new ArrayList<>();
        managers.add(SmsManager.getDefault());
    }

    public void setSims() {
        managers = new ArrayList<>();
        subscriptionManager = SubscriptionManager.from(context);
        subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (int i = 0; i < subInfoList.size(); i++) {
            int simId = subInfoList.get(i).getSubscriptionId();
            managers.add(SmsManager.getSmsManagerForSubscriptionId(simId));
        }

    }


    public void sendOne(String number, String message, PendingIntent sendPI, int managerPosition) {

        managers.get(managerPosition).sendTextMessage(number, null, message, sendPI, null);
    }
    public void send(String number, String message, String intentFilter, BroadcastReceiver receiver) {
        setSims();
        for (int i = 0; i < managers.size(); ++i) {
            String filter = intentFilter + (i+1);
            sendPI = PendingIntent.getBroadcast(context, i,
                    new Intent(filter),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            managers.get(i).sendTextMessage(number, null, message, sendPI, null);
            context.registerReceiver(receiver, new IntentFilter(filter));
            receiver.getResultCode();

        }
    }
}
