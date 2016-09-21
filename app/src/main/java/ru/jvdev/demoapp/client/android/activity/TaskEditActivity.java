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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.entity.dto.UsersPageDto;
import ru.jvdev.demoapp.client.android.spinner.SpinnerWithChooseItemArrayAdapter;
import ru.jvdev.demoapp.client.android.spinner.SpinnerWithChooseItemListener;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

public class TaskEditActivity extends AppCompatActivity {

    private static DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

    private Api.Tasks tasksApi;
    private Api.Users usersApi;

    private LinearLayout baseLayout;

    private int taskId;
    private EditText titleText;
    private Date date;
    private EditText dateText;
    private Spinner userSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseLayout = (LinearLayout) findViewById(R.id.base_layout);

        titleText = (EditText) findViewById(R.id.title_text);

        dateText = (EditText) findViewById(R.id.date_text);
        dateText.setKeyListener(null); // do not show keyboard on focus
        dateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    KeyboardUtils.hideKeyboard(TaskEditActivity.this);
                    showDatePicker();
                }
            }
        });

        userSpinner = (Spinner) findViewById(R.id.user_spinner);
        ArrayAdapter<User> spinnerAdapter = new SpinnerWithChooseItemArrayAdapter<>(this, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
        userSpinner.setOnItemSelectedListener(new SpinnerWithChooseItemListener(this));

        DemoApp app = (DemoApp) getApplicationContext();
        tasksApi = app.getRestProvider().getTasksApi();
        usersApi = app.getRestProvider().getUsersApi();

        Task task = (Task) getIntent().getSerializableExtra(TaskDetailsActivity.EXTRA_TASK);
        if (task != null) {
            getSupportActionBar().setTitle(R.string.title_edit_task);
            taskId = task.getId();
            titleText.setText(task.getTitle());
            updateUsersInSpinner(task.getUser());
            setDate(task.getDate());
        } else {
            getSupportActionBar().setTitle(R.string.title_new_task);
            setDate(Calendar.getInstance().getTime());
            updateUsersInSpinner(null);
        }
    }

    private void setDate(Date date) {
        this.date = date;
        dateText.setText(df.format(date));
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime(date);
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                setDate(c.getTime());
            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(today.getTimeInMillis());
        datePicker.show();
    }

    private void updateUsersInSpinner(final User selectedUser) {
        Call<UsersPageDto> usersPageDtoCall = usersApi.getUsers();
        usersPageDtoCall.enqueue(new Callback<UsersPageDto>() {
            @Override
            public void onResponse(Call<UsersPageDto> call, Response<UsersPageDto> response) {
                putDataToSpinner(response.body().getUsers());
                if (selectedUser != null) {
                    int userPosition = getPosition(userSpinner, selectedUser.getId());
                    userSpinner.setSelection(userPosition);
                }
                //listener.onDataLoaded();
            }

            private int getPosition(Spinner spinner, int userId) {
                for (int i = 0; i < spinner.getCount(); i++) {
                    if (((User) spinner.getItemAtPosition(i)).getId() == userId) {
                        return i;
                    }
                }
                return -1;
            }

            @Override
            public void onFailure(Call<UsersPageDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                //listener.onError(message);
            }
        });
    }

    private void putDataToSpinner(List<User> users) {
        ArrayAdapter<User> adapter = (ArrayAdapter<User>) userSpinner.getAdapter();
        adapter.clear();
        adapter.add(new User("Сотрудник", "", "", "", Role.NO_ROLE));
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }

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
                attemptUpdateTask();
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

    private void attemptUpdateTask() {
        clearFieldErrorsIfAny();

        Task task = initTask();
        Map<Integer, String> errors = validate(task);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendUpdateTaskRequest(taskId, task);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendUpdateTaskRequest(int taskId, Task task) {
        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> updateTaskCall = tasksApi.update(taskId, new TaskDto(task));
        updateTaskCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                // FIXME: 29.08.16 PUT to non-existing ID creates new instance instead of NOT FOUND
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showRequestError(message);
            }
        });
    }

    private void clearFieldErrorsIfAny() {
        for(int i = 0; i < baseLayout.getChildCount(); i++ ) {
            if (baseLayout.getChildAt(i) instanceof TextInputLayout) {
                ((TextInputLayout) baseLayout.getChildAt(i)).setError(null);
            }
        }
    }

    private Task initTask() {
        String title = titleText.getText().toString();
        Task task = new Task(title, date);
        User user = (User) userSpinner.getSelectedItem();
        if (user.getId() > 0) {
            task.setUser(user);
        }
        return task;
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