package com.liarstudio.mvideosmsfilter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

enum SortOrder { DATE, SUM }


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CODE_SEND = 3443;

    enum ButtonAction { SAVE, EMAIL }


    Button btnSave;
    Button btnEmail;

    TextView tvStatus;
    TextView tvSort;

    RadioGroup radioGroup;
    ProgressBar progressBar;

    SortOrder sortOrder;


    String fileName = "filtered.txt";
    ButtonAction outerAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnEmail = (Button) findViewById(R.id.btnEmail);

        tvSort = (TextView) findViewById(R.id.tvSort);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        outerAction = ButtonAction.SAVE;
        sortOrder = SortOrder.SUM;
        initListeners();
    }

    void initListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outerAction = ButtonAction.SAVE;
                FilterTask task = new FilterTask();
                task.execute();

            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outerAction = ButtonAction.EMAIL;
                FilterTask task = new FilterTask();
                task.execute();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.rbDate:
                        sortOrder = SortOrder.DATE;
                        break;
                    case R.id.rbSum:
                        sortOrder = SortOrder.SUM;
                        break;
                }
            }
        });

    }


    void toggleStatus(String message) {
        if (message != null && !message.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(message);
        } else {
            tvStatus.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

    }

    class FilterTask extends AsyncTask<Void, Void, Void> {

        List<String> messages;
        String statusMessage;
        SortOrder innerSortOrder;

        ButtonAction innerAction;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            innerAction = outerAction;
            innerSortOrder = sortOrder;
            toggleStatus("");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (innerAction == ButtonAction.SAVE)
                toggleStatus(statusMessage);
        }

        boolean parse() {

            Parser parser = new Parser(getApplicationContext());
            messages = parser.parse("M.Video", sortOrder);
            if (messages.isEmpty()) {
                statusMessage = getResources().getString(R.string.status_empty);
                return false;
            }
            return true;
        }
        void saveToFile() {

                File dir = new File(Environment.getExternalStorageDirectory(), "MVideo");
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
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            StringBuilder mesasgeList = new StringBuilder();
            for (String message : messages) {
                mesasgeList.append(message);
            }
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, mesasgeList.toString());
            try {
                statusMessage = getResources().getString(R.string.status_ok);
                startActivityForResult(Intent.createChooser(emailIntent, "Послать купоны..."), REQUEST_CODE_SEND);
            } catch (ActivityNotFoundException anfe) {
                Toast.makeText(MainActivity.this, "У вас не установлены почтовые клиенты.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if ( parse() ) {
                if (innerAction == ButtonAction.SAVE) {
                    saveToFile();
                } else {
                    sendEmail();
                }
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEND) {
            toggleStatus(getResources().getString(R.string.status_sent));
        }
    }
}
