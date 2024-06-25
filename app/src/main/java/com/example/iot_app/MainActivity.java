package com.example.iot_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button lucesButton, logoutButton;
    private DatabaseReference userRef, accionesRef;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean lucesEncendidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lucesButton = findViewById(R.id.lucesButton);
        logoutButton = findViewById(R.id.logoutButton);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("UsersData").child(userId);



            // Referencia a la ubicación de las acciones
            accionesRef = userRef.child("acciones");

            // Listener para el estado de las luces
            accionesRef.child("luces").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        lucesEncendidas = snapshot.getValue(Boolean.class);
                        actualizarEstadoBotonLuces();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error al obtener estado de las luces: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


            lucesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarAccion(accionesRef, "luces", !lucesEncendidas);
                }
            });

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    // Borrar el estado de la sesión en SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
                    editor.putBoolean("isLoggedIn", false);
                    editor.apply();

                    // Redirigir al LoginActivity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            });
        } else {
            // Manejar el caso donde no hay usuario autenticado
            Toast.makeText(MainActivity.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Método para enviar una acción a Firebase Realtime Database
    private void enviarAccion(DatabaseReference accionesRef, String accion, boolean estado) {
        accionesRef.child(accion).setValue(estado)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Acción " + accion + " enviada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al enviar acción " + accion + ": " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para actualizar el estado del botón de luces
    private void actualizarEstadoBotonLuces() {
        if (lucesEncendidas) {
            lucesButton.setText("Apagar Luces");
        } else {
            lucesButton.setText("Encender Luces");
        }
    }
}
