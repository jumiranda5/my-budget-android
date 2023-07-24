package com.jgm.mybudgetapp.fragmentsMain;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.adapters.PendingAdapter;
import com.jgm.mybudgetapp.databinding.FragmentPendingBinding;
import com.jgm.mybudgetapp.objects.MyDate;
import com.jgm.mybudgetapp.objects.TransactionResponse;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.utils.MyDateUtils;

import java.util.List;

public class PendingFragment extends Fragment {

    public PendingFragment() {
        // Required empty public constructor
    }

    // UI
    private FragmentPendingBinding binding;
    private RecyclerView mRecyclerView;
    private ImageButton mClose;

    private void setBinding() {
        mRecyclerView = binding.pendingList;
        mClose = binding.pendingToolbarClose;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPendingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mClose.setOnClickListener(v -> mInterface.navigateBack());
        getPendingData();

    }

    private void getPendingData() {
        AppDatabase db = AppDatabase.getDatabase(mContext);
        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            MyDate today = MyDateUtils.getCurrentDate(mContext);
            List<TransactionResponse> response =
                    db.TransactionDao().getPendingList(today.getDay(), today.getMonth(), today.getYear());

            handler.post(() -> {
                initRecyclerView(response);
                for (int i = 0; i < response.size(); i++) {
                    TransactionResponse t = response.get(i);
                    Log.d("debug-pending", t.getCategoryName() + " | " + t.getAmount() +
                            " | " + t.getDay() + "/" + t.getMonth() + "/" + t.getYear());
                }
            });

        });

    }

    private void initRecyclerView(List<TransactionResponse> response) {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        PendingAdapter adapter = new PendingAdapter(mContext, response);
        mRecyclerView.setAdapter(adapter);
    }
}