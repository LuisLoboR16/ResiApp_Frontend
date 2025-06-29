package Adapters;

import static Utils.Constants.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;

import org.jspecify.annotations.NonNull;

import android.text.Html;
import java.util.Collections;
import java.util.List;

import Models.Reservation;
import Models.Space;
import Models.SpaceRule;
import Models.User;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    private static List<Reservation> reservationList = Collections.emptyList();
    private static List<Space> spaceList = Collections.emptyList();
    private static List<SpaceRule> ruleList = Collections.emptyList();
    private static List<User> userList = Collections.emptyList();

    public void setUserList(List<User> userList) {
    }
    public interface onReservationActionListener{
        void onUpdate(Reservation reservation, List<User> userList,List<Space> spaceList);
        void onDelete(Reservation reservation);
    }

    private final onReservationActionListener listener;

    public ReservationAdapter(List<Reservation> reservationList, List<User> userList,List<Space> spaceList, onReservationActionListener listener){
        ReservationAdapter.reservationList = reservationList;
        ReservationAdapter.userList = userList;
        ReservationAdapter.spaceList = spaceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        List<User> userList = ReservationAdapter.userList;
        List<Space> spaceList = ReservationAdapter.spaceList;

        holder.tvReservationNumber.setText(holder.itemView.getContext().getString(R.string.reservation_number) + " " + reservation.getId());
        Space matchedSpace = null;
        for (Space s : spaceList) {
            if (s.getId() == reservation.getSpace().getId()) {
                matchedSpace = s;
                break;
            }
        }

        if (matchedSpace != null && matchedSpace.getImage() != null && matchedSpace.getImage().startsWith("data:image")) {
            try {
                String encoded = matchedSpace.getImage().split(",")[1];
                byte[] imageBytes = Base64.decode(encoded, Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.tvImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.tvImage.setImageResource(R.drawable.ic_resiapp_under_construction);
                Log.e(LOG_TAG, "Error decoding image: " + e.getMessage());
            }
        } else {
            holder.tvImage.setImageResource(R.drawable.ic_resiapp_under_construction);
        }

        if (reservation.getStartTime() != null && reservation.getEndTime() != null) {
            String startTimeFormatted = DATE_FORMAT_HOURS_CUSTOM.format(reservation.getStartTime());
            String endTimeFormatted = DATE_FORMAT_HOURS_CUSTOM.format(reservation.getEndTime());

            holder.tvStartTime.setText(startTimeFormatted+ " - " + endTimeFormatted);
            holder.tvEndTime.setText(DATE_FORMAT_CUSTOM.format(reservation.getStartTime()));

        } else {
            holder.tvEndTime.setText(R.string.date_unavailable);
        }

        List<SpaceRule> rules = (matchedSpace != null) ? matchedSpace.getSpaceRule() : Collections.emptyList();
        String ruleText = (!rules.isEmpty()) ? rules.get(0).getRule() : "N/A";

        holder.tvUser.setText(Html.fromHtml("<b>Resident:</b> " + reservation.getUser().getResidentName()));
        holder.tvSpace.setText(Html.fromHtml("<b>In:</b> " + reservation.getSpace().getSpaceName()));
        holder.tvSpaceRule.setText(Html.fromHtml("<b>Rule:</b> " + ruleText));



        SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "Resident");

        if (role.equalsIgnoreCase("Admin")) {
            holder.btnUpdate.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnUpdate.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(reservation,userList,spaceList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(reservation));
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvReservationNumber, tvStartTime, tvEndTime, tvUser,tvSpace, tvSpaceRule;
        ImageView tvImage;
        Button btnUpdate, btnDelete;

        public ReservationViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            tvReservationNumber = itemView.findViewById(R.id.tvReservationNumber);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime= itemView.findViewById(R.id.tvEndTime);
            tvUser = itemView.findViewById(R.id.tvResidentReservation);
            tvSpace = itemView.findViewById(R.id.tvSpaceReservation);
            tvSpaceRule = itemView.findViewById(R.id.tvSpaceRuleReservation);
            tvImage = itemView.findViewById(R.id.imgSpacePhotoReservations);

            btnUpdate = itemView.findViewById(R.id.btnUpdateReservations);
            btnDelete = itemView.findViewById(R.id.btnDeleteReservations);
        }
    }
}