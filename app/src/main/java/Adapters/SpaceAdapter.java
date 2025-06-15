package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;

import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

import Models.Space;
import Models.SpaceRule;

public class SpaceAdapter extends RecyclerView.Adapter<SpaceAdapter.SpaceViewHolder> {
    private static List<Space> spaceList = Collections.emptyList();
    private static List<SpaceRule> spaceRuleList = Collections.emptyList();

    public interface onSpaceActionListener{
        void onUpdate(Space space,List<SpaceRule> spaceRulesList);
        void onDelete(Space space);
    }

    private final onSpaceActionListener listener;

    public SpaceAdapter(List<Space> spaceList, List<SpaceRule> spaceRuleList, onSpaceActionListener listener){
        SpaceAdapter.spaceList = spaceList;
        SpaceAdapter.spaceRuleList = spaceRuleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_space, parent, false);
        return new SpaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull SpaceViewHolder holder, int position) {
        Space space = spaceList.get(position);
        List<SpaceRule> spaceRulesList = SpaceAdapter.spaceRuleList;
        holder.tvSpaceName.setText(space.getSpaceName());
        holder.tvCapacity.setText(String.valueOf(space.getCapacity()));
        holder.tvSpaceRule.setText(space.getSpaceRule().get(0).getRule());
        holder.tvAvailability.setText(space.isAvailability() ? "Available" : "Unavailable");

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(space,spaceRulesList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(space));
    }

    @Override
    public int getItemCount() {
        return spaceList.size();
    }

    public static class SpaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpaceName, tvCapacity, tvSpaceRule,tvAvailability;
        Button btnUpdate, btnDelete;

        public SpaceViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            tvSpaceName = itemView.findViewById(R.id.tvSpaceName);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvSpaceRule = itemView.findViewById(R.id.tvSpaceRule);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);

            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}