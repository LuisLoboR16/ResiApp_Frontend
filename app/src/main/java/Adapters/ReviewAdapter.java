package Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;

import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

import Models.Review;
import Models.Space;
import Models.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static List<Review> reviewList = Collections.emptyList();
    private static List<Space> spaceList = Collections.emptyList();
    private static List<User> userList = Collections.emptyList();

    public interface onReviewActionListener{
        void onUpdate(Review review, List<User> userList,List<Space> spaceList);
        void onDelete(Review review);
    }

    private final onReviewActionListener listener;

    public ReviewAdapter(List<Review> reviewList, List<User> userList,List<Space> spaceList, onReviewActionListener listener){
        ReviewAdapter.reviewList = reviewList;
        ReviewAdapter.userList = userList;
        ReviewAdapter.spaceList = spaceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        List<User> userList = ReviewAdapter.userList;
        List<Space> spaceList = ReviewAdapter.spaceList;

        int rating = review.getRating();
        holder.layoutStars.removeAllViews();

        for (int i = 0; i < rating; i++) {
            ImageView star = new ImageView(holder.itemView.getContext());
            star.setImageResource(R.drawable.ic_star);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(1, 0, 1, 0);
            star.setLayoutParams(params);
            star.setAdjustViewBounds(true);
            star.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.layoutStars.addView(star);
        }

        holder.tvComment.setText(review.getComment());
        holder.tvUser.setText(review.getUser().getResidentName());
        holder.tvSpace.setText(review.getSpace().getSpaceName());

        SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "Guess");

        if (role.equalsIgnoreCase("Admin")) {
            holder.btnUpdate.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnUpdate.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(review,userList,spaceList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(review));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment, tvUser,tvSpace;
        Button btnUpdate, btnDelete;
        LinearLayout  layoutStars;

        public ReviewViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            layoutStars = itemView.findViewById(R.id.layoutStars);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvUser = itemView.findViewById(R.id.tvResident);
            tvSpace = itemView.findViewById(R.id.tvSpace);

            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}