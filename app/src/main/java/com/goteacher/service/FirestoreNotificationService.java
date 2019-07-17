package com.goteacher.service;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

import static com.goteacher.Apps.app;

public class FirestoreNotificationService extends Service  {

    @Override
    public IBinder onBind(Intent intent) {
        String tes = ref.document(userID).toString();
        Log.d("FirestoreNotif", "onCreate: " + tes);
        return null;
    }

    public FirestoreNotificationService(){

    }

    CollectionReference ref;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    NotificationCompat.Builder notification;
    private String userID;

    @Override
    public void onCreate() {
        super.onCreate();
        firestore = FirebaseFirestore.getInstance();
        ref = firestore.collection("com.goteacher").document("Teacher").collection("User");
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(false);
        notification.setOngoing(true);

        userID = app().account.getEmail();
        ref.document(userID).collection("listPesanan").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FirestoreNotif", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d("FirestoreNotif", "New Msg: " + dc.getDocument().toObject(Message.class));
                            break;
                        case MODIFIED:
                            Log.d("FirestoreNotif", "Modified Msg: " + dc.getDocument().toObject(Message.class));
                            break;
                        case REMOVED:
                            Log.d("FirestoreNotif", "Removed Msg: " + dc.getDocument().toObject(Message.class));
                            break;
                    }
                }
            }
        });
        String tes = ref.document(userID).toString();
        Log.d("FirestoreNotif", "onCreate: " + tes);

    }
}
