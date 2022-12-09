package com.project1.community;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project1.community.Adapter.ChatAdapter;
import com.project1.community.Models.MessageModel;
import com.project1.community.databinding.ActivityChatDetailBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {


    ActivityChatDetailBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    //to use on ChatAdapter.java
    public static String receiver_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);


        //hide Action Bar
        getSupportActionBar().hide();

        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("userID");
        String username = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        receiver_image = profilePic;

        //backArrow
        binding.backArrow.setOnClickListener(view -> {
            Intent intent = new Intent(ChatDetailActivity.this,MainActivity.class);
            startActivity(intent);

        });

        //set UserName and Profile Pic
        binding.username.setText(username);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar3).into(binding.profileImage);

        database.getReference().child("Users").child(receiverId).child("currentStatus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null){
                            String currStatus = snapshot.getValue().toString();
                            binding.currentStatus2.setText(currStatus);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(messageModels,this,receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        
        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        //set messages
        database.getReference().child("chats").child(senderRoom)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messageModels.clear();
                                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                    MessageModel model = snapshot1.getValue(MessageModel.class);
                                    model.setMessageId(snapshot1.getKey());
                                    messageModels.add(model);
                                }
                                chatAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        binding.btnSend.setOnClickListener(view -> {
            //getting the message written in textField(EnterMessage)
            String message = binding.enterMessage.getText().toString().trim();

            //textField should not be empty
            if(!message.isEmpty()) {
                final MessageModel model = new MessageModel(senderId, message);
                model.setTimeStamp(new Date().getTime());
                binding.enterMessage.setText("");

                //storing in Firebase Database
                database.getReference().child("chats").child(senderRoom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats").child(receiverRoom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                            }
                        });
            }
        });

        //Online/Offline status and notifications
        database.getReference().child("chats").child(receiverRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        database.getReference().child("Users").child(auth.getUid()).child("currentStatus")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(!snapshot.getValue().toString().equals("Online")) {
                                            final String CHANNEL_ID = "Channel_Id";
                                            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "NewMessage", NotificationManager.IMPORTANCE_HIGH);
                                            getSystemService(NotificationManager.class).createNotificationChannel(channel);

                                            //when User touches Notification
                                            Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(ChatDetailActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                                            //create Notification
                                            Notification.Builder notification = new Notification.Builder(ChatDetailActivity.this, CHANNEL_ID)
                                                    .setContentTitle("New Message")
                                                    .setContentText("You got a new Message from " + username)
                                                    .setSmallIcon(R.drawable.official_icon)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true);
                                            //show Notification
                                            NotificationManagerCompat.from(ChatDetailActivity.this).notify(1, notification.build());
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(ChatDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}