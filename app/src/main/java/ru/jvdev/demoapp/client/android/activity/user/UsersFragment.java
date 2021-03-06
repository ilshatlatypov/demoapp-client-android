package ru.jvdev.demoapp.client.android.activity.user;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.activity.Refreshable;
import ru.jvdev.demoapp.client.android.activity.Searchable;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UsersPageDto;

import static android.app.Activity.RESULT_OK;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityRequestCode.CREATE;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityRequestCode.DETAILS;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.DELETED;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.ID;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;
import static ru.jvdev.demoapp.client.android.utils.SnackbarCustom.LENGTH_SHORT;

public class UsersFragment extends Fragment implements Refreshable, Searchable {

    private Api.Users usersApi;

    private ListView usersListView;
    private List<User> allUsers;

    private ViewSwitcher viewSwitcher;

    public UsersFragment() {
        // Required empty public constructor
    }

    public static UsersFragment newInstance() {
        return new UsersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateActivity();
            }
        });

        usersListView = (ListView) view.findViewById(R.id.users_list_view);
        ArrayAdapter<User> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        adapter.add(new User());
        usersListView.setAdapter(adapter);
        usersListView.setEmptyView(view.findViewById(android.R.id.empty));
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) parent.getItemAtPosition(position);
                openDetailsActivity(user);
            }
        });

        viewSwitcher = new ViewSwitcher(getActivity(), view, R.id.progress_bar, R.id.users_list_view, R.id.error_layout);
        Button retryButton = (Button) view.findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsers();
            }
        });

        usersApi = rest(getActivity()).getUsersApi();

        updateUsers();

        return view;
    }

    private void openCreateActivity() {
        Intent intent = new Intent(getActivity(), UserEditActivity.class);
        this.startActivityForResult(intent, CREATE);
    }

    private void openDetailsActivity(User user) {
        Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
        intent.putExtra(ID, user.getId());
        this.startActivityForResult(intent, DETAILS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(usersListView, R.string.prompt_user_saved, LENGTH_SHORT).show();
                updateUsers();
            }
        } else if (requestCode == DETAILS) {
            if (resultCode == DELETED) {
                Snackbar.make(usersListView, R.string.prompt_user_deleted, LENGTH_SHORT).show();
                updateUsers();
            } else if (resultCode == NEED_PARENT_REFRESH) {
                updateUsers();
            }
        }
    }

    @Override
    public void refreshData() {
        updateUsers();
    }

    @Override
    public void applySearch(final String searchString) {
        if (allUsers == null) {
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if (StringUtils.startsWithIgnoreCase(u.getFirstname(), searchString)
                    || StringUtils.startsWithIgnoreCase(u.getLastname(), searchString)) {
                filtered.add(u);
            }
        }
        putDataToList(filtered);
    }

    private void updateUsers() {
        viewSwitcher.showProgressBar();

        Call<UsersPageDto> pageCall = usersApi.getUsers();
        pageCall.enqueue(new Callback<UsersPageDto>() {
            @Override
            public void onResponse(Call<UsersPageDto> call, Response<UsersPageDto> response) {
                allUsers = response.body().getUsers();
                putDataToList(allUsers);
                viewSwitcher.showMainLayout();
            }

            @Override
            public void onFailure(Call<UsersPageDto> call, Throwable t) {
                allUsers = null;
                String message = requestFailureMessage(getActivity(), t);
                showError(message);
            }
        });
    }

    private void putDataToList(List<User> users) {
        ArrayAdapter<User> adapter = (ArrayAdapter<User>) usersListView.getAdapter();
        adapter.clear();
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }

    private void showError(String errorMessage) {
        TextView errorTextView = (TextView) getActivity().findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }
}
