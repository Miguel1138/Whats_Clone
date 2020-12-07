package com.miguel_santos.com.example.whats_clone;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    private User user;
    private User me;
    private EditText edtChat;
    private Button bntChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getUsername());

        RecyclerView recycler = findViewById(R.id.chat_recycler_view);
        edtChat = findViewById(R.id.chat_edt_msg);
        bntChat = findViewById(R.id.chat_btn_send_msg);

        bntChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        adapter = new GroupAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        fetchMessages();
                    }
                });

    }


    private void fetchMessages() {
        if (me != null) {

            String fromID = me.getUserID();
            String toID = user.getUserID();


            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromID)
                    .collection(toID)
                    .orderBy("timeStamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            List<DocumentChange> documentChanges = value.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType().equals(DocumentChange.Type.ADDED)) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        adapter.add(new MessageItem(message));
                                    }
                                }
                            }
                        }
                    });
        }

    }

    private void sendMessage() {
        String textSent = edtChat.getText().toString();
        edtChat.setText(null);

        final String user_fromID = FirebaseAuth.getInstance().getUid();
        final String user_toID = user.getUserID();
        final long timeStamp = System.currentTimeMillis();

        final Message message = new Message();
        message.setFromID(user_fromID);
        message.setToID(user_toID);
        message.setTimeStamp(timeStamp);
        message.setText(textSent);

        if (!message.getText().isEmpty()) {
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(user_fromID)
                    .collection(user_toID)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            Contact contact = new Contact();
                            contact.setUserID(user_toID);
                            contact.setUsername(user.getUsername());
                            contact.setPhotoUrl(user.getProfileUrl());
                            contact.setLastMessage(message.getText());
                            contact.setTimeStamp(message.getTimeStamp());

                            FirebaseFirestore.getInstance().collection("/last-message")
                                    .document(user_fromID)
                                    .collection("contacts")
                                    .document(user_toID)
                                    .set(contact);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("teste", e.getMessage(), e);
                        }
                    });

            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(user_toID)
                    .collection(user_fromID)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            Contact contact = new Contact();
                            contact.setUserID(user_fromID);
                            contact.setUsername(user.getUsername());
                            contact.setPhotoUrl(user.getProfileUrl());
                            contact.setLastMessage(message.getText());
                            contact.setTimeStamp(message.getTimeStamp());

                            FirebaseFirestore.getInstance().collection("/last-message")
                                    .document(user_toID)
                                    .collection("contacts")
                                    .document(user_fromID)
                                    .set(contact);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("teste", e.getMessage(), e);
                        }
                    });
        }

    }

    private class MessageItem extends Item<ViewHolder> {

        private final Message message;

        public MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            ImageView img_userMessage = viewHolder.itemView.findViewById(R.id.img_msg_user);
            TextView txt_userText = viewHolder.itemView.findViewById(R.id.txt_msg_user);

            txt_userText.setText(message.getText());
            Picasso.get()
                    .load(message.getFromID().equals(FirebaseAuth.getInstance().getUid())
                            ? me.getProfileUrl()
                            : user.getProfileUrl())
                    .into(img_userMessage);
        }

        @Override
        public int getLayout() {
            return message.getFromID().equals(FirebaseAuth.getInstance().getUid())
                    ? R.layout.item_message_sent
                    : R.layout.item_message_received;
        }
    }
}