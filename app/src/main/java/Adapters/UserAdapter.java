package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resiapp.R;
import com.example.resiapp.Users;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static List<Users> listaUsuarios = Collections.emptyList();

    public interface OnUserActionListener {
        void onActualizar(Users user);
        void onEliminar(Users user);
    }

    private final OnUserActionListener listener;

    public UserAdapter(List<Users> listaUsuarios, OnUserActionListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
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

        // Acciones
        holder.btnActualizar.setOnClickListener(v -> listener.onActualizar(usuario));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(usuario));
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail, tvApartamento, tvRol, tvInfo;
        Button btnActualizar, btnEliminar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvApartamento = itemView.findViewById(R.id.tvApartamento);
            tvRol = itemView.findViewById(R.id.tvRol);
            btnActualizar = itemView.findViewById(R.id.btnActualizar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void eliminarItem(Users user) {
            int position = listaUsuarios.indexOf(user);
            if (position != -1) {
                listaUsuarios.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
}
