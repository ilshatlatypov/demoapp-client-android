package ru.jvdev.demoapp.client.android.activity;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UsersPageDto;

import static android.app.Activity.RESULT_OK;
import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.CREATE;
import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.DETAILS;
import static ru.jvdev.demoapp.client.android.utils.ActivityResultCode.DELETED;
import static ru.jvdev.demoapp.client.android.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.tryCastAsDataLoadingListener;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.ID;

public class UsersFragment extends Fragment implements RefreshableFragment {

    private Api.Users usersApi;

    private ListView usersListView;
    private DataLoadingListener listener;

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
        usersListView.setAdapter(adapter);
        usersListView.setEmptyView(view.findViewById(android.R.id.empty));
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) parent.getItemAtPosition(position);
                openDetailsActivity(user);
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
                Snackbar.make(usersListView, R.string.prompt_user_saved, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            }
        } else if (requestCode == DETAILS) {
            if (resultCode == DELETED) {
                Snackbar.make(usersListView, R.string.prompt_user_deleted, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            } else if (resultCode == NEED_PARENT_REFRESH) {
                updateUsers();
            }
        }
    }

    @Override
    public void refreshFragmentData() {
        updateUsers();
    }

    private void updateUsers() {
        Call<UsersPageDto> pageCall = usersApi.getUsers();
        pageCall.enqueue(new Callback<UsersPageDto>() {
            @Override
            public void onResponse(Call<UsersPageDto> call, Response<UsersPageDto> response) {
                putDataToList(response.body().getUsers());
                listener.onDataLoaded();
            }

            @Override
            public void onFailure(Call<UsersPageDto> call, Throwable t) {
                String message = requestFailureMessage(getActivity(), t);
                listener.onError(message);
            }
        });
    }

    private void putDataToList(List<User> users) {
        ArrayAdapter<User> adapter = (ArrayAdapter<User>) usersListView.getAdapter();
        adapter.clear();
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }

    //region Fragment-Activity attach-detach
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = tryCastAsDataLoadingListener(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = tryCastAsDataLoadingListener(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    //endregion
}
