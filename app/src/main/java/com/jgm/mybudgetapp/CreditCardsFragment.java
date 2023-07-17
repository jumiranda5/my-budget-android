package com.jgm.mybudgetapp;

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
import android.widget.Button;
import android.widget.ImageButton;

import com.jgm.mybudgetapp.adapters.CardAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCreditCardsBinding;
import com.jgm.mybudgetapp.room.AppDatabase;
import com.jgm.mybudgetapp.room.dao.CardDao;
import com.jgm.mybudgetapp.room.entity.CreditCard;
import com.jgm.mybudgetapp.utils.Tags;

import java.util.ArrayList;
import java.util.List;

public class CreditCardsFragment extends Fragment {

    public CreditCardsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-credit-cards";

    // List
    private List<CreditCard> cardsList = new ArrayList<>();
    CardAdapter adapter;

    // UI
    private FragmentCreditCardsBinding binding;
    private ImageButton mAddCard;
    private RecyclerView mRecyclerView;

    private void setBinding() {
        mAddCard = binding.buttonAddCard;
        mRecyclerView = binding.cardsList;
    }

    // Interfaces
    private Context mContext;
    private MainInterface mInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(Tags.LOG_LIFECYCLE, "Credit Cards onAttach");
        mContext = context;
        mInterface = (MainInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(Tags.LOG_LIFECYCLE, "Credit Cards onCreateView");
        binding = FragmentCreditCardsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Tags.LOG_LIFECYCLE, "Credit Cards onViewCreated");

        mAddCard.setOnClickListener(v -> mInterface.openCardForm(false, null, 0));

        Handler handler = new Handler(Looper.getMainLooper());
        AppDatabase.dbExecutor.execute(() -> {

            CardDao cardDao = AppDatabase.getDatabase(mContext).CardDao();
            cardsList = cardDao.getCreditCards();

            handler.post(() -> {
                Log.d(Tags.LOG_DB, "Done reading all credit cards from db: " + cardsList.size());
                initRecyclerView();
            });
        });

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void updateUiAfterInsertion(CreditCard card) {
        adapter.addItem(card);
    }

    public void updateListAfterDelete(int pos) {
        Log.d(LOG, "Update ui list after item delete");
        adapter.deleteItem(pos);
    }

    public void updateListAfterEdit(int pos, CreditCard editedCard) {
        Log.d(LOG, "Update ui list after item edit");
        adapter.updateItem(pos, editedCard);
    }


    /* ===============================================================================
                                        LIST
     =============================================================================== */

    private void initRecyclerView() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        adapter = new CardAdapter(mContext, cardsList);
        mRecyclerView.setAdapter(adapter);
    }

}