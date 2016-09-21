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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UsersPageDto;
import ru.jvdev.demoapp.client.android.utils.ActivityResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment implements RefreshableFragment {

    private static final int CREATE_REQUEST = 1;
    private static final int DETAILS_REQUEST = 2;
    public static final String EXTRA_USER_ID = "user_id";

    private Context context;
    private ListView usersListView;

    private FragmentDataLoadingListener listener;

    public UsersFragment() {
        // Required empty public constructor
    }

    public static UsersFragment newInstance() {
        return new UsersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        context = view.getContext();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateUserActivity();
            }
        });


        usersListView = (ListView) view.findViewById(R.id.users_list_view);
        ArrayAdapter<User> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        usersListView.setAdapter(adapter);
        usersListView.setEmptyView(view.findViewById(android.R.id.empty));
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) parent.getItemAtPosition(position);
                openDetailsActivity(user);
            }
        });

        updateUsers();

        return view;
    }

    private void openCreateUserActivity() {
        Intent intent = new Intent(context, CreateOrUpdateUserActivity.class);
        this.startActivityForResult(intent, CREATE_REQUEST);
    }

    private void openDetailsActivity(User user) {
        Intent intent = new Intent(context, UserDetailsActivity.class);
        intent.putExtra(EXTRA_USER_ID, user.getId());
        this.startActivityForResult(intent, DETAILS_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(usersListView, R.string.prompt_user_saved, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            }
        } else if (requestCode == DETAILS_REQUEST) {
            if (resultCode == ActivityResult.DELETED) {
                Snackbar.make(usersListView, R.string.prompt_user_deleted, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            } else if (resultCode == ActivityResult.NEED_PARENT_REFRESH) {
                updateUsers();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentDataLoadingListener) {
            listener = (FragmentDataLoadingListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement FragmentDataLoadingListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentDataLoadingListener) {
            listener = (FragmentDataLoadingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentDataLoadingListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void refreshFragmentData() {
        updateUsers();
    }

    private void updateUsers() {
        DemoApp app = (DemoApp) getActivity().getApplicationContext();
        Api.Users usersApi = app.getRestProvider().getUsersApi();

        Call<UsersPageDto> usersPageDtoCall = usersApi.getUsers();
        usersPageDtoCall.enqueue(new Callback<UsersPageDto>() {
            @Override
            public void onResponse(Call<UsersPageDto> call, Response<UsersPageDto> response) {
                putDataToList(response.body().getUsers());
                listener.onDataLoaded();
            }

            @Override
            public void onFailure(Call<UsersPageDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
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
}
