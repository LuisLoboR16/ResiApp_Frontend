package Adapters;

import android.util.Log;
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

import API.Constants;
import Models.Review;
import Models.Space;
import Models.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    static final String LOG_TAG = Constants.LOG_TAG;
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

        holder.tvRating.setText(String.valueOf(review.getRating()));
        holder.tvComment.setText(review.getComment());

        Log.d(LOG_TAG, "Review User Adapter: " + review.getUser().getResidentName());

        holder.tvUser.setText(review.getUser().getResidentName());
        holder.tvSpace.setText(review.getSpace().getSpaceName());

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(review,userList,spaceList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(review));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvRating, tvComment, tvUser,tvSpace;
        Button btnUpdate, btnDelete;

        public ReviewViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvUser = itemView.findViewById(R.id.tvResident);
            tvSpace = itemView.findViewById(R.id.tvSpace);

            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}