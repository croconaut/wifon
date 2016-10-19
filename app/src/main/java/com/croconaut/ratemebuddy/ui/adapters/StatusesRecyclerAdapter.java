package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.ItemClickCallback;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.util.List;

public class StatusesRecyclerAdapter extends RecyclerView.Adapter<StatusesRecyclerAdapter.ViewHolder>{
    private List<Status> mStatuses;
    private Context mContext;
    private TypedValue typedValue;
    private Typeface tLight;
    private Typeface tSemiBold;
    private ItemClickCallback mItemClickCallback;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlStatusView;
        private RelativeLayout divider;
        private TextView tvStatus;
        private TextView tvLikes;
        private TextView tvComments;
        private ImageView ivLikes;
        private ImageView ivComments;

        public ViewHolder(View v) {
            super(v);
            rlStatusView = (RelativeLayout) v.findViewById(R.id.rlStatusView);
            divider = (RelativeLayout) v.findViewById(R.id.divider);
            tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            tvLikes = (TextView) v.findViewById(R.id.tvLikes);
            tvComments = (TextView) v.findViewById(R.id.tvComments);
            ivLikes = (ImageView) v.findViewById(R.id.likeIcon);
            ivComments = (ImageView) v.findViewById(R.id.commentIcon);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    //use array of profiles
    public StatusesRecyclerAdapter(List<Status> statuses, Context context, ItemClickCallback itemClickCallback) {
        this.mStatuses = statuses;
        this.mItemClickCallback = itemClickCallback;
        this.mContext = context;
        typedValue = new TypedValue();

        tLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/light.ttf");
        tSemiBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/semibold.ttf");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public StatusesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_status_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Status status = mStatuses.get(position);
        if(status.equals(MyProfile.getInstance(mContext).getStatus())) {
            //current status
            holder.tvStatus.setTypeface(tSemiBold);
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext,R.color.material_white));

            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true);
            holder.rlStatusView.setBackgroundColor(typedValue.data);

            holder.ivLikes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_thumb_up_white_24dp));
            holder.ivComments.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_comment_white_24dp));

            holder.tvLikes.setTextColor(ContextCompat.getColor(mContext,R.color.material_white));
            holder.tvComments.setTextColor(ContextCompat.getColor(mContext,R.color.material_white));

            holder.divider.setBackgroundColor(typedValue.data);
        } else {
            //other statuses
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext,R.color.material_grey_600));
            holder.tvStatus.setTypeface(tLight);

            int[] attrs = {R.attr.selectableItemBackground, R.attr.iconLikes, R.attr.iconComments};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);

            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

            holder.rlStatusView.setBackgroundResource(typedArray.getResourceId(0, 0));
            //noinspection ResourceType
            holder.ivLikes.setImageDrawable(ContextCompat.getDrawable(mContext, typedArray.getResourceId(1, 0)));
            //noinspection ResourceType
            holder.ivComments.setImageDrawable(ContextCompat.getDrawable(mContext, typedArray.getResourceId(2, 0)));
            holder.tvLikes.setTextColor(typedValue.data);
            holder.tvComments.setTextColor(typedValue.data);

            holder.divider.setBackgroundColor(ContextCompat.getColor(mContext,R.color.material_grey_200));
        }
        holder.tvStatus.setText(status.getContent());

        holder.tvLikes.setText(String.valueOf(status.getVotes().size()));
        holder.tvComments.setText(String.valueOf(status.getComments().size()));

        holder.rlStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickCallback.onItemClick(status);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mStatuses.size();
    }


}