package com.example.assignment4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddWord extends AppCompatActivity {

    Button cancelButton;
    Button submitButton;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        db = FirebaseFirestore.getInstance();

        cancelButton = findViewById(R.id.cancelButton);
        submitButton = findViewById(R.id.submitButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the MainActivity
                Intent intent = new Intent(AddWord.this, MainActivity.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        db.collection("Wordle").document("Words")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Document exists, get the data
                            Map<String, Object> data = documentSnapshot.getData();

                            // Get the size of the data (number of key-value pairs)
                            int numberOfPairs = data != null ? data.size() : 0;
                            submitButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText wordText = findViewById(R.id.newWord);
                                    String newWord = wordText.getText().toString();
                                    String newKey = "word" + (numberOfPairs + 1);
                                    if(newWord.length() == 5 && newWord.matches("^[a-zA-Z]+$")) {
                                        addNewEntry("Wordle", "Words", newKey, newWord);
                                    } else{
                                        Toast.makeText(v.getContext(), "An error has occurred.", Toast.LENGTH_SHORT).show();
                                        wordText.setBackgroundColor(Color.MAGENTA);
                                    }

                                }
                            });

                            Log.d("DocumentInfo", "Number of key-value pairs: " + numberOfPairs);
                        } else {
                            // Document does not exist
                            Log.d("DocumentInfo", "Document does not exist");
                        }
                    }
                });


    }

    private void addNewEntry(String collectionName, String documentId, String... keyValuePairs) {
        // Create a map with dynamic key-value pairs for your new entry
        Map<String, Object> entryMap = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                entryMap.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
        // Add a new entry with dynamic key-value pairs
        db.collection(collectionName).document(documentId)
                .update(entryMap)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    // This code will be executed when the entry is successfully added
                    Toast.makeText(this, "Word added successfully", Toast.LENGTH_SHORT).show();
                    // Create an Intent to start the MainActivity
                    Intent intent = new Intent(AddWord.this, MainActivity.class);

                    // Start the new activity
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    // This code will be executed if there is an error adding the entry

                });
    }
}