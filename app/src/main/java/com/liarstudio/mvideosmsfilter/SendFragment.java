package com.liarstudio.mvideosmsfilter;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class SendFragment extends Fragment {

    static final int REQUEST_CODE_SEND_SMS = 102;
    public static String[] SEND_SMS_PERMISSIONS = {Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE};

    public static String ACTION_SMS_SENT = "sms sent";
    public static String EXTRA_SIM_NUMBER = "sim number";

    BroadcastReceiver receiver;
    Button btnSend;
    EditText etNumber;
    EditText etMessage;
    CheckBox cbDualSim;

    TextView tvStatus;
    ProgressBar progressBar;

    View toastView;

    boolean isTaskRunning;
    int currentSim;

    public SendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                currentSim++;
                switch (getResultCode()) {
                    case android.app.Activity.RESULT_OK:
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(ACTION_SMS_SENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        btnSend = view.findViewById(R.id.btn_send);
        etNumber = view.findViewById(R.id.et_number);
        etMessage = view.findViewById(R.id.et_message);
        cbDualSim = view.findViewById(R.id.cb_dual_sim);
        tvStatus = view.findViewById(R.id.tv_status);
        progressBar = view.findViewById(R.id.progress_bar);
        initListeners();

        toastView = inflater.inflate(R.layout.toast_main, (ViewGroup) view.findViewById(R.id.toast_main_container));

        return view;
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
            currentSim = 0;
            isTaskRunning = true;
            isDual = cbDualSim.isChecked();
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Подготовка к отправке смс...");
            progressBar.setVisibility(View.VISIBLE);
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            isTaskRunning = false;
            currentSim = 0;
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

                String simName = StringUtils.buildString("SIM", Integer.toString(i+1));
                publishProgress(StringUtils.buildString("Отправка сообщения с ", simName));

                PendingIntent sendPI = PendingIntent.getBroadcast(getActivity(), i,
                        new Intent(ACTION_SMS_SENT).putExtra(EXTRA_SIM_NUMBER, simName),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                ms.sendOne(number, message, sendPI, i);

                String tmp = (StringUtils.buildString("Ожидание подтверждения ", simName));
                while (currentSim == i) {
                    publishProgress(tmp);
                    SystemClock.sleep(500);
                    publishProgress((StringUtils.buildString(tmp, ".")));
                    SystemClock.sleep(500);
                    publishProgress((StringUtils.buildString(tmp, "..")));
                    SystemClock.sleep(500);
                    publishProgress((StringUtils.buildString(tmp, "...")));
                    SystemClock.sleep(500);
                }
            }
            publishProgress("Отправка завершена!");
            SystemClock.sleep(1500);
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            tvStatus.setText(values[0]);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_SEND_SMS && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                SendTask st = new SendTask();
                st.execute(etNumber.getText().toString(), etMessage.getText().toString());
                return;
            }
        }
        progressBar.setVisibility(View.GONE);
        showToast(getResources().getString(R.string.permission_error_sms));

    }
    void showToast(String message) {

        TextView text = toastView.findViewById(R.id.toast_text_view);
        text.setText(message);

        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.show();
    }
    void showNotification(String message) {
        Context context = getContext();
        Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("Смс")
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .build();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}