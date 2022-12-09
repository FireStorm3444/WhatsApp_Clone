package com.project1.community.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project1.community.AppController;
import com.project1.community.ChatDetailActivity;
import com.project1.community.Models.Users;
import com.project1.community.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_user_show, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);

        //set profile pic and UserName of every User in Chats Fragment
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.userName.setText(users.getUserName());

        //to set current status
        if(users.getCurrentStatus().equals("Online")){
            holder.currentStatus.setText(users.getCurrentStatus());
        } else {
            holder.currentStatus.setText("");
        }

        //To set About
        if (users.getAbout() != null) {
            holder.about.setText(users.getAbout());
        }

        //send data in chat detail activity
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("userID", users.getUserID());
            intent.putExtra("profilePic", users.getProfilePic());
            intent.putExtra("userName", users.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, about, currentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            about = itemView.findViewById(R.id.about);
            currentStatus = itemView.findViewById(R.id.currentStatus1);
        }
    }
}
