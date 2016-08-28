package org.hello.activity;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.hello.MyService;
import org.hello.R;
import org.hello.entity.Task;
import org.hello.entity.dto.TasksPageDto;
import org.hello.utils.RestUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment implements RefreshableFragment {

    private MyService service = RestUtils.getService();

    private Context context;
    private ListView tasksListView;

    private FragmentDataLoadingListener listener;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        context = view.getContext();

        if (tasksListView == null) {
            tasksListView = (ListView) view.findViewById(R.id.tasks_list_view);
            ArrayAdapter<Task> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            tasksListView.setAdapter(adapter);
        }
        updateTasks();

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

    @Override
    public void refreshFragmentData() {
        updateTasks();
    }

    private void updateTasks() {
        Call<TasksPageDto> tasksPageDtoCall = service.getTasks();
        tasksPageDtoCall.enqueue(new Callback<TasksPageDto>() {
            @Override
            public void onResponse(Call<TasksPageDto> call, Response<TasksPageDto> response) {
                putDataToList(response.body().getTasks());
                listener.onDataLoaded();
            }

            @Override
            public void onFailure(Call<TasksPageDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                listener.onError(message);
            }
        });
    }

    private void putDataToList(List<Task> tasks) {
        ArrayAdapter<Task> adapter = (ArrayAdapter<Task>) tasksListView.getAdapter();
        adapter.clear();
        adapter.addAll(tasks);
        adapter.notifyDataSetChanged();
    }
}
