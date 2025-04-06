package com.example.projetws.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetws.R;
import com.example.projetws.beans.Etudiant;

import java.util.List;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private Context context;
    private List<Etudiant> etudiants;
    private OnEtudiantClickListener listener;

    public interface OnEtudiantClickListener {
        void onEdit(Etudiant etudiant);
        void onDelete(Etudiant etudiant);
    }

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnEtudiantClickListener listener) {
        this.context = context;
        this.etudiants = etudiants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.nomPrenom.setText(etudiant.getNom() + " " + etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());

        holder.itemView.setOnClickListener(v -> showPopup(etudiant));
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    public class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView nomPrenom, ville;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            nomPrenom = itemView.findViewById(R.id.nomPrenom);
            ville = itemView.findViewById(R.id.ville);
        }
    }

    private void showPopup(Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choisir une action")
                .setItems(new CharSequence[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                    if (which == 0) {
                        listener.onEdit(etudiant);
                    } else {
                        listener.onDelete(etudiant);
                    }
                })
                .show();
    }
}
