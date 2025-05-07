package com.project.todotasks;
import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateTasksFragment extends DialogFragment {

    private TextInputEditText taskTitle;
    private Button btnDate, btnTime, btnSave, btnCancle, btnDelete;
    private String title;
    private String selectedDate; // Stores "yyyy-MM-dd"
    private String selectedTime; // Stores "HH:mm"
    private TaskList taskToEdit = null;
    private int editPosition = -1;

    private static final String BUTTON_TEXT_SELECT_DATE = "Select Date";
    private static final String BUTTON_TEXT_SELECT_TIME = "Select Time";
    private static final String DIALOG_TITLE_ADD = "Add Task";
    private static final String DIALOG_TITLE_EDIT = "Edit Task";
    private static final String PICKER_TITLE_DATE = "Set Date";
    private static final String PICKER_TITLE_TIME = "Set Time";

    public void setTaskToEdit(TaskList task, int position) {
        this.taskToEdit = task;
        this.editPosition = position;
    }

    public interface TaskDialogListener {
        void onTaskAdded(TaskList tasks);
        void onTaskUpdated(TaskList newTask, int editPosition);
        void onTaskDeleted(int editPosition);
    }
    private TaskDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TaskDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement TaskDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_create_tasks, null);
        // Get UI elements
        taskTitle = view.findViewById(R.id.taskTitle);
        btnDate = view.findViewById(R.id.btnEditDate);
        btnTime = view.findViewById(R.id.btnEditTime);
        btnSave = view.findViewById(R.id.btnSaveTask);
        btnCancle = view.findViewById(R.id.btnCancelTask);
        btnDelete = view.findViewById(R.id.btnDeleteTask);
        btnDate.setOnClickListener(v -> showDatePicker(btnDate));
        btnTime.setOnClickListener(v -> showTimePicker(btnTime));


        // Condition for edit task
        if (taskToEdit != null) {
            taskTitle.setText(taskToEdit.getTaskTitle());
            selectedDate = taskToEdit.getTaskDateString();
            selectedTime = taskToEdit.getTaskTimeString();
            btnDate.setText(selectedDate != null ? selectedDate : BUTTON_TEXT_SELECT_DATE);
            btnTime.setText(selectedTime != null ? selectedTime : BUTTON_TEXT_SELECT_TIME);
        } else {
            // Set default text for buttons using hardcoded strings
            btnDate.setText(BUTTON_TEXT_SELECT_DATE);
            btnTime.setText(BUTTON_TEXT_SELECT_TIME);
        }

        btnSave.setOnClickListener(v -> {
            title = taskTitle.getText() != null ? taskTitle.getText().toString().trim() : "";

            // Basic validation
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                return; // Don't proceed
            }
            if (selectedDate == null || selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return; // Don't proceed
            }
            if (selectedTime == null || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return; // Don't proceed
            }
            TaskList newTask = new TaskList(title, selectedDate, selectedTime);

            // Check if we're editing or adding
            if (taskToEdit != null && editPosition != -1) {
                listener.onTaskUpdated(newTask, editPosition); // Update
            } else {
                listener.onTaskAdded(newTask); // Add
            }
            dismiss();
        });

        btnCancle.setOnClickListener(v -> {
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (editPosition != -1){
                // show confirmation before deleting
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to COMPLETELY DELETE this task?")
                        .setPositiveButton("Yes", (dialog, which) ->{
                            listener.onTaskDeleted(editPosition);
                            dismiss();
                        })
                        .setNegativeButton("Cancle", null)
                        .show();
            }
        });

        // create builder
        builder.setView(view).
                setTitle(taskToEdit == null ? DIALOG_TITLE_ADD : DIALOG_TITLE_EDIT);
        return builder.create();

    }

    private void showTimePicker(Button button) {
        // Determine initial hour/minute
        LocalTime currentTime = LocalTime.now();
        int initialHour = currentTime.getHour();
        int initialMinute = currentTime.getMinute();
        if (selectedTime != null && selectedTime.contains(":")) {
            try {
                String[] parts = selectedTime.split(":");
                initialHour = Integer.parseInt(parts[0]);
                initialMinute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                Log.e("TimePicker", "Error parsing existing time: " + selectedTime, e);
            }
        }

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTitleText(PICKER_TITLE_TIME) // Use hardcoded string
                .setTimeFormat(TimeFormat.CLOCK_12H) // Or CLOCK_24H
                .setHour(initialHour).setMinute(initialMinute).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build();

        timePicker.addOnPositiveButtonClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Format hour and minute to HH:mm
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Log.d("TimePicker", "Selected Time: " + selectedTime);
            button.setText(formatDisplayTime(hour, minute));
        });
        timePicker.show(getParentFragmentManager(), "TimePicker");
    }

    private void showDatePicker(Button button) {
        // Determine initial selection
        long initialSelection = MaterialDatePicker.todayInUtcMilliseconds();
        if (selectedDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(selectedDate);
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    initialSelection = cal.getTimeInMillis();
                }
            } catch (Exception e) {
                Log.e("DatePicker", "Error parsing existing date: " + selectedDate, e);
            }
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(PICKER_TITLE_DATE) //
                .setSelection(initialSelection).build();


        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sft_date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sft_date.format(new Date(selection));
            Log.d("DatePicker", "Selected Date: " + selectedDate);
            button.setText(selectedDate);

        });
        datePicker.show(getParentFragmentManager(), "DatePicker");
    }


    // display time in 12-hour format with AM/PM
    private String formatDisplayTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        return sdfDisplay.format(calendar.getTime());

    }

}