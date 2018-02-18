package com.liarstudio.mvideosmsfilter;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.liarstudio.mvideosmsfilter.Parsers.AndroidParser;
import com.liarstudio.mvideosmsfilter.Parsers.Parser;
import com.liarstudio.mvideosmsfilter.Parsers.SortOrder;
import com.liarstudio.mvideosmsfilter.Parsers.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class SaveFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    private static final int PERMISSION_REQUEST_READ_SMS = 100;
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 101;

    final String CLASS_NAME = getClass().getSimpleName();

    static final int REQUEST_CODE_SEND = 3443;

    enum ButtonAction {SAVE, EMAIL}


    Button btnSave;
    Button btnEmail;

    TextView tvStatus;
    TextView tvSort;

    CheckBox cbPickDate;
    DatePicker datePicker;
    ProgressBar progressBar;

    RadioGroup rgSort;

    View toastView;
    Toast toastResult;

    SortOrder sortOrder;
    boolean[] tasks;

    String fileName = "mvideo.txt";


    public SaveFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasks = new boolean[Task.values().length];
        tasks[1] = true;
        sortOrder = SortOrder.SUM;
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_save, container, false);
        btnSave = view.findViewById(R.id.btnSave);
        btnEmail = view.findViewById(R.id.btnEmail);

        tvSort = view.findViewById(R.id.tv_sort);
        tvStatus = view.findViewById(R.id.tv_status);

        rgSort = view.findViewById(R.id.rg_sort);
        progressBar = view.findViewById(R.id.progress_bar);

        toastView = inflater.inflate(R.layout.toast_main, (ViewGroup) view.findViewById(R.id.toast_main_container));


        CheckBox cbSms = view.findViewById(R.id.cb_sms_promo);
        CheckBox cbPhilips = view.findViewById(R.id.cb_philips);
        CheckBox cbSorry = view.findViewById(R.id.cb_sorry);
        CheckBox cbPickup = view.findViewById(R.id.cb_pickup);
        cbSms.setOnCheckedChangeListener(this); cbPhilips.setOnCheckedChangeListener(this);
        cbPickup.setOnCheckedChangeListener(this); cbSorry.setOnCheckedChangeListener(this);
        cbSms.setChecked(true);

        cbPickDate = view.findViewById(R.id.cb_pick_date);
        cbPickDate.setOnCheckedChangeListener(this);

        datePicker = view.findViewById(R.id.date_picker);

        initListeners();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        Log.d(CLASS_NAME, "onResume");
        toastResult = new Toast(getActivity());
        super.onResume();

    }

    void showToast(String message) {
        TextView text = toastView.findViewById(R.id.toast_text_view);
        text.setText(message);

        toastResult.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toastResult.setDuration(Toast.LENGTH_SHORT);
        toastResult.setView(toastView);
        toastResult.show();
    }


    void initListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                requestPermissions(
                        new String[]{Manifest.permission.READ_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_STORAGE
                );
            }
        });

        /*SaveFragment.FilterTask task = new SaveFragment.FilterTask();
        task.execute(ButtonAction.SAVE);
        */

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                requestPermissions(
                        new String[]{Manifest.permission.READ_SMS},
                        PERMISSION_REQUEST_READ_SMS
                );
            }
        });
        rgSort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.rbDate:
                        sortOrder = SortOrder.DATE;
                        break;
                    case R.id.rbSum:
                        sortOrder = SortOrder.SUM;
                        break;
                    default:
                        sortOrder = SortOrder.SUM;
                        break;
                }
            }
        });

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cb_sms_promo:
                tasks[0] = b;
                break;
            case R.id.cb_philips:
                tasks[1] = b;
                break;
            case R.id.cb_sorry:
                tasks[2] = b;
                break;
            case R.id.cb_pickup:
                tasks[3] = b;
                break;
            default:
                if (b)
                    datePicker.setVisibility(View.VISIBLE);
                else
                    datePicker.setVisibility(View.GONE);
        }
    }

    class FilterTask extends AsyncTask<ButtonAction, String, Void> {

        List<String> messages;
        SortOrder innerSortOrder;
        String statusMessage;
        List<Task> innerTasks;
        Calendar date;

        @Override
        protected void onPreExecute() {
            innerSortOrder = sortOrder;

            messages = new ArrayList<>();

            innerTasks = new ArrayList<>();
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i])
                    innerTasks.add(Task.values()[i]);
            }
            if (cbPickDate.isChecked()) {
                date = new GregorianCalendar(
                        datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth()
                );

            }


            tvStatus.setText("Формируем список шаблонов...");
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.requestFocus();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (statusMessage != null)
                showToast(statusMessage);
            progressBar.setVisibility(View.GONE);
            tvStatus.setVisibility(View.GONE);
        }

        boolean parse() {
            publishProgress("Парсинг сообщений...");
            Parser parser = new AndroidParser(getActivity());
            messages = parser.parse(sortOrder, date, innerTasks.toArray(new Task[innerTasks.size()]));
            if (messages.isEmpty()) {
                statusMessage = getResources().getString(R.string.status_empty);
                return false;
            }
            return true;
        }

        void saveToFile() {
            publishProgress("Сохранение в файл...");
            File dir = new File(Environment.getExternalStorageDirectory(), "Coupons");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, fileName);

            try {
                file.createNewFile();

                FileWriter out = new FileWriter(file);
                for (String message : messages)
                    out.write(message);
                out.close();
                statusMessage = getResources().getString(R.string.status_ok);
            } catch (IOException e) {
                e.printStackTrace();
                statusMessage = getResources().getString(R.string.status_io_error);
            }
        }

        void sendEmail() {
            publishProgress("Подготовка почтового клиента...");
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            /*emailIntent.setType("message/rfc822");
            StringBuilder messageList = new StringBuilder();
            for (String message : messages) {
                messageList.append(message);
            }
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, messageList.toString()); */
            emailIntent.setType("text/*");
            StringBuilder messageList = new StringBuilder();
            for (String message : messages) {
                messageList.append(message);
            }
            emailIntent.putExtra(Intent.EXTRA_TEXT, messageList.toString());

            try {
                startActivity(Intent.createChooser(emailIntent, "Послать купоны..."));
            } catch (ActivityNotFoundException anfe) {
                statusMessage = "У вас не установлены почтовые клиенты.";
            }
        }

        @Override
        protected Void doInBackground(ButtonAction... buttonActions) {
            if (parse()) {
                if (buttonActions[0] == ButtonAction.SAVE) {
                    saveToFile();
                } else {
                    sendEmail();
                }
            }
            SystemClock.sleep(1500);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tvStatus.setText(values[0]);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == REQUEST_CODE_SEND) {
            showToast(getResources().getString(R.string.status_sent));
        }*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_SMS:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FilterTask st = new FilterTask();
                    st.execute(ButtonAction.EMAIL);
                } else {
                    progressBar.setVisibility(View.GONE);
                    showToast(getResources().getString(R.string.permission_error_sms));
                }
                break;
            case PERMISSION_REQUEST_WRITE_STORAGE:
                if (grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    FilterTask st = new FilterTask();
                    st.execute(ButtonAction.SAVE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    showToast(getResources().getString(R.string.permission_error_write_storage));
                }
        }
    }
}
