package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;
import com.example.resiapp.Users;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<Users> listaUsuarios;

    public UserAdapter(List<Users> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users usuario = listaUsuarios.get(position);
        holder.tvNombre.setText(usuario.getResidentName());
        holder.tvEmail.setText(usuario.getEmail());
        holder.tvApartamento.setText(usuario.getAparment());
        holder.tvRol.setText(usuario.getRole());
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvApartamento, tvRol, tvInfo;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvApartamento = itemView.findViewById(R.id.tvApartamento);
            tvRol = itemView.findViewById(R.id.tvRol);
        }
    }
}
