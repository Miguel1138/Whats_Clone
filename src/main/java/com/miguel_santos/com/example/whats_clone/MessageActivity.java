package com.miguel_santos.com.example.whats_clone;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private User user;
    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        RecyclerView recyclerView = findViewById(R.id.recycler_contact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new GroupAdapter();
        recyclerView.setAdapter(adapter);
        verifyAuthetication();
        fetchLastMessage();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent= new Intent(MessageActivity.this, ChatActivity.class);
                MessageActivity.ContactItem contactItem = (MessageActivity.ContactItem) item;
                intent.putExtra("contact", (Parcelable) contactItem.contact);
                startActivity(intent);
            }
        });

    }

    private void fetchLastMessage() {
        String uuid = FirebaseAuth.getInstance().getUid();
        if (uuid != null ) {
            FirebaseFirestore.getInstance().collection("/last-message")
                    .document(uuid)
                    .collection("contacts")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            List<DocumentChange> documentChanges= value.getDocumentChanges();

                            if(documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if(doc.getType() == DocumentChange.Type.ADDED) {
                                        Contact contact = doc.getDocument().toObject(Contact.class);
                                        adapter.add(new ContactItem(contact));
                                    }
                                }
                            }
                        }
                    });
        } else {
            Log.e("teste", "objeto nulo");
        }
    }


    private void verifyAuthetication() {

        if (FirebaseAuth.getInstance().getUid() == null) {

            Intent intent = new Intent(MessageActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case (R.id.contacts):
                Intent intent = new Intent(MessageActivity.this, ContactsActivity.class);
                startActivity(intent);
                break;

            case (R.id.logout):
                FirebaseAuth.getInstance().signOut();
                verifyAuthetication();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ContactItem extends Item<ViewHolder> {

        private final Contact contact;

        private ContactItem(Contact contact) {
            this.contact = contact;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {

            final ImageView user_photo = viewHolder.itemView.findViewById(R.id.img_userMessage);
            final TextView username = viewHolder.itemView.findViewById(R.id.txt_last_msg_username);
            final TextView last_message = viewHolder.itemView.findViewById(R.id.txt_last_msg);

            Picasso.get()
                    .load(contact.getPhotoUrl())
                    .into(user_photo);
            username.setText(contact.getUsername());
            last_message.setText(contact.getLastMessage());

        }

        @Override
        public int getLayout() {
            return R.layout.item_user_message;
        }
    }
}