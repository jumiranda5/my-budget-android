package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jgm.mybudgetapp.adapters.CardAdapter;
import com.jgm.mybudgetapp.databinding.FragmentCreditCardsBinding;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.utils.NumberUtils;

import java.util.ArrayList;

public class CreditCardsFragment extends Fragment {

    public CreditCardsFragment() {
        // Required empty public constructor
    }

    private static final String LOG = "debug-credit-cards";

    // List
    private ArrayList<Card> cardsList = new ArrayList<>();
    CardAdapter adapter;

    // UI
    private FragmentCreditCardsBinding binding;
    private Button mAddCard;
    private TextView mTotal;
    private RecyclerView mRecyclerView;

    private void setBinding() {
        mAddCard = binding.buttonAddCreditCard;
        mTotal = binding.creditCardsTotal;
        mRecyclerView = binding.cardsList;
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
        binding = FragmentCreditCardsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddCard.setOnClickListener(v -> mInterface.openCardForm(false, null, 0));

        initRecyclerView();
        mInterface.getCreditCardsData();
        setTotal();

    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void updateListAfterDbRead(ArrayList<Card> dbCards) {
        Log.d(LOG, "Update ui list: " + dbCards.size());
        cardsList = dbCards;
        initRecyclerView();
    }

    public void updateUiAfterInsertion(Card card) {
        adapter.addItem(card);
    }

    public void updateListAfterDelete(int pos) {
        Log.d("debug-cards", "Update ui list after item delete");
        adapter.deleteItem(pos);
    }

    public void updateListAfterEdit(int pos, Card editedCard) {
        Log.d("debug-cards", "Update ui list after item edit");
        adapter.updateItem(pos, editedCard);
    }


    /* ===============================================================================
                                         TOTAL
     =============================================================================== */

    private void setTotal() {
        float total = 0.0f;
        for (int i = 0; i < cardsList.size(); i++) {
            total = total + cardsList.get(i).getTotal();
        }
        String[] currencyFormat = NumberUtils.getCurrencyFormat(mContext, total);
        String formattedValue = currencyFormat[0] + currencyFormat[1];
        mTotal.setText(formattedValue);
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