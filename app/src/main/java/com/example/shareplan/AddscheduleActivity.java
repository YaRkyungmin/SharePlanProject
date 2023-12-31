package com.example.shareplan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AddscheduleActivity extends AppCompatActivity {
    private EditText editText;
    private RadioGroup radioGroup;
    private ArrayList<TodoInfo> arrayList;
    private DatePicker datePicker;
    private Button button;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        radioGroup = findViewById(R.id.radiogroup);
        editText = findViewById(R.id.schedule_name);
        datePicker = findViewById(R.id.date);

        TodoInfo todoInfo = new TodoInfo();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.homework:
                        todoInfo.setType("Homework");
                        break;
                    case R.id.quiz:
                        todoInfo.setType("Quiz");
                        break;
                    case R.id.lecture:
                        todoInfo.setType("Lecture");
                        break;
                    case R.id.test:
                        todoInfo.setType("Test");
                        break;
                    default:
                        todoInfo.setType("");
                }
            }
        });

        // 달력에서 선택된 날짜 intent로 받아오기
        Intent intent = getIntent();
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0);
        int day = intent.getIntExtra("day", 0);
        // date picker 건들이지 않아도 todoInfo 객체에 date 속성 set
        todoInfo.setDate(String.valueOf(year) + "-" + String.valueOf(month+1) + "-" + String.valueOf(day));
        // datePicker intent로 받아온 날짜 데이터로 초기화
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            // datePicker에 변화 생기면 todoInfo date 속성 set
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                String year = String.valueOf(i);
                String month = String.valueOf(i1+1);
                String day = String.valueOf(i2);
                todoInfo.setDate(year + "-" + month + "-" + day);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("SharePlan");
        String lec_uid = intent.getStringExtra("lecUid");

        button = findViewById(R.id.add_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText.getText().toString();
                todoInfo.setTitle(title);

                // 일정 정보를 모두 입력하였을 경우
                if(!todoInfo.getTitle().equals("") && !todoInfo.getType().equals("") && !todoInfo.getDate().equals("")){
                    databaseReference.child("TodoInfo").child(lec_uid).child(todoInfo.getDate()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            arrayList = new ArrayList<>();
                            for(DataSnapshot tododata : snapshot.getChildren()){
                                arrayList.add(tododata.getValue(TodoInfo.class));
                            }
                            arrayList.add(todoInfo);
                            databaseReference.child("TodoInfo").child(lec_uid).child(todoInfo.getDate()).setValue(arrayList);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {    // 입력하지 않은 일정 정보가 있을 경우
                    Toast.makeText(getApplicationContext(), "모든 정보를 입력해 주세요", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}