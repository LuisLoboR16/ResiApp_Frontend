package Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;
import Models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onUpdate(User user);
        void onDelete(User user);
    }

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvEmail, tvApartment, tvRole, tvPassword;
        private final ImageView imgPhoto;
        private final Button btnUpdate, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvApartment = itemView.findViewById(R.id.tvApartmentInformation);
            tvRole = itemView.findViewById(R.id.tvRole);
            imgPhoto = itemView.findViewById(R.id.imgUserPhoto);

            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(User user, OnUserActionListener listener) {
            tvName.setText(user.getResidentName());
            tvEmail.setText(user.getEmail());
            tvPassword.setText(user.getPassword());
            tvApartment.setText(user.getApartmentInformation());
            tvRole.setText(user.getRole());

            loadProfileImage(user.getImageBase64());

            btnUpdate.setOnClickListener(v -> listener.onUpdate(user));
            btnDelete.setOnClickListener(v -> listener.onDelete(user));
        }

        private void loadProfileImage(String base64Image) {
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.split(",")[1];
                    }
                    byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imgPhoto.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imgPhoto.setImageResource(R.drawable.ic_resiapp_under_construction);
                }
            } else {
                imgPhoto.setImageResource(R.drawable.ic_resiapp_under_construction);
            }
        }
    }
}