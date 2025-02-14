package com.easygbs.easygbd.adapter;

import android.text.TextWatcher;
import android.widget.EditText;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    @BindingAdapter("textWatcher")
    public static void setTextWatcher(EditText editText, TextWatcher textWatcher) {
        if (editText != null) {
            editText.addTextChangedListener(textWatcher);
        }
    }
}
