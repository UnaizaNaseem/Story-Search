package com.midterm.storysearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class LoadingScreenFragment extends Fragment {

    private TextView loadingText;
    private ImageView typewriterImage;

    public LoadingScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_screen, container, false);

        loadingText = view.findViewById(R.id.loadingText);
        typewriterImage = view.findViewById(R.id.typewriterImage);

        // Set up typewriter animation
        Animation typewriterAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.typewriter_animation);
        typewriterAnimation.setInterpolator(new LinearInterpolator());
        typewriterAnimation.setRepeatMode(Animation.RESTART);
        typewriterAnimation.setRepeatCount(Animation.INFINITE);
        typewriterImage.startAnimation(typewriterAnimation);

        // Set up blinking animation for loadingText
        Animation blinkingAnimation = new AlphaAnimation(1, 0);
        blinkingAnimation.setDuration(100);
        blinkingAnimation.setInterpolator(new LinearInterpolator());
        blinkingAnimation.setRepeatMode(Animation.REVERSE);
        blinkingAnimation.setRepeatCount(Animation.INFINITE);
        blinkingAnimation.setStartOffset(10); // Delay before starting
        loadingText.startAnimation(blinkingAnimation);

        return view;
    }
}


