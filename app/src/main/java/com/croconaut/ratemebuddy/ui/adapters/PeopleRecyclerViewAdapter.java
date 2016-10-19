package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommentActivity;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.DisplayProfilesActivity;
import com.croconaut.ratemebuddy.activities.PeopleParentActivityBB;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.util.ArrayList;

public class PeopleRecyclerViewAdapter extends RecyclerView.Adapter<PeopleRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = PeopleRecyclerViewAdapter.class.getName();

    private static final int SNACKBAR_SHOW_DURATION = 4500;

    private ArrayList<Profile> mProfiles;
    private PeopleParentActivityBB activityBB;
    private RecyclerView recyclerView;

    private Typeface tSemiBold;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlProfileItem;
        private RelativeLayout rlLikes;
        private RelativeLayout rlComments;
        private RelativeLayout rlFriends;
        private ImageView ivPhoto;
        private ImageView ivIconLike;
        private TextView tvName;
        private TextView tvStatus;
        private TextView tvLikes;
        private TextView tvComments;
        private TextView tvUnreadMessages;
        private TextView tvFriends;
        private ImageButton ibFavourite;
        private ImageButton ibNotFavourite;
        private ImageButton ibBlock;
        private ImageButton ibRemoveFavourite;
        private ProgressBar pgProfileNotReady;

        public ViewHolder(View v) {
            super(v);
            rlProfileItem = (RelativeLayout) v.findViewById(R.id.rlProfileItem);
            rlLikes = (RelativeLayout) v.findViewById(R.id.rlLikes);
            rlComments = (RelativeLayout) v.findViewById(R.id.rlComments);
            rlFriends = (RelativeLayout) v.findViewById(R.id.rlFriends);
            ivPhoto = (ImageView) v.findViewById(R.id.ivPhoto);
            ivIconLike = (ImageView) v.findViewById(R.id.likeIcon);
            tvUnreadMessages = (TextView) v.findViewById(R.id.unreadMessages);
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            tvLikes = (TextView) v.findViewById(R.id.likesNumber);
            tvComments = (TextView) v.findViewById(R.id.commentsNumber);
            tvFriends = (TextView) v.findViewById(R.id.friendsNumber);
            ibFavourite = (ImageButton) v.findViewById(R.id.ibFavourite);
            ibNotFavourite = (ImageButton) v.findViewById(R.id.ibNotFavourite);
            ibBlock = (ImageButton) v.findViewById(R.id.ibBlock);
            ibRemoveFavourite = (ImageButton) v.findViewById(R.id.ibRemoveFavourite);
            pgProfileNotReady = (ProgressBar) v.findViewById(R.id.pgNotReady);
        }
    }

    public PeopleRecyclerViewAdapter(ArrayList<Profile> profiles, PeopleParentActivityBB activityBB, RecyclerView rv) {
        mProfiles = profiles;
        this.activityBB = activityBB;
        recyclerView = rv;

        tSemiBold = Typeface.createFromAsset(activityBB.getAssets(), "fonts/semibold.ttf");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PeopleRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_profile_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Profile profile = mProfiles.get(position);


        Glide.with(activityBB)
                .load(profile.getThumbUri())
                .asBitmap()
                .signature(new StringSignature(
                        String.valueOf(profile.getTimeStamp())))
                .error(ProfileUtils.getTextDrawableForProfile(profile))
                .thumbnail(0.2f)
                .transform(new CircleTransform(activityBB))
                .into(holder.ivPhoto);

        //set name
        holder.tvName.setText(mProfiles.get(position).getName());
        holder.tvName.setTypeface(tSemiBold);

        holder.rlFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getFriendsList().isEmpty()) {
                    Toast.makeText(
                            activityBB,
                            activityBB.getString(R.string.toast_empty_profiles, profile.getName()),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Intent intent = new Intent(activityBB, DisplayProfilesActivity.class);
                    intent.putExtra(DisplayProfilesActivity.EXTRA_REMOTE_PROFILE_ID, profile.getIdent());
                    intent.putExtra(DisplayProfilesActivity.EXTRA_DISPLAY_TYPE, DisplayProfilesActivity.DISPLAY_FRIENDS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activityBB.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    activityBB.startActivity(intent);
                }
            }
        });

        if (profile.getFriendsList() != null) {
            holder.rlFriends.setVisibility(View.VISIBLE);

            holder.tvFriends.setText(
                    profile.getFriendsList() != null
                            ? String.valueOf(profile.getFriendsList().size())
                            : String.valueOf(0) //zero friends
            );
        } else {
            holder.rlFriends.setVisibility(View.GONE);
        }

        //set status and number of likes & comments
        if (profile.getStatus() != null && profile.getStatus().getContent() != null) {
            holder.rlLikes.setVisibility(View.VISIBLE);
            holder.rlComments.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(profile.getStatus().getContent());
            holder.tvLikes.setText(String.valueOf(profile.getStatus().getVotes().size()));
            holder.tvComments.setText(String.valueOf(profile.getStatus().getComments().size()));
        } else {
            holder.rlLikes.setVisibility(View.GONE);
            holder.rlComments.setVisibility(View.GONE);
            holder.tvStatus.setText(profile.isUnknown() ? profile.getCrocoId() : null);
            holder.tvLikes.setText(null);
            holder.tvComments.setText(null);
        }

        //show number of unread messages
        if (profile.getUnreadMessages().isEmpty()) {
            holder.tvUnreadMessages.setVisibility(View.GONE);
        } else {
            holder.tvUnreadMessages.setVisibility(View.VISIBLE);
            holder.tvUnreadMessages.setText(String.valueOf(profile.getUnreadMessages().size()));
        }

        //open communication activity
        holder.rlProfileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityBB, CommunicationActivity.class);
                intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getCrocoId());
                intent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
                activityBB.startActivity(intent);
                activityBB.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        //send like
        MyProfile myProfile = MyProfile.getInstance(activityBB);
        if (profile.getStatus() != null) {
            for (VoteUp voteUp : profile.getStatus().getVotes()) {
                if (voteUp.getProfileId().equals(myProfile.getProfileId())) {
                    holder.rlLikes.setEnabled(false);
                    holder.ivIconLike.setEnabled(false);
                    break;
                }
            }
        }

        holder.rlLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityBB.sendLike(profile);
                activityBB.updateCurrentFragment();
            }
        });

        //open comments
        holder.rlComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityBB, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_CROCO_ID, profile.getCrocoId());
                activityBB.startActivity(intent);
            }
        });

        //if profile is favourite show 'remove from favourites', otherwise show 'block'

        if (profile.getType() == Profile.FAVOURITE) {
            holder.ibNotFavourite.setVisibility(View.GONE);
            holder.ibFavourite.setVisibility(View.VISIBLE);
            holder.ibBlock.setVisibility(View.GONE);
            holder.ibRemoveFavourite.setVisibility(View.GONE);
        } else {
            holder.ibNotFavourite.setVisibility(profile.isUnknown() ? View.GONE : View.VISIBLE);
            holder.ibFavourite.setVisibility(View.GONE);
            holder.ibBlock.setVisibility(View.VISIBLE);
            holder.ibRemoveFavourite.setVisibility(View.GONE);
        }

        if (profile.getProfileId() == null) {
            holder.pgProfileNotReady.setVisibility(View.VISIBLE);
            holder.ibFavourite.setVisibility(View.GONE);
            holder.ibNotFavourite.setVisibility(View.GONE);
            holder.ibRemoveFavourite.setVisibility(View.GONE);
        } else holder.pgProfileNotReady.setVisibility(View.GONE);


        holder.ibFavourite.setOnClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View v) {
                                                      profile.setType(Profile.UNKNOWN);

                                                      Communication.changeUserTrustLevel(
                                                              activityBB,
                                                              profile.getCrocoId(),
                                                              Communication.USER_TRUST_LEVEL_NORMAL
                                                      );

                                                      insertOrUpdateProfile(profile);

                                                      notifyItemChanged(mProfiles.indexOf(profile));

                                                      changeRmbProfile();

                                                      Snackbar.make(recyclerView, activityBB.getString(R.string.toast_menu_favourites_removed, profile.getName()), SNACKBAR_SHOW_DURATION)
                                                              .setAction(R.string.action_back, new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View view) {
                                                                      profile.setType(Profile.FAVOURITE);

                                                                      Communication.changeUserTrustLevel(
                                                                              activityBB,
                                                                              profile.getCrocoId(),
                                                                              Communication.USER_TRUST_LEVEL_TRUSTED
                                                                      );

                                                                      insertOrUpdateProfile(profile);

                                                                      changeRmbProfile();

                                                                      notifyItemChanged(mProfiles.indexOf(profile));
                                                                      activityBB.updateCurrentFragment();
                                                                  }
                                                              })
                                                              .show();

                                                      activityBB.updateCurrentFragment();
                                                  }
                                              }

        );

        //add favourite
        holder.ibNotFavourite.setOnClickListener(new View.OnClickListener()

                                                 {
                                                     @Override
                                                     public void onClick(View view) {
                                                         profile.setType(Profile.FAVOURITE);

                                                         insertOrUpdateProfile(profile);

                                                         changeRmbProfile();

                                                         Communication.changeUserTrustLevel(activityBB, profile.getCrocoId(), Communication.USER_TRUST_LEVEL_TRUSTED);

                                                         notifyItemChanged(mProfiles.indexOf(profile));

                                                         Snackbar.make(recyclerView, activityBB.getString(R.string.toast_menu_nearby_favourites, profile.getName()), SNACKBAR_SHOW_DURATION)
                                                                 .setAction(R.string.action_back, new View.OnClickListener() {
                                                                     @Override
                                                                     public void onClick(View view) {
                                                                         profile.setType(Profile.UNKNOWN);
                                                                         insertOrUpdateProfile(profile);

                                                                         changeRmbProfile();

                                                                         Communication.changeUserTrustLevel(
                                                                                 activityBB,
                                                                                 profile.getCrocoId(),
                                                                                 Communication.USER_TRUST_LEVEL_NORMAL
                                                                         );

                                                                         activityBB.updateCurrentFragment();

                                                                         notifyItemChanged(mProfiles.indexOf(profile));
                                                                     }
                                                                 })
                                                                 .show();

                                                         activityBB.updateCurrentFragment();
                                                     }
                                                 }

        );

        //block user
        holder.ibBlock.setOnClickListener(new View.OnClickListener()

                                          {
                                              @Override
                                              public void onClick(View view) {
                                                  profile.setType(Profile.BLOCKED);
                                                  insertOrUpdateProfile(profile);
                                                  Communication.changeUserTrustLevel(activityBB, profile.getCrocoId(), Communication.USER_TRUST_LEVEL_BLOCKED);

                                                  final int posBefore = mProfiles.indexOf(profile);

                                                  if (activityBB.getAppData().getNearbyPeople().contains(profile)) {
                                                      activityBB.getAppData().getNearbyPeople().remove(profile);
                                                  }

                                                  mProfiles.remove(profile);
                                                  notifyItemRemoved(posBefore);

                                                  Snackbar.make(recyclerView, activityBB.getString(R.string.toast_menu_blocked_addded, profile.getName()), SNACKBAR_SHOW_DURATION)
                                                          .setAction(R.string.action_back, new View.OnClickListener() {
                                                              @Override
                                                              public void onClick(View view) {
                                                                  Communication.changeUserTrustLevel(
                                                                          activityBB,
                                                                          profile.getCrocoId(),
                                                                          Communication.USER_TRUST_LEVEL_NORMAL
                                                                  );

                                                                  profile.setType(Profile.UNKNOWN);

                                                                  insertOrUpdateProfile(profile);

                                                                  mProfiles.add(posBefore, profile);
                                                                  notifyItemInserted(posBefore);
                                                                  activityBB.updateCurrentFragment();
                                                              }
                                                          })
                                                          .show();

                                                  activityBB.updateCurrentFragment();
                                              }
                                          }

        );

        //remove user from favourites
        holder.ibRemoveFavourite.setOnClickListener(new View.OnClickListener()

                                                    {
                                                        @Override
                                                        public void onClick(View view) {
                                                            profile.setType(Profile.UNKNOWN);

                                                            Communication.changeUserTrustLevel(activityBB, profile.getCrocoId(), Communication.USER_TRUST_LEVEL_NORMAL);

                                                            insertOrUpdateProfile(profile);

                                                            changeRmbProfile();

                                                            notifyItemChanged(mProfiles.indexOf(profile));

                                                            Snackbar.make(recyclerView, activityBB.getString(R.string.toast_menu_favourites_removed, profile.getName()), SNACKBAR_SHOW_DURATION)
                                                                    .setAction(R.string.action_back, new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View view) {
                                                                            profile.setType(Profile.FAVOURITE);

                                                                            Communication.changeUserTrustLevel(
                                                                                    activityBB,
                                                                                    profile.getCrocoId(),
                                                                                    Communication.USER_TRUST_LEVEL_TRUSTED
                                                                            );

                                                                            insertOrUpdateProfile(profile);

                                                                            changeRmbProfile();

                                                                            notifyItemChanged(mProfiles.indexOf(profile));
                                                                            activityBB.updateCurrentFragment();
                                                                        }
                                                                    })
                                                                    .show();

                                                            activityBB.updateCurrentFragment();
                                                        }
                                                    }

        );
    }

    private void changeRmbProfile() {
        try {
            MyProfile myProfile = MyProfile.getInstance(activityBB);
            ProfileUtils.createAndSendRmbProfile(
                    activityBB.getAppData(),
                    myProfile,
                    myProfile.getThumbUri()
            );
        } catch (IOException e) {
            Log.e(TAG, "Cannot create RMPProfile: ", e);
        }
    }

    private void insertOrUpdateProfile(Profile profile) {
        if (activityBB.getAppData().getProfileDataSource().isProfileInDB(profile.getCrocoId()))
            activityBB.getAppData().getProfileDataSource().updateProfile(profile);
        else activityBB.getAppData().getProfileDataSource().insertProfile(profile);
    }


    public void setData(ArrayList<Profile> profiles) {
        this.mProfiles = profiles;
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

}
