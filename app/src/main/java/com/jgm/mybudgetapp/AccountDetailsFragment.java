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

import com.jgm.mybudgetapp.databinding.FragmentAccountDetailsBinding;
import com.jgm.mybudgetapp.objects.AccountObj;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.room.entity.Account;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconUtils;

public class AccountDetailsFragment extends Fragment {

    public AccountDetailsFragment() {
        // Required empty public constructor
    }

    // Vars
    private int position;
    private Account account;

    // UI
    private FragmentAccountDetailsBinding binding;
    private ImageButton buttonBack, buttonEdit;
    private TextView mAccountName;
    private ImageView mAccountIcon;

    private void setBinding() {
        buttonBack = binding.accountDetailsBackButton;
        buttonEdit = binding.accountDetailsEditButton;
        mAccountName = binding.accountDetailsTitle;
        mAccountIcon = binding.accountDetailsIcon;
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
        binding = FragmentAccountDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setBinding();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonBack.setOnClickListener(v-> mInterface.navigateBack());
        buttonEdit.setOnClickListener(v-> mInterface.openAccountForm(true, account, position));

        if (account != null) initAccountInfo();
    }

    /* ===============================================================================
                                       INTERFACE
     =============================================================================== */

    public void setAccount(Account account, int position) {
        this.position = position;
        this.account = account;
    }

    public void updateAccountAfterEdit(Account editedAccount) {
        account = editedAccount;
        initAccountInfo();
    }

    /* ===============================================================================
                                          DATA
     =============================================================================== */

    private void initAccountInfo() {
        mAccountName.setText(account.getName());
        Color color = ColorUtils.getColor(account.getColorId());
        Icon icon = IconUtils.getIcon(account.getIconId());
        mAccountIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        mAccountIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));
    }
}