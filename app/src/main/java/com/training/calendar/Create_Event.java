package com.training.calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Create_Event extends AppCompatActivity {


    private DatePickerDialog datePickerDialog;
    private Button StartDate , EndDate ,StartTime , EndTime; // these at first will have today's date until changed by user
    private String date;
    private boolean Caller; // this is set by the clicked button
    private int hour , minute;
    private EditText eventTitle;// this will have the name of the event entered by the user
    private EditText eventDecs;
    private SwitchCompat dateSwitch;
    private List<CategoryData> categoryDataList = new ArrayList<>();
    private AppDatabase AppDB;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems; //for category drop-down list
    private String Category;
    private boolean hasDate;
    private long startD;
    private long endD;
    private int sDay = -1 ,sMonth ,sYear, eDay = -1 ,eMonth ,eYear;
    private long sTime , eTime;
    private CardView sCard ,eCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        initAllViews();
        initSwitchListener();
        initDatePicker();
        initDropDownList();

    }

    public String getDate() {
        return date;
    }
    public String getEventTitle() {
        return String.valueOf(eventTitle.getText());
    }
    public String getEventDesc() {
        return String.valueOf(eventDecs.getText());
    }
    private void initDropDownList() {
        // initialize DB
        AppDB = AppDatabase.getDbInstance(this);
        // store DB value in data list
        categoryDataList = AppDB.categoryDao().getAllC();

        String[] items = new String[categoryDataList.size()];
        int i =0;
        for (Object value: categoryDataList) {
            CategoryData data = categoryDataList.get(i);
            items[i] =  data.getTitle();
            i++;
        }
        adapterItems = new ArrayAdapter<String>(this,R.layout.category_list_item,items);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                 Category = adapterView.getItemAtPosition(position).toString(); // store selection in item


            }
        });

    }
    private void initAllViews() {
        StartTime = findViewById(R.id.StartTimePicker);
        EndTime = findViewById(R.id.EndTimePicker);
        eventTitle = findViewById(R.id.TitleEdit);
        eventDecs = findViewById(R.id.taskDesc);
        StartDate = findViewById(R.id.CategoryColorPicker);
        EndDate = findViewById(R.id.EndDatePicker);
        StartDate.setText(getTodaysDate());
        EndDate.setText(getTodaysDate());
        dateSwitch = findViewById(R.id.DateSwitch);
        autoCompleteTextView = findViewById(R.id.auto_complete_text);
        sCard =findViewById(R.id.StartCard);
        eCard =findViewById(R.id.EndCard);
//         default state
        sCard.setVisibility(View.INVISIBLE);
        eCard.setVisibility(View.INVISIBLE);

    }
    private void initSwitchListener() {
//
        dateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean Checked) {
                if (Checked){
                    hasDate = true;
                    sCard.setVisibility(View.VISIBLE);
                    eCard.setVisibility(View.VISIBLE);
                }else{
                    hasDate = false;
                    sCard.setVisibility(View.INVISIBLE);
                    eCard.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR); // this will help us set default value to Today's Date
        int month = cal.get(Calendar.MONTH);
        month += 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day , month , year);
    }
    private void initDatePicker() {

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                date = makeDateString(day,month+1,year);
                if (Caller){
                    StartDate.setText(date);
                    //startD = LocalDate.of(year , month,day);

                    cal.set(Calendar.YEAR , year); // this will help us set default value to Today's Date
                    cal.set(Calendar.MONTH , month);
                    cal.set(Calendar.DAY_OF_MONTH , day);
                    cal.set(Calendar.HOUR_OF_DAY ,0);
                    cal.set(Calendar.MINUTE , 0);
                    cal.set(Calendar.SECOND , 0);
                    startD = cal.getTimeInMillis();

                    sDay = day; sMonth = month; sYear = year;
                }else {
                    EndDate.setText(date);
                   // endD = LocalDate.of(year , month ,day);

                    cal.set(Calendar.YEAR , year); // this will help us set default value to Today's Date
                    cal.set(Calendar.MONTH , month);
                    cal.set(Calendar.DAY_OF_MONTH , day);
                    cal.set(Calendar.HOUR_OF_DAY ,23);
                    cal.set(Calendar.MINUTE , 59);
                    cal.set(Calendar.SECOND , 59);
                    endD = cal.getTimeInMillis();
                    eDay = day; eMonth = month; eYear = year;
                }
            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR); // this will help us set default value to Today's Date
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this,style,dateSetListener ,year ,month ,day ); // initializing the dialog
    }
    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }
    private String getMonthFormat(int month) { // this method is to get the "String" value instead of Digital
        if (month == 1) return "JAN";
        if (month == 2) return "FEB";
        if (month == 3) return "MAR";
        if (month == 4) return "APR";
        if (month == 5) return "MAY";
        if (month == 6) return "JUN";
        if (month == 7) return "JUL";
        if (month == 8) return "AUG";
        if (month == 9) return "SEP";
        if (month == 10) return "OCT";
        if (month == 11) return "NOV";
        if (month == 12) return "DEC";
        //Default should never be reached
        return "JAN";
    }
    public  void  openDatePicker(View view){
        if (view.equals(StartDate)){ // this will excute if the user clicks starts date picker
            Caller = true;
            datePickerDialog.show();
        }
        if (view.equals(EndDate)){// this will excute if the user clicks Ends date picker
            Caller = false;
            datePickerDialog.show();
        }

    }
    public void popTimePicker(View view){
        if (sDay==-1){
            // instruct user to pick date first
            Toast toast = Toast.makeText(getApplicationContext() ,"Please Enter a Date first",Toast.LENGTH_SHORT);
            toast.show();
        }else{
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour; minute = selectedMinute;
                if (view.equals(StartTime)){
                    StartTime.setText(String.format(Locale.getDefault(), "%02d:%02d",hour ,minute));

                        Calendar cal =Calendar.getInstance();
                        cal.set(Calendar.YEAR ,sYear); cal.set(Calendar.MONTH ,sMonth); cal.set(Calendar.DAY_OF_MONTH ,sDay); cal.set(Calendar.HOUR_OF_DAY ,selectedHour); cal.set(Calendar.MINUTE ,selectedMinute);
                        sTime = cal.getTimeInMillis();
                }
                if (view.equals(EndTime)){
                    EndTime.setText(String.format(Locale.getDefault(), "%02d:%02d",hour ,minute));

                        Calendar cal =Calendar.getInstance();
                        cal.set(Calendar.YEAR ,eYear); cal.set(Calendar.MONTH ,eMonth); cal.set(Calendar.DAY_OF_MONTH ,eDay); cal.set(Calendar.HOUR_OF_DAY ,selectedHour); cal.set(Calendar.MINUTE ,selectedMinute);
                        eTime = cal.getTimeInMillis();

                }
            }
        };
        int style = AlertDialog.THEME_HOLO_LIGHT; // to change the style of the dialog plug in this style as 2nd parameter in the following method

        TimePickerDialog timePickerDialog = new TimePickerDialog(this , onTimeSetListener , hour ,minute , true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();}
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void SaveButton(View view) { // this method is empty at the moment the plan is to make it save data to DB and go back to main page
        AppDatabase db = AppDatabase.getDbInstance(this.getApplicationContext());
        User user = new User();
        user.taskName = getEventTitle().toString();
        user.description = getEventDesc().toString();
        user.cat = Category;
        user.date = getDate();
        user.setDone(false);

//        user.setStartDate(StartDate.getText().toString()); // this  store String in DB
//        user.setEndDate(EndDate.getText().toString()); // this  store String in DB
//        user.setStartTime(StartTime.getText().toString());// this  store String in DB
//        user.setEndTime(EndTime.getText().toString());// this  store String in DB

        if(hasDate){
            user.setStartDate(startD-1); // this will store long in DB
            user.setEndDate(endD+2); // this will store long in DB
            user.setStartTime(sTime); // this will store long in DB ,, this will point to the exact minute the event start
            user.setEndTime(eTime); // this will store long in DB ,, this will point to the exact minute the event end
            user.setTaskDay(-1);
        }else{
            user.setStartDate(-1); // this will store long in DB
            user.setEndDate(-1); // this will store long in DB
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            user.setTaskDay(timestamp.getTime()); // this will add new tasks automatically to user's "myDay"
            user.setStartTime(sTime); // this will store long in DB
            user.setEndTime(eTime); // this will store long in DB
        }


        user.setHasDate(hasDate);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        user.setCreateTime(timestamp.getTime());
        db.userDao().insertAll(user);
        finish();

    }
}