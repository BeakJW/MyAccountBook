package com.jinwoo.myaccountbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    CalendarView calView;
    TextView tv;
    int selectYear, selectMonth, selectDay;
    String filename;

    EditText editText, editText2;
    View dialogView;

    static int income = 0;
    static int expense = 0;
    static int balance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calView = findViewById(R.id.calendarView);
        tv = findViewById(R.id.textView);

        //SD카드에 READ, WRITE권한 주기
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        //SD카드 경로 지정
        final String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File myDir = new File(sdpath + "/Account");
        myDir.mkdir();    //sd카드에 Account폴더 생성
        load();

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int datOfMonth) {
                selectYear = year;
                selectMonth = month + 1;    //시스템 상에서는 month가 1 작게 나오기 때문
                selectDay = datOfMonth;


                filename = Integer.toString(selectYear) + "년"
                        + Integer.toString(selectMonth) + "월"
                        + Integer.toString(selectDay) + "일";
                String path = sdpath + "/Account/" + filename + ".txt";
                File files = new File(path);
                if (files.exists()) {    //파일이 존재하는 경우 읽어오기
                    try {
                        FileInputStream fin = new FileInputStream(path);
                        byte[] txt = new byte[100];
                        fin.read(txt);
                        String str = new String(txt);
                        AlertDialog.Builder readDlg = new AlertDialog.Builder(MainActivity.this);
                        readDlg.setTitle("가계부 읽기");
                        readDlg.setMessage(str);
                        readDlg.setPositiveButton("확인", null);
                        readDlg.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {    //파일이 존재하지 않는 경우 파일 생성하기
                    dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog, null);
                    editText = dialogView.findViewById(R.id.editText);
                    editText2 = dialogView.findViewById(R.id.editText2);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                    dlg.setTitle("가계부 쓰기");
                    dlg.setView(dialogView);
                    dlg.setNegativeButton("취소", null);
                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                FileOutputStream fout = new FileOutputStream(path);
                                String editStr = editText.getText().toString();
                                int etIncome = Integer.parseInt(editStr.equals("") || editStr == null ? "0" : editStr);
                                //조건? 참: 거짓
                                String editStr2 = editText2.getText().toString();
                                int etExpense = Integer.parseInt(editStr2.equals("") || editStr2 == null ? "0" : editStr2);
                                String writeStr = "수입 : " + etIncome + "\n" + "지출 : " + etExpense;
                                fout.write(writeStr.getBytes());
                                fout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            load();
                        }
                    });
                    dlg.show();
                }
            }
        });
    }

    public void load() {
        String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(sdpath + "/Account");
        File list[] = file.listFiles();
        List<Map<String, Integer>> moneyList = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(list[i].toString());
                byte[] txt = new byte[100];
                fin.read(txt);
                String str = new String(txt);
                String[] strArr = str.split("\n");
                Map<String, Integer> map = new HashMap<>();
                int in = Integer.parseInt(strArr[0].replaceAll("[^0-9]", ""));
                map.put("Income", in);
                int ex = Integer.parseInt(strArr[1].replaceAll("[^0-9]", ""));
                map.put("Expense", ex);
                moneyList.add(i, map);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("******" + moneyList.toString());
        income = 0;
        expense = 0;
        for (int i = 0; i < moneyList.size(); i++) {
            income += moneyList.get(i).get("Income");
            expense += moneyList.get(i).get("Expense");
        }
        balance = income - expense;
        String str = "수입 합계 : " + income + "\n"
                + "지출 합계 : " + expense + "\n"
                + "잔액 : " + balance;
        tv.setText(str);
    }
}


