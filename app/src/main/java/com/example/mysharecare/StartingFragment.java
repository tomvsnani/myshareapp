package com.example.mysharecare;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class StartingFragment extends Fragment {
    Button sendButton;
    Button receiveButton;
    LinearLayout startingLinearLayout;
    Toolbar toolbar;

    public StartingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_starting, container, false);
        sendButton =view. findViewById(R.id.sendButton);
        receiveButton =view .findViewById(R.id.receiveButton);
        startingLinearLayout =view. findViewById(R.id.startingLinearLayout);
        toolbar=view.findViewById(R.id.startActivitytoolbar);
        ((AppCompatActivity)(getActivity())).setSupportActionBar(toolbar);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.startcontainer, new SelectItemsToSendFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MainActivity.class);

                if (view.getId() == R.id.receiveButton) {
                    intent.putExtra(AppConstants.SENDRECEIVEEXTRA, AppConstants.RECEIVE);
                }
                startActivity(intent);
            }
        });
        return view;
    }


}