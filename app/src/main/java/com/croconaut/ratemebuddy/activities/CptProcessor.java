package com.croconaut.ratemebuddy.activities;

import android.content.Intent;

import java.io.IOException;

public interface CptProcessor {
    boolean process (Intent cptIntent) throws IOException, ClassNotFoundException;
}
