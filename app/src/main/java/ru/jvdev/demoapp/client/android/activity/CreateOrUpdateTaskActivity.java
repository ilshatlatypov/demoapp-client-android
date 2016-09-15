package ru.jvdev.demoapp.client.android.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.net.ConnectException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

public class CreateOrUpdateTaskActivity extends AppCompatActivity {

    private static DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

    private Api.Tasks tasksApi;
    private LinearLayout baseLayout;

    private int taskId;
    private EditText titleText;
    private EditText dateText;
//    private Spinner userSpinner;

    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_update_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseLayout = (LinearLayout) findViewById(R.id.base_layout);

        titleText = (EditText) findViewById(R.id.title_text);

        Task task = (Task) getIntent().getSerializableExtra(UserDetailsActivity.EXTRA_USER);
        if (task != null) {
            taskId = task.getId();
            // fillFieldsWithData(task);
            getSupportActionBar().setTitle(R.string.title_edit_task);
        } else {
            getSupportActionBar().setTitle(R.string.title_new_task);
        }

        date = Calendar.getInstance().getTime();
        dateText = (EditText) findViewById(R.id.date_text);
        dateText.setText(df.format(date));
        dateText.setKeyListener(null); // do not show keyboard on focus
        dateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    KeyboardUtils.hideKeyboard(CreateOrUpdateTaskActivity.this);
                    showDatePicker();
                }
            }
        });

//        userSpinner = (Spinner) findViewById(R.id.user_spinner);
//        userSpinner = (Spinner) findViewById(R.id.user_spinner);
//        ArrayAdapter<String> spinnerAdapter = new SpinnerWithChooseItemArrayAdapter<>(this, R.layout.spinner_item);
//        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        userSpinner.setAdapter(spinnerAdapter);

        DemoApp app = (DemoApp) getApplicationContext();
        tasksApi = app.getRestProvider().getTasksApi();

        // updateUsers();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);

                date = c.getTime();
                dateText.setText(df.format(date));
            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(c.getTimeInMillis());
        datePicker.show();
    }

//    private void updateUsers() {
//        DemoApp app = (DemoApp) getApplicationContext();
//        Api.Users usersApi = app.getRestProvider().getUsersApi();
//
//        Call<UsersPageDto> usersPageDtoCall = usersApi.getUsers();
//        usersPageDtoCall.enqueue(new Callback<UsersPageDto>() {
//            @Override
//            public void onResponse(Call<UsersPageDto> call, Response<UsersPageDto> response) {
//                putDataToList(response.body().getUsers());
//                //listener.onDataLoaded();
//            }
//
//            @Override
//            public void onFailure(Call<UsersPageDto> call, Throwable t) {
//                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
//                        getString(R.string.error_server_unavailable) :
//                        getString(R.string.error_unknown, t.getMessage());
//                //listener.onError(message);
//            }
//        });
//    }
//
//    private void putDataToList(List<User> users) {
//        ArrayAdapter<User> adapter = (ArrayAdapter<User>) userSpinner.getAdapter();
//        adapter.clear();
//        adapter.add(new User("Сотрудник", "", "", "", Role.NO_ROLE));
//        adapter.addAll(users);
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_save) {
            if (taskId == 0) {
                attemptCreateTask();
            } else {
                // attemptUpdateTask();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptCreateTask() {
        clearFieldErrorsIfAny();

        Task task = initTask();
        Map<Integer, String> errors = validate(task);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendCreateTaskRequest(task);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendCreateTaskRequest(Task task) {
        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> createUserCall = tasksApi.create(new TaskDto(task));
        createUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showRequestError(message);
            }
        });
    }

//    private void attemptUpdateTask() {
//        clearFieldErrorsIfAny();
//
//        Task task = initTask();
//        Map<Integer, String> errors = validate(task);
//
//        if (errors.isEmpty()) {
//            KeyboardUtils.hideKeyboard(this);
//            sendUpdateTaskRequest(taskId, task);
//        } else {
//            displayFieldErrors(errors);
//        }
//    }
//
//    private void sendUpdateUserRequest(int taskId, Task task) {
//        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();
//
//        Call<Void> updateTaskCall = tasksApi.update(taskId, new TaskDto(task));
//        updateTaskCall.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    setResult(RESULT_OK, new Intent());
//                    finish();
//                }
//                // FIXME: 29.08.16 PUT to non-existing ID creates new instance instead of NOT FOUND
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
//                        getString(R.string.error_server_unavailable) :
//                        getString(R.string.error_unknown, t.getMessage());
//                showRequestError(message);
//            }
//        });
//    }

    private void clearFieldErrorsIfAny() {
        for(int i = 0; i < baseLayout.getChildCount(); i++ ) {
            if (baseLayout.getChildAt(i) instanceof TextInputLayout) {
                ((TextInputLayout) baseLayout.getChildAt(i)).setError(null);
            }
        }
    }

    private Task initTask() {
        String title = titleText.getText().toString();
        return new Task(title, date);
    }

    private Map<Integer, String> validate(Task task) {
        Map<Integer, String> errors = new LinkedHashMap<>();

        if (TextUtils.isEmpty(task.getTitle())) {
            errors.put(R.id.title_layout, getString(R.string.error_field_required));
        }

        return errors;
    }

    private void displayFieldErrors(Map<Integer, String> errors) {
        boolean focusSet = false;
        for (Integer textLayoutId : errors.keySet()) {
            String errorMessage = errors.get(textLayoutId);
            TextInputLayout layout = ((TextInputLayout) findViewById(textLayoutId));
            layout.setError(errorMessage);
            if (!focusSet) { // set focus to first EditText with error
                layout.getChildAt(0).requestFocus();
                focusSet = true;
            }
        }
    }

    private void showRequestError(String errorMessage) {
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptCreateTask();
                    }
                }).show();
    }
}
