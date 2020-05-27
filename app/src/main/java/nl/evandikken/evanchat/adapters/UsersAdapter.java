package nl.evandikken.evanchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.evandikken.evanchat.R;
import nl.evandikken.evanchat.models.UserModel;

public class UsersAdapter extends BaseAdapter {
    private ArrayList<UserModel> usersList, allUsers;

    private LayoutInflater inflater;
    private ViewHolder holder;

    public UsersAdapter(Context context, ArrayList<UserModel> allUsers) {
        this.usersList = new ArrayList<>();
        this.allUsers = allUsers;

        inflater = LayoutInflater.from(context);
        holder = null;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.main_list_user_item, parent, false);
            holder = new ViewHolder();

            holder.ivIcon = convertView.findViewById(R.id.requestListItemImage);
            holder.tvName = convertView.findViewById(R.id.requestsListItemName);
            holder.tvStatus = convertView.findViewById(R.id.mainListItemStatus);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserModel user = usersList.get(position);

        if (user.getProfile_picture().equals("default")) {
            holder.ivIcon.setImageResource(R.drawable.user_icon);
        } else {
            Picasso.get().load(user.getProfile_picture()).placeholder(R.drawable.user_icon).into(holder.ivIcon);
        }

        holder.tvName.setText(user.getUsername());
        holder.tvStatus.setText(user.getStatus());

        return convertView;
    }

    static class ViewHolder {
        CircleImageView ivIcon;
        TextView tvName;
        TextView tvStatus;
    }

    public void filterList(String query) {
        ArrayList<UserModel> newList = new ArrayList<>();

        for (UserModel userModel : allUsers) {
            if (userModel.getEmail().contains(query.toLowerCase()) || userModel.getUsername().toLowerCase().contains(query.toLowerCase())) {
                if (!userModel.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    newList.add(userModel);
                }
            }
        }
        this.usersList = newList;
        notifyDataSetChanged();
    }

    public void resetList() {
        this.usersList = new ArrayList<>();
        notifyDataSetChanged();
    }
}