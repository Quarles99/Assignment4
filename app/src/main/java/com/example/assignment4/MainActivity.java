package com.example.assignment4;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String collectionName = "Wordle";
    String documentId = "Words";
    String currentWord;
    char[] currentChars;
    char[] userAttempt;
    Button submitButton;
    Button addButton;
    Button restartButton;
    Button clearDatabase;
    int attempt = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitButton = findViewById(R.id.Submit);
        addButton = findViewById(R.id.addWord);
        restartButton = findViewById(R.id.restartButton);
        clearDatabase = findViewById(R.id.clearData);

        currentChars = new char[5];
        userAttempt = new char[5];

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the AddWord activity
                Intent intent = new Intent(MainActivity.this, AddWord.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        clearDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> emptyMap = new HashMap<>();
                db.collection(collectionName).document(documentId).set(emptyMap);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            boolean guessedWord = false;
            public void onClick(View v) {
                String packageName = getPackageName();
                int correctLetters = 0;
                //Submit attempt
                if(attempt < 7 && !guessedWord) {
                    int i = 1;

                    //Check attempt against chosen word
                    for(char currentChar : userAttempt){
                        //Reset user entry and carry over entry to attempt
                        String inputID = "Entry" + i;
                        Log.d("inputID", inputID);
                        int entryID = getResources().getIdentifier(inputID, "id", packageName);
                        Log.d("entryID", String.valueOf(entryID));
                        if(entryID != 0){
                            EditText inputText = findViewById(entryID);
                            userAttempt[i-1] = inputText.getText().toString().charAt(0);
                            Log.d("User Attempt", String.valueOf(userAttempt[i-1]));
                            inputText.setText("");
                        }
                        if(i < 6) {
                            String currentID = "row" + attempt + "Letter" + i;
                            Log.d("ID", currentID);
                            int resID = getResources().getIdentifier(currentID, "id", packageName);

                            Log.d("resID", resID + "");
                            if(resID != 0) {
                                EditText currentCharText = findViewById(resID);
                                Log.d("EditText", "Found EditText: " + currentCharText);
                                currentCharText.setText(String.valueOf(userAttempt[i-1]));
                                Log.d("Current Text", currentCharText.getText().toString());
                                //Color background based on response
                                if (userAttempt[i-1] == currentChars[i-1]) {
                                    currentCharText.setBackgroundColor(Color.GREEN);
                                    correctLetters++;
                                } else if (currentWord.contains(String.valueOf(userAttempt[i-1]))) {
                                    currentCharText.setBackgroundColor(Color.YELLOW);
                                } else {
                                    currentCharText.setBackgroundColor(Color.RED);
                                }
                            } else {
                                Log.d("EditText", "EditText not found for ID: " + currentID);
                            }
                            if(correctLetters == 5){
                                guessedWord = true;
                            }
                            i++;
                        }
                    }
                }
                if(guessedWord){
                    //Let the user know they won
                    Toast.makeText(v.getContext(), "Congratulations! You have won the game!", Toast.LENGTH_LONG).show();
                }
                attempt++;
            }
        });

        db = FirebaseFirestore.getInstance();
        getRandomFieldFromDocument(collectionName, documentId, new OnRandomFieldCallback() {
            public void onRandomField (String randomField){
                currentWord = randomField;
                int i = 0;
                for(char currentChar : currentWord.toCharArray()){
                    if(i < 5){
                        currentChars[i] = currentChar;
                        i++;
                    }
                }
                Log.d("CurrentWord", currentWord);
            }
        });

    }



    // Method to select a random field from document
    public void getRandomFieldFromDocument(String collectionName, String documentId, OnRandomFieldCallback callback) {
        DocumentReference documentReference = db.collection(collectionName).document(documentId);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document != null && document.exists()) {
                        handleRandomField(document.getData(), callback);
                    } else {
                        // Handle the case where the document does not exist
                        Log.d("Firestore", "Document does not exist");
                    }
                } else {
                    // Handle errors
                    Log.e("Firestore", "Error getting document", task.getException());
                }
            }
        });
    }
    // Method to handle the randomly selected field
    private void handleRandomField(Map<String, Object> data, OnRandomFieldCallback callback) {
        // Get all the field names from the document
        List<String> fieldNames = new ArrayList<>(data.keySet());

        // Selects a random field name
        if (!fieldNames.isEmpty()) {
            int randomIndex = new Random().nextInt(fieldNames.size());
            String randomFieldName = fieldNames.get(randomIndex);

            // Use the data from the random field as needed
            Object randomFieldValue = data.get(randomFieldName);
            callback.onRandomField(randomFieldValue.toString());
        } else {
            // Handle the case where there are no fields in the document
            Log.d("Firestore", "No fields in the document");
        }
    }

    public interface OnRandomFieldCallback {
        void onRandomField(String randomField);
    }

}