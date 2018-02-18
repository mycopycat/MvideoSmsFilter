package com.liarstudio.mvideosmsfilter;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.liarstudio.mvideosmsfilter.Parsers.RegexPattern;
import com.liarstudio.mvideosmsfilter.Receivers.SimStateChangedListener;
import com.liarstudio.mvideosmsfilter.Receivers.SmsDeliveredListener;
import com.liarstudio.mvideosmsfilter.Receivers.SmsReceivedListener;
import com.liarstudio.mvideosmsfilter.Receivers.SimStateReceiver;
import com.liarstudio.mvideosmsfilter.Receivers.SmsDeliveredReceiver;
import com.liarstudio.mvideosmsfilter.Receivers.SmsReceivedReceiver;

import org.jetbrains.annotations.NotNull;

import static com.liarstudio.mvideosmsfilter.Receivers.SmsDeliveredReceiver.EXTRA_SIM_NUMBER;


public class SendFragment extends Fragment
        implements SimStateChangedListener, SmsDeliveredListener, SmsReceivedListener{

    public static final int REQUEST_CODE_SEND_SMS = 102;
    public static String[] SEND_SMS_PERMISSIONS = {Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_SMS};

    final String CLASS_NAME = this.getClass().getSimpleName();


    SmsDeliveredReceiver smsDeliveredReceiver;
    SmsReceivedReceiver smsReceiverReceiver;
    SimStateReceiver simStateReceiver;
    Notificator notificator;
    Button btnSend;
    EditText etNumber;
    EditText etMessage;
    RadioGroup rgSim;

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

        notificator = new Notificator(getContext());
        simStateReceiver = new SimStateReceiver();
        simStateReceiver.setListener(this);

        smsDeliveredReceiver = new SmsDeliveredReceiver();
        smsDeliveredReceiver.setListener(this);


        smsReceiverReceiver = new SmsReceivedReceiver();
        smsReceiverReceiver.setListener(this);

        super.onCreate(savedInstanceState);

        toastResult = new Toast(getActivity());
        toastResult.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toastResult.setDuration(Toast.LENGTH_SHORT);
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
        rgSim = view.findViewById(R.id.rg_sim);
        tvStatus = view.findViewById(R.id.tv_status);
        progressBar = view.findViewById(R.id.progress_bar);
        if (isTaskRunning)
            progressBar.setVisibility(View.VISIBLE);
        initListeners();

        toastView = inflater.inflate(R.layout.toast_main, (ViewGroup) view.findViewById(R.id.toast_main_container));
        getActivity().registerReceiver(smsDeliveredReceiver, new IntentFilter(Intent.ACTION_SEND));
        getActivity().registerReceiver(smsReceiverReceiver, new IntentFilter(SmsReceivedReceiver.RECEIVER_ACTION));
        getActivity().registerReceiver(simStateReceiver, new IntentFilter(SimStateReceiver.SIM_STATE_CHANGED));
        simStateReceiver.onReceive(getContext(), new Intent(SimStateReceiver.SIM_STATE_CHANGED));

        //buildReceiveSmsDialog("большое большое смс сообщение бла бла бла бла бла бла бла отправьте хуй на мидери чтобы почесть память предков");
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(CLASS_NAME, "onDestroyView");
        getActivity().unregisterReceiver(smsDeliveredReceiver);
        getActivity().unregisterReceiver(smsReceiverReceiver);
        getActivity().unregisterReceiver(simStateReceiver);
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

    @Override
    public void onSimStateChanged(boolean isAbsent) {
        if (isAbsent) rgSim.setVisibility(View.GONE); else rgSim.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSmsDelivered(@NotNull String message, boolean isOk) {
        if (isOk)
            smsSent++;
        showToast(message);
    }

    @Override
    public void onSmsDelivered(@NotNull String message) { smsSent++; showToast(message);}

    @Override
    public void onSmsReceived(String message) {messageToMv = message;}

    class SendTask extends AsyncTask<String, String, Void> {

        String number;
        String message;
        SimSelector selector;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsSent = 0;
            isTaskRunning = true;
            switch (rgSim.getCheckedRadioButtonId()) {
                case R.id.rb_first: {selector = SimSelector.FIRST; break;}
                case R.id.rb_second: {selector = SimSelector.SECOND; break;}
                case R.id.rb_all: {selector = SimSelector.ALL; break;}
            }
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Подготовка к отправке смс...");
            notificator.showNotification("Смс сообщения", "Отправка...", android.R.drawable.ic_menu_share, true);
            progressBar.setVisibility(View.VISIBLE);
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            notificator.showNotification("Смс сообщения", "Отправка завершена", android.R.drawable.ic_dialog_email, false);
            isTaskRunning = false;
            smsSent = 0;
            progressBar.setVisibility(View.GONE);
            tvStatus.setVisibility(View.GONE); //setText("");
        }
        /*void sendMessage() {
            SmsReceiver smsDeliveredReceiver = new SmsReceiver();
            MessageSender ms = new MessageSender(getActivity());
            if (isDual)
                ms.send(number, message, "Смс отправлено, SIM", smsDeliveredReceiver);
            else
               // ms.sendOne(number, message);
        }*/

        @Override
        protected Void doInBackground(String... strings) {
            number = strings[0];
            message = strings[1];

            MessageSender ms = new MessageSender(getActivity());
            int simCount = ms.getManagers().size();
            int position = 0;
            if (simCount > 1) {
                switch (selector) {
                    case FIRST: {simCount = 1; break;}
                    case SECOND: {position = 1; break;}
                }
            }

            for (; position < simCount; position++) {

                smsSent = 0;

                messageToMv = null;
                String simName = "SIM" + (position + 1);
                publishProgress("Отправка сообщения с " + simName);

                PendingIntent sendPI = PendingIntent.getBroadcast(getActivity(), 0,
                        new Intent(Intent.ACTION_SEND).putExtra(EXTRA_SIM_NUMBER, simName),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                ms.sendOne(number, message, sendPI, position);
                int j = 0;

                while (smsSent == 0 && j < 40) {
                    j++;
                    publishProgress(simName + ": отправка первого СМС");
                    SystemClock.sleep(500);
                }
                if (j >= 40) {
                    publishProgress("Тайм-аут!");
                    SystemClock.sleep(2500);
                    break;
                }
                if (!number.equals(RegexPattern.NUMBER_3443)) {

                    j = 0;
                    while (messageToMv == null && j < 120) {
                        j++;
                        publishProgress(simName + ": ожидание СМС от " + RegexPattern.NUMBER_2420);
                        SystemClock.sleep(500);
                    }
                    if (j >= 120) {

                        publishProgress("Тайм-аут!");
                        SystemClock.sleep(2500);
                        break;
                    }
                    PendingIntent sendPINext = PendingIntent.getBroadcast(getActivity(), 0,
                            new Intent(Intent.ACTION_SEND).putExtra(EXTRA_SIM_NUMBER, simName),
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    ms.sendOne(number, messageToMv, sendPINext, position);

                    j = 0;
                    while (smsSent == 1 && j < 40) {
                        j++;
                        publishProgress(simName + ": отправка второго СМС");
                        SystemClock.sleep(500);
                    }
                    if (j >= 40) {
                        publishProgress(simName + ": Тайм-аут!");
                        SystemClock.sleep(2500);
                        break;
                    }
                    publishProgress("Отправка с " + simName + " завершена!");
                    SystemClock.sleep(1500);

                }
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

}