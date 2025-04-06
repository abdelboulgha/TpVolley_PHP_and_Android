package com.example.projetws;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws.adapter.EtudiantAdapter;
import com.example.projetws.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListEtudiantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private RequestQueue requestQueue;

    private static final String URL_LIST = "http://192.168.56.1/projet/Source Files/ws/loadEtudiant.php";
    private static final String URL_DELETE = "http://192.168.56.1/projet/Source Files/ws/deleteEtudiant.php";
    private static final String URL_UPDATE = "http://192.168.56.1/projet/Source Files/ws/updateEtudiant.php";
    private Dialog dialog;

    private Button ajouterBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiants);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = Volley.newRequestQueue(this);
        fetchEtudiants();

        ajouterBtn = findViewById(R.id.ajouterBtn);
        ajouterBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ListEtudiantsActivity.this, AddEtudiantActivity.class);
            startActivity(intent);
        });
    }

    private void fetchEtudiants() {
        StringRequest request = new StringRequest(Request.Method.POST, URL_LIST,
                response -> {
                    List<Etudiant> etudiants = new Gson().fromJson(response, new TypeToken<List<Etudiant>>() {}.getType());
                    adapter = new EtudiantAdapter(this, etudiants, new EtudiantAdapter.OnEtudiantClickListener() {
                        @Override
                        public void onEdit(Etudiant etudiant) {
                            showEditDialog(etudiant);
                        }

                        @Override
                        public void onDelete(Etudiant etudiant) {
                            confirmDelete(etudiant);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                },
                error -> Log.e("Volley Error", error.toString())
        );
        requestQueue.add(request);
    }

    private void confirmDelete(Etudiant etudiant) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer cet étudiant ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteEtudiant(etudiant.getId()))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteEtudiant(int id) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_DELETE,
                response -> {
                    Log.d("Response", response);
                },
                error -> Log.e("Volley Error", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id)); // L'étudiant à supprimer
                return params;
            }
        };
        fetchEtudiants();
        requestQueue.add(request);
    }

    private void updateEtudiant(Etudiant updatedEtudiant) {
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, URL_UPDATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // Handle response (e.g., refresh the list)
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(updatedEtudiant.getId()));
                params.put("nom", updatedEtudiant.getNom());
                params.put("prenom", updatedEtudiant.getPrenom());
                params.put("ville", updatedEtudiant.getVille());
                params.put("sexe", updatedEtudiant.getSexe());
                return params;
            }

        };
        fetchEtudiants();
        requestQueue.add(request);

    }

    private void showEditDialog(final Etudiant etudiant) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_edit_etudiant, null);

        // Initialize dialog components
        final EditText etNom = dialogView.findViewById(R.id.et_nom);
        final EditText etPrenom = dialogView.findViewById(R.id.et_prenom);
        final Spinner spinnerVille = dialogView.findViewById(R.id.spinner_ville);
        final RadioGroup radioGroupSexe = dialogView.findViewById(R.id.radio_group_sexe);
        final Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Set the current values in the dialog fields
        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());

        // Load Ville list from strings.xml
        ArrayAdapter<String> villeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.villes));
        villeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVille.setAdapter(villeAdapter);

        // Set selected item of the Spinner to the current Etudiant's ville
        String currentVille = etudiant.getVille();
        int spinnerPosition = villeAdapter.getPosition(currentVille);
        spinnerVille.setSelection(spinnerPosition);

        // Set Radio Button for Gender
        if (etudiant.getSexe().equals("homme")) {
            radioGroupSexe.check(R.id.radio_homme);
        } else {
            radioGroupSexe.check(R.id.radio_femme);
        }

        // Show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Etudiant");
        builder.setView(dialogView);

        // Create the dialog before setting the OnClickListener
        AlertDialog dialog = builder.create();
        dialog.show();

        // Save the edited Etudiant when the "Save" button is clicked
        btnSave.setOnClickListener(v -> {
            String updatedNom = etNom.getText().toString();
            String updatedPrenom = etPrenom.getText().toString();
            String updatedVille = spinnerVille.getSelectedItem().toString();
            String updatedSexe = (radioGroupSexe.getCheckedRadioButtonId() == R.id.radio_homme) ? "homme" : "femme";

            // Create the updated Etudiant object
            Etudiant updatedEtudiant = new Etudiant(etudiant.getId(), updatedNom, updatedPrenom, updatedVille, updatedSexe);

            // Call your update method (Volley POST request) here
            updateEtudiant(updatedEtudiant);

            fetchEtudiants();
            // Dismiss the dialog
            dialog.dismiss();

            // Refresh the list (RecyclerView) after updating
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEtudiants();
    }
}