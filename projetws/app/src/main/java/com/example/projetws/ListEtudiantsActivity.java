package com.example.projetws;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListEtudiantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    private static final String URL_LIST = "http://192.168.174.232/projet/Source Files/ws/loadEtudiant.php";
    private static final String URL_DELETE = "http://192.168.174.232/projet/Source Files/ws/deleteEtudiant.php";
    private static final String URL_UPDATE = "http://192.168.174.232/projet/Source Files/ws/updateEtudiant.php";
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button ajouterBtn;
    private Bitmap imageBitmap;
    private String photoName;
    private ImageView currentImageView;
    private Etudiant currentEditingEtudiant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiants);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement...");
        progressDialog.setCancelable(false);

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

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void fetchEtudiants() {
        showLoading(true);

        StringRequest request = new StringRequest(Request.Method.POST, URL_LIST,
                response -> {
                    showLoading(false);
                    try {
                        List<Etudiant> etudiants = new Gson().fromJson(response, new TypeToken<List<Etudiant>>() {}.getType());

                        // Update the UI on the main thread
                        runOnUiThread(() -> {
                            if (adapter == null) {
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
                            } else {
                                adapter.updateData(etudiants);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("JSON Parse Error", e.getMessage());
                        Toast.makeText(this, "Erreur de traitement des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
        showLoading(true);
        StringRequest request = new StringRequest(Request.Method.POST, URL_DELETE,
                response -> {
                    showLoading(false);
                    Log.d("Response", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();

                            // Add small delay before fetching the updated list
                            new Handler().postDelayed(() -> {
                                fetchEtudiants();
                            }, 300);
                        } else {
                            String message = jsonResponse.has("message") ? jsonResponse.getString("message") : "Erreur lors de la suppression";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        Toast.makeText(this, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                        fetchEtudiants();
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void updateEtudiant(final Etudiant updatedEtudiant) {
        // Save the current editing etudiant for reference
        currentEditingEtudiant = updatedEtudiant;

        // Show loading indicator
        showLoading(true);

        StringRequest request = new StringRequest(Request.Method.POST, URL_UPDATE,
                response -> {
                    Log.d("Response", "Raw server response: " + response);
                    showLoading(false);

                    try {
                        // Vérifier si la réponse est un JSON valide
                        if (response.trim().startsWith("{")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                                Toast.makeText(this, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();

                                // Reset image data after successful update
                                imageBitmap = null;
                                photoName = null;

                                // Important: Wait longer before fetching updated data
                                new Handler().postDelayed(() -> {
                                    fetchEtudiants();
                                }, 100); // Increased delay to 1 second

                            } else {
                                String message = jsonResponse.has("message") ? jsonResponse.getString("message") : "Erreur inconnue";
                                Toast.makeText(this, "Erreur: " + message, Toast.LENGTH_SHORT).show();
                                // Refresh anyway to ensure UI is in sync
                                new Handler().postDelayed(() -> fetchEtudiants(), 100);
                            }
                        } else {
                            Log.e("JSON Error", "Response is not valid JSON: " + response);
                            //Toast.makeText(this, "Réponse serveur invalide, rafraîchissement des données", Toast.LENGTH_SHORT).show();
                            // Refresh anyway
                            new Handler().postDelayed(() -> fetchEtudiants(), 100);
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", "Error parsing response: " + e.getMessage());
                        Toast.makeText(this, "Erreur de traitement - l'étudiant pourrait avoir été mis à jour", Toast.LENGTH_SHORT).show();
                        // Refresh anyway to make sure we display current data
                        new Handler().postDelayed(() -> fetchEtudiants(), 1000);
                    }
                },
                error -> {
                    showLoading(false);
                    Log.e("Error", "Network error: " + error.toString());

                    String errorMsg = "Erreur lors de la mise à jour";
                    if (error.networkResponse != null) {
                        errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();

                    // Refresh anyway after error
                    new Handler().postDelayed(() -> fetchEtudiants(), 1000);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(updatedEtudiant.getId()));
                params.put("nom", updatedEtudiant.getNom());
                params.put("prenom", updatedEtudiant.getPrenom());
                params.put("ville", updatedEtudiant.getVille());
                params.put("sexe", updatedEtudiant.getSexe());
                params.put("dateNaissance", updatedEtudiant.getDateNaissance());

                if (imageBitmap != null) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        params.put("photo", Base64.encodeToString(byteArray, Base64.DEFAULT));
                        params.put("photoName", photoName);
                        Log.d("Image", "Image encodée et ajoutée");
                    } catch (Exception e) {
                        Log.e("Image Error", "Error encoding image", e);
                    }
                } else if (updatedEtudiant.getPhoto() != null) {
                    params.put("photo_existante", updatedEtudiant.getPhoto());
                    Log.d("Image", "Utilisation de la photo existante: " + updatedEtudiant.getPhoto());
                }

                // Log pour déboguer
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    Log.d("PARAMS", entry.getKey() + ": " +
                            (entry.getKey().equals("photo") ? "ENCODED_IMAGE" : entry.getValue()));
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Set retry policy to handle slow connections
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000, // Increased timeout to 60 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the queue
        requestQueue.add(request);
    }

    private void showEditDialog(final Etudiant etudiant) {
        // Reset image data when opening a new dialog
        imageBitmap = null;
        photoName = null;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_edit_etudiant, null);

        final EditText etNom = dialogView.findViewById(R.id.et_nom);
        final EditText etPrenom = dialogView.findViewById(R.id.et_prenom);
        final Spinner spinnerVille = dialogView.findViewById(R.id.spinner_ville);
        final RadioGroup radioGroupSexe = dialogView.findViewById(R.id.radio_group_sexe);
        final Button btnDate = dialogView.findViewById(R.id.btn_date);
        final Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        currentImageView = dialogView.findViewById(R.id.img_etudiant);
        final Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Set current values
        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());
        btnDate.setText(etudiant.getDateNaissance());

        // Load current image if exists
        if (etudiant.getPhoto() != null && !etudiant.getPhoto().isEmpty()) {
            String imageUrl = "http://192.168.174.232/projet/Source Files/uploads/" + etudiant.getPhoto();
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(currentImageView);
        } else {
            currentImageView.setImageResource(R.mipmap.ic_launcher);
        }

        // Ville spinner
        ArrayAdapter<String> villeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.villes));
        villeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVille.setAdapter(villeAdapter);

        // Set selected ville if it exists in the array
        int position = findPositionInArray(getResources().getStringArray(R.array.villes), etudiant.getVille());
        if (position >= 0) {
            spinnerVille.setSelection(position);
        }

        // Sexe radio group
        if (etudiant.getSexe() != null && etudiant.getSexe().equals("homme")) {
            radioGroupSexe.check(R.id.radio_homme);
        } else {
            radioGroupSexe.check(R.id.radio_femme);
        }

        // Date picker
        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            // Parse date if available
            if (etudiant.getDateNaissance() != null && !etudiant.getDateNaissance().equals("0000-00-00")) {
                try {
                    String[] dateParts = etudiant.getDateNaissance().split("-");
                    if (dateParts.length == 3) {
                        year = Integer.parseInt(dateParts[0]);
                        month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based in Calendar
                        day = Integer.parseInt(dateParts[2]);
                    }
                } catch (Exception e) {
                    Log.e("Date Parse", "Error parsing date", e);
                }
            }

            new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedMonth = selectedMonth + 1;
                        String date = selectedYear + "-" + String.format("%02d", selectedMonth) + "-" + String.format("%02d", selectedDay);
                        btnDate.setText(date);
                    },
                    year,
                    month,
                    day
            ).show();
        });

        // Image selection
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier l'étudiant");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String updatedNom = etNom.getText().toString().trim();
            String updatedPrenom = etPrenom.getText().toString().trim();
            String updatedVille = spinnerVille.getSelectedItem().toString();
            String updatedSexe = (radioGroupSexe.getCheckedRadioButtonId() == R.id.radio_homme) ? "homme" : "femme";
            String updatedDate = btnDate.getText().toString();

            if (updatedNom.isEmpty() || updatedPrenom.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            Etudiant updatedEtudiant = new Etudiant(
                    etudiant.getId(),
                    updatedNom,
                    updatedPrenom,
                    updatedVille,
                    updatedSexe,
                    updatedDate,
                    imageBitmap != null ? photoName : etudiant.getPhoto()
            );

            updateEtudiant(updatedEtudiant);
            dialog.dismiss();
        });
    }

    // Helper method to find position of a string in an array
    private int findPositionInArray(String[] array, String value) {
        if (value == null) return 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0; // Default to first position if not found
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (currentImageView != null) {
                    currentImageView.setImageBitmap(imageBitmap);
                }
                photoName = "img_" + System.currentTimeMillis() + ".jpg";
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEtudiants();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}