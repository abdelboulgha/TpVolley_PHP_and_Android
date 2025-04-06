package com.example.projetws.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetws.R;
import com.example.projetws.beans.Etudiant;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private final Context context;
    private List<Etudiant> etudiants;
    private final OnEtudiantClickListener listener;

    public interface OnEtudiantClickListener {
        void onEdit(Etudiant etudiant);
        void onDelete(Etudiant etudiant);
    }

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnEtudiantClickListener listener) {
        this.context = context;
        this.etudiants = etudiants != null ? etudiants : new java.util.ArrayList<>();
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

        holder.nom.setText(String.format("Nom: %s",
                etudiant.getNom() != null ? etudiant.getNom() : ""));

        holder.prenom.setText(String.format("Prenom: %s",
                etudiant.getPrenom() != null ? etudiant.getPrenom() : ""));
        holder.ville.setText(String.format("Ville: %s",
                etudiant.getVille() != null ? etudiant.getVille() : ""));

        // Correction ici - afficher le sexe et non la ville
        holder.sexe.setText(String.format("Sexe: %s",
                etudiant.getSexe() != null ? etudiant.getSexe() : ""));

        holder.dateNaissance.setText(String.format("Né(e) le: %s",
                (etudiant.getDateNaissance() != null && !etudiant.getDateNaissance().equals("0000-00-00"))
                        ? etudiant.getDateNaissance()
                        : "Non définie"));

        try {
            if (etudiant.getPhoto() != null && !etudiant.getPhoto().isEmpty()) {
                String imageUrl = "http://192.168.174.232/projet/Source Files/uploads/" + etudiant.getPhoto();
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.user);
            }
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.user);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) showPopupMenu(v, etudiant);
        });
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    private void showPopupMenu(View view, Etudiant etudiant) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(context, view);
        popup.inflate(R.menu.etudiant_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                listener.onEdit(etudiant);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                listener.onDelete(etudiant);
                return true;
            }
            return false;
        });
        popup.show();
    }

    public void updateData(List<Etudiant> newEtudiants) {
        this.etudiants = newEtudiants != null ? newEtudiants : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nom,prenom, ville, sexe, dateNaissance;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            ville = itemView.findViewById(R.id.ville);
            sexe = itemView.findViewById(R.id.sexe);
            dateNaissance = itemView.findViewById(R.id.dateNaissance);
        }
    }
}