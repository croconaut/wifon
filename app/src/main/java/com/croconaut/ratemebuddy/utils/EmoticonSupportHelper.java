package com.croconaut.ratemebuddy.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.croconaut.ratemebuddy.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoticonSupportHelper {

    private final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private final Map<Pattern, Integer> emoticons = new HashMap<>(0);

    public void initializeSmiles() {
        addPattern(emoticons, ":)", R.drawable.emo_smile);
        addPattern(emoticons, ":))", R.drawable.emo_smile);
        addPattern(emoticons, ":-)", R.drawable.emo_smile);
        addPattern(emoticons, ":-))", R.drawable.emo_smile);
        addPattern(emoticons, ":->", R.drawable.emo_smile);

        addPattern(emoticons, ":'(", R.drawable.emo_sad);
        addPattern(emoticons, ":-(", R.drawable.emo_sad);
        addPattern(emoticons, ":(", R.drawable.emo_sad);

        addPattern(emoticons, ":-D", R.drawable.emo_laugh);
        addPattern(emoticons, ":D", R.drawable.emo_laugh);
        addPattern(emoticons, ":d", R.drawable.emo_laugh);

        addPattern(emoticons, ";-)", R.drawable.emo_wink);
        addPattern(emoticons, ";)", R.drawable.emo_wink);

        addPattern(emoticons, ":-/", R.drawable.emo_no_mood);
        addPattern(emoticons, ":/", R.drawable.emo_no_mood);

        addPattern(emoticons, ":O", R.drawable.emo_surprise);
        addPattern(emoticons, ":o", R.drawable.emo_surprise);
        addPattern(emoticons, ":-o", R.drawable.emo_surprise);
        addPattern(emoticons, ":-O", R.drawable.emo_surprise);

        addPattern(emoticons, ":-*", R.drawable.emo_love);
        addPattern(emoticons, ":*", R.drawable.emo_love);
        addPattern(emoticons, "<3", R.drawable.emo_love);

        addPattern(emoticons, ":-P", R.drawable.emo_tongue);
        addPattern(emoticons, ":P", R.drawable.emo_tongue);
        addPattern(emoticons, ":p", R.drawable.emo_tongue);
        addPattern(emoticons, ":-p", R.drawable.emo_tongue);

        addPattern(emoticons, ">:(", R.drawable.emo_angry);
        addPattern(emoticons, ">:-(", R.drawable.emo_angry);
    }

    private void addPattern(Map<Pattern, Integer> map, String smile, int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    public boolean addSmiles(Context context, Spannable spannable) {
        initializeSmiles();

        boolean hasChanges = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                    if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span);
                    } else {
                        set = false;
                        break;
                    }
                }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new ImageSpan(context, entry.getValue()), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
        return spannable;
    }
}
