package com.jgm.mybudgetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jgm.mybudgetapp.MainInterface;
import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.dialogs.MethodPickerDialog;
import com.jgm.mybudgetapp.objects.Color;
import com.jgm.mybudgetapp.objects.Icon;
import com.jgm.mybudgetapp.objects.PaymentMethod;
import com.jgm.mybudgetapp.utils.ColorUtils;
import com.jgm.mybudgetapp.utils.IconOutlineUtils;
import com.jgm.mybudgetapp.utils.IconUtils;

import java.util.List;

public class MethodPickerAdapter extends RecyclerView.Adapter<MethodPickerAdapter.ListViewHolder>{

    private final Context mContext;
    private final List<PaymentMethod> mDataList;
    private final LayoutInflater layoutInflater;
    private final MainInterface mInterface;
    private final MethodPickerDialog dialog;

    public MethodPickerAdapter(Context context, List<PaymentMethod> mDataList, MethodPickerDialog dialog) {
        this.mContext = context;
        this.mDataList = mDataList;
        this.mInterface = (MainInterface) context;
        layoutInflater = LayoutInflater.from(context);
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_method_picker, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        PaymentMethod method = mDataList.get(position);
        Color color = ColorUtils.getColor(method.getColorId());
        Icon icon;
        String info;
        String methodType;

        switch (method.getType()) {
            case 0: methodType = mContext.getString(R.string.account_cash); break;
            case 1: methodType = mContext.getString(R.string.account_checking); break;
            case 2: methodType = mContext.getString(R.string.account_savings); break;
            default: methodType = mContext.getString(R.string.title_credit_card);
        }

        // Set icon to credit card if type = 3
        if (method.getType() == 3) {
            icon = IconOutlineUtils.getIcon(70);
            info = methodType + " - billing day: " + method.getBillingDay();
        }
        else {
            icon = IconOutlineUtils.getIcon(method.getIconId());
            info = methodType;
        }

        // Set Icon and color
        holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, icon.getIcon()));
        holder.mIcon.setContentDescription(icon.getIconName());
        holder.mIcon.setImageTintList(ContextCompat.getColorStateList(mContext, color.getColor()));

        // Set account name and type
        holder.mName.setText(method.getName());
        holder.mInfo.setText(info);

        // Choose item on click and send it to AddFragment
        holder.mContainer.setOnClickListener(v -> {
            mInterface.setSelectedPaymentMethod(method);
            dialog.dismiss();
        });

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mName, mInfo;
        private final ConstraintLayout mContainer;

        private ListViewHolder(@NonNull View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.item_method_picker_icon);
            mName = itemView.findViewById(R.id.item_method_picker_name);
            mInfo = itemView.findViewById(R.id.item_method_picker_info);
            mContainer = itemView.findViewById(R.id.item_method_picker_container);

        }
    }
}
