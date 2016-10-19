package com.croconaut.ratemebuddy.utils;


import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.Comparator;

public class Comparators {

    public static Comparator<Profile> profileRatingComparator = new Comparator<Profile>() {
        @Override
        public int compare(Profile lhs, Profile rhs) {
            Status s1 = lhs.getStatus();
            Status s2 = rhs.getStatus();

            if(s1 != null && s2 != null){
                int likes1 = s1.getVotes().size();
                int likes2 = s2.getVotes().size();

                int sub = likes2 - likes1;
                return sub == 0 ? lhs.getName().compareTo(rhs.getName()) : sub;
            }else if (s1 != null){
                return -1;
            }else if (s2 != null) {
                return 1;
            }

            boolean isUnkn1 = lhs.isUnknown();
            boolean isUnkn2 = rhs.isUnknown();

            if(!isUnkn1 && !isUnkn2)
                return lhs.getName().compareTo(rhs.getName());
            else if (!isUnkn1)
                return -1;
            else return 1;
        }
    };

    public static Comparator<Profile> profileNameComparator = new Comparator<Profile>() {
        @Override
        public int compare(Profile lhs, Profile rhs) {
            boolean isUnkn1 = lhs.isUnknown();
            boolean isUnkn2 = rhs.isUnknown();

            if(!isUnkn1 && !isUnkn2)
                return lhs.getName().compareTo(rhs.getName());
            else if (!isUnkn1)
                return -1;
            else return 1;
        }
    };

    public static Comparator<Profile> profileFavouriteComparator = new Comparator<Profile>() {
        @Override
        public int compare(Profile lhs, Profile rhs) {
            boolean isFav1 = lhs.getType() == Profile.FAVOURITE;
            boolean isFav2 = rhs.getType() == Profile.FAVOURITE;

            if(!isFav1 && !isFav2)
                return 0;
            else if (!isFav1)
                return -1;
            else return 1;
        }
    };
}
