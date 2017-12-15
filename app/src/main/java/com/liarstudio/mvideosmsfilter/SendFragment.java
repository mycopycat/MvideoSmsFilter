package com.liarstudio.mvideosmsfilter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class SendFragment extends Fragment {

    public static final int REQUEST_CODE_SEND_SMS = 102;
    public static final int NOTIFICATION_SMS_ID = 1;
    public static String[] SEND_SMS_PERMISSIONS = {Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_SMS};

    final String CLASS_NAME = this.getClass().getSimpleName();
    public static String ACTION_SMS_SENT = "sms sent";
    public static String EXTRA_SIM_NUMBER = "sim number";
    static final String RECEIVER_ACTION ="android.provider.Telephony.SMS_RECEIVED";


    BroadcastReceiver receiver;
    BroadcastReceiver receiverNext;
    Button btnSend;
    EditText etNumber;
    EditText etMessage;
    CheckBox cbDualSim;

    TextView tvStatus;
    ProgressBar progressBar;

    View toastView;
    Toast toastResult;

    boolean isTaskRunning;
    int smsSent;
    String messageToMv = null;

    public SendFragment() {

        Log.d(CLASS_NAME, "Constructor");
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreate");
        setRetainInstance(true);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case android.app.Activity.RESULT_OK:
                        smsSent++;
                        showToast("Смс отправлено; " + intent.getStringExtra(EXTRA_SIM_NUMBER));
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        showToast("Ошибка! Generic failure; " + intent.getStringExtra(EXTRA_SIM_NUMBER));
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        showToast("Ошибка! No service; " + intent.getStringExtra(EXTRA_SIM_NUMBER));
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showToast("Ошибка! Null PDU; " + intent.getStringExtra(EXTRA_SIM_NUMBER));
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        showToast("Ошибка! Radio off; " + intent.getStringExtra(EXTRA_SIM_NUMBER));
                        break;
                }
            }
        };


        receiverNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(RECEIVER_ACTION)) {

                    SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                    String body = "";
                    String phone = "";
                    for (SmsMessage msg: smsMessages) {
                        body = StringUtils.buildString(body, msg.getMessageBody());
                        phone = msg.getOriginatingAddress();

                        if (!phone.equals("2420"))
                            return;
                        //Toast.makeText(GasService.this,strMessage, Toast.LENGTH_SHORT).show();
                        }
                    if(phone.equals("2420"))
                    {
                        if (body.contains(RegexPattern.REFUSE_2420) || body.contains(RegexPattern.NOT_ACCEPTED_2420))
                            return;

                        //buildReceiveSmsDialog(body);
                        messageToMv = Parser.extractConfirmation(body);
                        abortBroadcast();
                    }

                }
            }
        };

        super.onCreate(savedInstanceState);

        toastResult = new Toast(getActivity());
        toastResult.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toastResult.setDuration(Toast.LENGTH_SHORT);
    }

    public void buildReceiveSmsDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("2420");

        LinearLayout layout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);


        final TextView tvBody = new TextView(getActivity());

        tvBody.setPadding(10, 5, 10, 5);

        //tvBody.setElegantTextHeight(true);
        tvBody.setMinLines(5);
        tvBody.setMaxLines(50);
        tvBody.setSingleLine(false);
        tvBody.setGravity(Gravity.CENTER);
        //tvBody.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tvBody.setText(msg);
        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setWidth(50);
        input.setGravity(Gravity.CENTER);
        layout.addView(tvBody, 0);
        layout.addView(input, 1);
        builder.setView(layout);


        // Set up the buttons
        builder.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messageToMv = input.getText().toString();
            }
        });
        /*builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });*/

        builder.show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        Log.d(CLASS_NAME, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        btnSend = view.findViewById(R.id.btn_send);
        etNumber = view.findViewById(R.id.et_number);
        etMessage = view.findViewById(R.id.et_message);
        cbDualSim = view.findViewById(R.id.cb_dual_sim);
        tvStatus = view.findViewById(R.id.tv_status);
        progressBar = view.findViewById(R.id.progress_bar);
        if (isTaskRunning)
            progressBar.setVisibility(View.VISIBLE);
        initListeners();

        toastView = inflater.inflate(R.layout.toast_main, (ViewGroup) view.findViewById(R.id.toast_main_container));
        getActivity().registerReceiver(receiver, new IntentFilter(ACTION_SMS_SENT));
        getActivity().registerReceiver(receiverNext, new IntentFilter(RECEIVER_ACTION));
        //buildReceiveSmsDialog("большое большое смс сообщение бла бла бла бла бла бла бла отправьте хуй на мидери чтобы почесть память предков");
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(CLASS_NAME, "onDestroyView");
        getActivity().unregisterReceiver(receiver);
        super.onDestroyView();

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(CLASS_NAME, "onPause");
        toastResult = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(CLASS_NAME, "onResume");
        toastResult = new Toast(getActivity());
    }



    void initListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTaskRunning) {
                    progressBar.setVisibility(View.VISIBLE);
                    requestPermissions(
                            SEND_SMS_PERMISSIONS,
                            REQUEST_CODE_SEND_SMS
                    );
                }
            }
        });
    }

    class SendTask extends AsyncTask<String, String, Void> {

        String number;
        String message;
        boolean isDual;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsSent = 0;
            isTaskRunning = true;
            isDual = cbDualSim.isChecked();
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Подготовка к отправке смс...");
            showNotification("Смс сообщения", "Отправка...", android.R.drawable.ic_menu_share, true);
            progressBar.setVisibility(View.VISIBLE);
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            showNotification("Смс сообщения", "Отправка завершена", android.R.drawable.ic_dialog_email, false);
            isTaskRunning = false;
            smsSent = 0;
            progressBar.setVisibility(View.GONE);
            tvStatus.setVisibility(View.GONE); //setText("");
        }


        /*void sendMessage() {
            SmsReceiver receiver = new SmsReceiver();
            MessageSender ms = new MessageSender(getActivity());
            if (isDual)
                ms.send(number, message, "Смс отправлено, SIM", receiver);
            else
               // ms.sendOne(number, message);
        }*/

        @Override
        protected Void doInBackground(String... strings) {
            number = strings[0];
            message = strings[1];

            MessageSender ms = new MessageSender(getActivity());
            ms.setSims();
            int simCount = ms.managers.size();


            for (int i = 0; i < simCount; i++) {

                smsSent = 0;
                messageToMv = null;
                String simName = StringUtils.buildString("SIM", Integer.toString(i + 1));
                publishProgress(StringUtils.buildString("Отправка сообщения с ", simName));

                PendingIntent sendPI = PendingIntent.getBroadcast(getActivity(), 0,
                        new Intent(ACTION_SMS_SENT).putExtra(EXTRA_SIM_NUMBER, simName),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                ms.sendOne(number, message, sendPI, i);


                int j = 0;
                while (smsSent == 0 && j < 40) {
                    j++;
                    publishProgress(StringUtils.buildString(simName, ": отправка первого СМС"));
                    SystemClock.sleep(500);
                }
                if (j >= 40) {
                    publishProgress("Тайм-аут!");
                    SystemClock.sleep(2500);
                    break;
                }
                j = 0;
                while (messageToMv == null && j < 120) {
                    j++;
                    publishProgress(StringUtils.buildString(simName, ": ожидание СМС от 2420"));
                    SystemClock.sleep(500);
                }
                if (j >= 120) {

                    publishProgress("Тайм-аут!");
                    SystemClock.sleep(2500);
                    break;
                }
                PendingIntent sendPINext = PendingIntent.getBroadcast(getActivity(), 0,
                        new Intent(ACTION_SMS_SENT).putExtra(EXTRA_SIM_NUMBER, simName),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                ms.sendOne(number, messageToMv, sendPINext, i);

                j = 0;
                while (smsSent == 1 && j < 40) {
                    j++;
                    publishProgress(StringUtils.buildString(simName, ": отправка второго СМС"));
                    SystemClock.sleep(500);
                }
                if (j >= 40) {
                    publishProgress(StringUtils.buildString(simName, ": Тайм-аут!"));
                    SystemClock.sleep(2500);
                    break;
                }
                publishProgress(StringUtils.buildString("Отправка с ", simName, " завершена!"));
                SystemClock.sleep(1500);

            }

            publishProgress("Отправка завершена!");
            SystemClock.sleep(1000);
            return null;

        }
        @Override
        protected void onProgressUpdate(String... values) {
            tvStatus.setText(values[0]);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_SEND_SMS && grantResults.length == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                SendTask st = new SendTask();
                st.execute(etNumber.getText().toString(), etMessage.getText().toString());
                return;
            }
        }
        progressBar.setVisibility(View.GONE);
        showToast(getResources().getString(R.string.permission_error_sms));

    }
    void showToast(String message) {
        if (toastResult != null) {

            TextView text = toastView.findViewById(R.id.toast_text_view);
            text.setText(message);

            toastResult.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            toastResult.setDuration(Toast.LENGTH_SHORT);
            toastResult.setView(toastView);
            toastResult.show();
        }
    }
    void showNotification(String title, String message, int id, int icon, boolean isProgressShowing) {
        Context context = getContext();
        /*Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        */
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setProgress(0, 0, isProgressShowing)
                //.setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
    void showNotification(String title, String message, int icon, boolean isProgressRunning) {
        Context context = getContext();
        /*Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        */
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setProgress(0, 0, isProgressRunning)
                //.setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_meh_notif))
                .setOngoing(isProgressRunning)
                .build();
        if (!isProgressRunning)
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_SMS_ID, notification);

    }

}