package com.miguel_santos.com.example.whats_clone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        RecyclerView recycler = findViewById(R.id.contacts_recycler_view);
        adapter = new GroupAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent= new Intent(ContactsActivity.this, ChatActivity.class);
                UserItem userItem = (UserItem) item;
                intent.putExtra("user", userItem.user);

                startActivity(intent);
            }
        });
        fetchUsers();
    }

   private void fetchUsers() {

        FirebaseFirestore.getInstance().collection("/users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("teste", error.getMessage(), error);
                            return;
                        }

                        List<DocumentSnapshot> docs = value.getDocuments();
                        for (DocumentSnapshot documentSnapshot: docs) {

                            User user = documentSnapshot.toObject(User.class);
                            Log.d("teste", user.getUsername());
                            adapter.add(new UserItem(user));
                        }
                    }
                });
   }

   private class UserItem extends Item<ViewHolder> {

        private final User user;

       private UserItem(User user) {
           this.user = user;
       }

       @Override
       public void bind(@NonNull ViewHolder viewHolder, int position) {
           TextView txtUsername = viewHolder.itemView.findViewById(R.id.txt_contact_username);
           ImageView imgContactPhoto = viewHolder.itemView.findViewById(R.id.img_contact_user);

           txtUsername.setText(user.getUsername());
           Picasso.get()
                   .load(user.getProfileUrl())
                   .into(imgContactPhoto);


       }

       @Override
       public int getLayout() {
           return (R.layout.item_user);
       }
   }

}