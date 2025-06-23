package Adapters;

import static API.Constants.DATE_FORMAT;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;

import org.jspecify.annotations.NonNull;

import android.text.Html;
import java.util.Collections;
import java.util.List;

import API.Constants;
import Models.Reservation;
import Models.Space;
import Models.User;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    static final String LOG_TAG = Constants.LOG_TAG;
    private static List<Reservation> reservationList = Collections.emptyList();
    private static List<Space> spaceList = Collections.emptyList();
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        List<User> userList = ReservationAdapter.userList;
        List<Space> spaceList = ReservationAdapter.spaceList;

        holder.tvReservationNumber.setText("Reservation #" + reservation.getId());

        if (reservation.getStartTime() != null) {
            holder.tvStartTime.setText(Html.fromHtml("<b>From:</b> " + DATE_FORMAT.format(reservation.getStartTime())));
        } else {
            holder.tvStartTime.setText("Date unavailable");
        }

        if (reservation.getEndTime() != null) {
            holder.tvEndTime.setText(Html.fromHtml("<b>To:</b> " + DATE_FORMAT.format(reservation.getEndTime())));
        } else {
            holder.tvEndTime.setText("Date unavailable");
        }

        holder.tvUser.setText(Html.fromHtml("<b>Resident:</b> " + reservation.getUser().getResidentName()));
        holder.tvSpace.setText(Html.fromHtml("<b>In:</b> " + reservation.getSpace().getSpaceName()));

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(reservation,userList,spaceList));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(reservation));
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvReservationNumber, tvStartTime, tvEndTime, tvUser,tvSpace;
        Button btnUpdate, btnDelete;

        public ReservationViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            tvReservationNumber = itemView.findViewById(R.id.tvReservationNumber);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime= itemView.findViewById(R.id.tvEndTime);
            tvUser = itemView.findViewById(R.id.tvResidentReservation);
            tvSpace = itemView.findViewById(R.id.tvSpaceReservation);

            btnUpdate = itemView.findViewById(R.id.btnUpdateReservations);
            btnDelete = itemView.findViewById(R.id.btnDeleteReservations);
        }
    }
}