package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;

import Models.SpaceRule;

import java.util.Collections;
import java.util.List;

public class SpaceRuleAdapter extends RecyclerView.Adapter<SpaceRuleAdapter.SpaceRuleViewHolder> {
    private static List<SpaceRule> spaceRuleList = Collections.emptyList();

    public interface OnSpaceRuleActionListener {
        void onUpdate(SpaceRule spaceRule);
        void onDelete(SpaceRule spaceRule);
    }

    private final OnSpaceRuleActionListener listener;

    public SpaceRuleAdapter(List<SpaceRule> spaceRuleList, OnSpaceRuleActionListener listener) {
        SpaceRuleAdapter.spaceRuleList = spaceRuleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpaceRuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_space_rule, parent, false);
        return new SpaceRuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpaceRuleViewHolder holder, int position) {
        SpaceRule spaceRule = spaceRuleList.get(position);
        holder.tvRule.setText(spaceRule.getRule());

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(spaceRule));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(spaceRule));
    }

    @Override
    public int getItemCount() {
        return spaceRuleList.size();
    }

    public static class SpaceRuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvRule;
        Button btnUpdate, btnDelete;

        public SpaceRuleViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRule = itemView.findViewById(R.id.tvRule);

            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}