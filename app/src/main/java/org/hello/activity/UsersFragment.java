package org.hello.activity;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.hello.R;
import org.hello.entity.User;
import org.hello.utils.RestUtils;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    private Context context;
    private ListView usersListView;

    private FragmentDataLoadingListener listener;
    private UpdateUsersTask updateUsersTask;

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

        if (usersListView == null) {
            usersListView = (ListView) view.findViewById(R.id.users_list_view);
            ArrayAdapter<User> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            usersListView.setAdapter(adapter);
        }
        updateUsers();

        return view;
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

    private void updateUsers() {
        if (updateUsersTask != null) {
            return;
        }
        updateUsersTask = new UpdateUsersTask();
        updateUsersTask.execute();
    }

    private class UpdateUsersTask extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected List<User> doInBackground(Void... params) {
            try {
                return RestUtils.getUsersListRetrofit();
            } catch (IOException e) {
                listener.onError(ErrorType.SERVER_UNAVAILABLE);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<User> users) {
            updateUsersTask = null;
            if (users != null) {
                putDataToList(users);
                listener.onDataLoaded();
            }
        }
    }

    private void putDataToList(List<User> users) {
        ArrayAdapter<User> adapter = (ArrayAdapter<User>) usersListView.getAdapter();
        adapter.clear();
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }
}
