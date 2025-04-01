package com.project.todotasks;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CreateTasksDialogFragment extends DialogFragment {

    private EditText taskTitle;

    private String title;
    private String selectedDate = "", selectedTime = "";

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
        Button btnDate = view.findViewById(R.id.btnEditDate);
        Button btnTime = view.findViewById(R.id.btnEditTime);

        btnDate.setOnClickListener(v ->showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        builder.setView(view)
                .setTitle("Create Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    title = taskTitle.getText().toString().trim();
                    TaskList newTask = new TaskList(title, selectedDate, selectedTime);
                    listener.onTaskAdded(newTask);
                    Toast.makeText(requireContext(), title+selectedTime+selectedDate, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
        return  builder.create();
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("Set Time")
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();
        timePicker.addOnPositiveButtonClickListener(view -> selectedTime = MessageFormat.format("{0}:{1}",
                String.format(Locale.getDefault(), "%02d", timePicker.getHour()),
                String.format(Locale.getDefault(), "%02d", timePicker.getMinute())
                ));
        timePicker.show(requireActivity().getSupportFragmentManager(), "tag");
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Set Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> selectedDate = new SimpleDateFormat("MM-dd-yy", Locale.getDefault()).format(new Date(selection)));
        datePicker.show(requireActivity().getSupportFragmentManager(), "tag");
    }

}