package com.jgm.mybudgetapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jgm.mybudgetapp.databinding.FragmentCreditCardDetailsBinding;
import com.jgm.mybudgetapp.objects.Card;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.utils.ColorUtils;

public class CreditCardDetailsFragment extends Fragment {

    public CreditCardDetailsFragment() {
        // Required empty public constructor
    }

    // Vars
    private int position;
    private Card creditCard;

    // UI
    private FragmentCreditCardDetailsBinding binding;
    private ImageButton mBack, mEdit;
    private ImageView mCardIcon;
    private TextView mCardName;

    private void setBinding() {
        mBack = binding.cardDetailsBackButton;
        mEdit = binding.cardDetailsEditButton;
        mCardIcon = binding.cardDetailsIcon;
        mCardName = binding.cardDetailsTitle;
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
        binding = FragmentCreditCardDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBack.setOnClickListener(v-> mInterface.navigateBack());
        mEdit.setOnClickListener(v-> mInterface.openCardForm(true, creditCard, position));

        if (creditCard != null) initCreditCardInfo();
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setCreditCard(Card card, int position) {
        this.position = position;
        creditCard = card;
    }

    public void updateCreditCardAfterEdit(Card card) {
        creditCard = card;
        initCreditCardInfo();
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    private void initCreditCardInfo() {
        mCardName.setText(creditCard.getName());
        Color color = ColorUtils.getColor(creditCard.getColorId());
        mCardIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
    }
}