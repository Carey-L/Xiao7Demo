package com.example.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.view.MyCalendar;
import com.example.xiao7demo.BaseActivity;

import java.util.Date;

public class CalendarTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_test);
        MyCalendar calendar = findViewById(R.id.activty_my_calendar);
        calendar.updateMonth(new Date());
        calendar.setOnIClickListener(mills -> {
            Toast.makeText(this, MyCalendar.getMonthDayYear(new Date(mills)), Toast.LENGTH_SHORT).show();
        });
        TextView up = findViewById(R.id.up);
        up.setOnClickListener(view -> calendar.monthChange(-1));
        TextView down = findViewById(R.id.down);
        down.setOnClickListener(view -> calendar.monthChange(1));
    }


}