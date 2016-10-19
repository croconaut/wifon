package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.croconaut.ratemebuddy.R;

public class EmoticonsAdapter extends BaseAdapter {
    private Context mContext;
    private EditText text;

    // Keep all Images in array
    private Integer[] mEmoticons = {
            R.drawable.emo_smile, R.drawable.emo_sad,
            R.drawable.emo_laugh, R.drawable.emo_wink,
            R.drawable.emo_no_mood, R.drawable.emo_tongue,
            R.drawable.emo_surprise, R.drawable.emo_angry,
            R.drawable.emo_love
    };

    private String[] mSigns = {":)", ":(", ":D", ";)", ":/", ":P", ":o", ">:(", "<3"};

    // Constructor
    public EmoticonsAdapter(Context context, EditText text) {
        mContext = context;
        this.text = text;
    }

    public int getCount() {
        return mEmoticons.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    (int)mContext.getResources().getDimension(R.dimen.gv_emoticon_idem_width),
                    (int)mContext.getResources().getDimension(R.dimen.gv_emoticon_idem_height)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }


        Glide.with(mContext)
                .load(mEmoticons[position])
                .asBitmap()
                .thumbnail(0.3f)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text.append(mSigns[position]);
            }
        });

        return imageView;
    }

}