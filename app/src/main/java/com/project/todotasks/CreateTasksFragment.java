package com.project.todotasks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

public class CreateTasksFragment extends DialogFragment {

    private EditText taskTitle;
    private String title;
    private String selectedDate;
    private LocalTime selectedTime;
    private Button btnDate;
    private Button btnTime;
    private TaskList taskToEdit;

    public interface TaskDialogListener{
        void onTaskAdded(TaskList tasks);
    }
    private TaskDialogListener listener;

    // connect the listener to the context
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TaskDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " implement TaskDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_create_tasks, null);

        // get ui elements
        taskTitle = view.findViewById(R.id.taskTitle);
        btnDate = view.findViewById(R.id.btnEditDate);
        btnTime = view.findViewById(R.id.btnEditTime);

        btnDate.setOnClickListener(v ->showDatePicker(btnDate));
        btnTime.setOnClickListener(v -> showTimePicker(btnTime));

        builder.setView(view)
                .setTitle("Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    title = taskTitle.getText().toString().trim();
                    String time = selectedTime.toString();
                    if (!title.isEmpty()){
                        TaskList newTask = new TaskList(title, selectedDate, time);
                        listener.onTaskAdded(newTask);
                    } else {
                        Toast.makeText(requireContext(), "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
        return  builder.create();
    }

    private void showTimePicker(Button button) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("Set Time")
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();
        timePicker.addOnPositiveButtonClickListener(view -> {
           // get time
           int hour = timePicker.getHour();
           int minute = timePicker.getMinute();

           // format for hour:mins:sec
            selectedTime = LocalTime.of(hour, minute);
            Log.d("TimePicker", "Selected LocalTime: " + selectedTime.toString());
            button.setText(selectedTime.toString());
        });
        timePicker.show(requireActivity().getSupportFragmentManager(), "tag");
    }

    private void showDatePicker(Button button) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Set Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
           SimpleDateFormat sft_date = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
           selectedDate = sft_date.format(new Date(selection));
           button.setText(selectedDate);
        });
        datePicker.show(requireActivity().getSupportFragmentManager(), "tag");
    }

}