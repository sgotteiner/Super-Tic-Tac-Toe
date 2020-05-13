package com.sagi.supertictactoeonline.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.entities.User;
import com.sagi.supertictactoeonline.utilities.DownloadImage;
import com.sagi.supertictactoeonline.utilities.constants.FireBaseConstant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.PlaceHolder>{
    private ArrayList<User> users;
    private LayoutInflater layoutInflater;
    private Context context;
    private CallbackAdapterUser mListener;
    private boolean isJoin;


    public AdapterUser(ArrayList<User> users, Context context, CallbackAdapterUser callbackAdapterUser, boolean isJoin) {
        this.users = users;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.mListener = callbackAdapterUser;
        this.isJoin = isJoin;
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtRank;
        private ImageView imgProfile;


        public PlaceHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            txtRank = view.findViewById(R.id.txtRank);
            imgProfile = view.findViewById(R.id.imgProfile);
        }
    }

    @Override
    public AdapterUser.PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_user, parent, false);

        return new AdapterUser.PlaceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterUser.PlaceHolder holder, final int position) {

        final User user = users.get(position);

        holder.txtName.setText(user.getName());
        holder.txtRank.setText("(" + user.getRank() + ")");
        new DownloadImage(user.getName(), new DownloadImage.IDownloadImage() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).fit().into(holder.imgProfile);
            }

            @Override
            public void onFail(String error) {
            }
        }).startLoading();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPick(user, isJoin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface CallbackAdapterUser {
        void onPick(User user, boolean isJoin);
    }
}
