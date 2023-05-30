package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

    // List
    private final ArrayList<Card> cards = new ArrayList<>();

    // UI
    private FragmentCreditCardsBinding binding;
    private ImageButton mBack;
    private Button mAddCard;
    private TextView mTotal;
    private RecyclerView mRecyclerView;

    private void setBinding() {
        mBack = binding.cardsBackButton;
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

        mBack.setOnClickListener(v -> mInterface.navigateBack());
        mAddCard.setOnClickListener(v -> mInterface.openCardForm(false));

        initDummyList();
        setTotal();
        initRecyclerView();

    }

    private void setTotal() {
        float total = 0.0f;
        for (int i = 0; i < cards.size(); i++) {
            total = total + cards.get(i).getTotal();
        }
        String[] currencyFormat = NumberUtils.getCurrencyFormat(mContext, total);
        String formattedValue = currencyFormat[0] + currencyFormat[1];
        mTotal.setText(formattedValue);
    }

    private void initRecyclerView() {
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(listLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        CardAdapter adapter = new CardAdapter(mContext, cards);
        mRecyclerView.setAdapter(adapter);
    }

    private void initDummyList() {
        Card c1 = new Card(0, "Card 1", R.color.colorAccent, 8, true);
        Card c2 = new Card(0, "Card 2", R.color.expense, 14, true);
        Card c3 = new Card(0, "Card 3", R.color.savings, 21, true);

        c1.setTotal(1200.0f);
        c2.setTotal(850.5f);
        c3.setTotal(500.0f);

        cards.add(c1);
        cards.add(c2);
        cards.add(c3);
    }
}