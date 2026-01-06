package com.simats.eathmover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Activity for chatting with the operator.
 */
public class ChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Get operator data from intent
        final Intent intent = getIntent();
        final String operatorId = intent.getStringExtra("operator_id");
        final String operatorName = intent.getStringExtra("operator_name");
        final String operatorPhone = intent.getStringExtra("operator_phone");
        
        // Set operator name as title
        if (operatorName != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(operatorName);
        }

        // Make toolbar clickable to navigate to Operator Details (clicking on title area)
        if (toolbar != null) {
            // Find the title TextView and make it clickable
            View titleView = toolbar.getChildAt(0);
            if (titleView != null && titleView instanceof TextView) {
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailsIntent = new Intent(ChatActivity.this, OperatorDetailsActivity.class);
                        if (operatorId != null) {
                            detailsIntent.putExtra("operator_id", operatorId);
                        }
                        if (operatorName != null) {
                            detailsIntent.putExtra("operator_name", operatorName);
                        }
                        if (operatorPhone != null) {
                            detailsIntent.putExtra("operator_phone", operatorPhone);
                        }
                        startActivity(detailsIntent);
                    }
                });
            }
        }

        // Phone icon in toolbar - Call operator
        ImageButton btnCall = findViewById(R.id.btn_call_from_chat);
        if (btnCall != null) {
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    String operatorPhone = intent.getStringExtra("operator_phone");
                    if (operatorPhone != null && !operatorPhone.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + operatorPhone));
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(ChatActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        chatContainer = findViewById(R.id.chat_container);
        etMessage = findViewById(R.id.et_message_input);

        // Send button
        ImageButton btnSend = findViewById(R.id.btn_send);
        if (btnSend != null) {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = etMessage.getText().toString().trim();
                    if (!message.isEmpty()) {
                        addMessage(message, true);
                        etMessage.setText("");
                    }
                }
            });
        }

        // Add sample messages
        addSampleMessages();
    }

    private void addSampleMessages() {
        addMessage("Hi", false);
        addMessage("Great.", true);
        addMessage("No problem. See you soon!", false);
    }

    private void addMessage(String message, boolean isFromMe) {
        View messageView = getLayoutInflater().inflate(
                isFromMe ? R.layout.item_message_sent : R.layout.item_message_received,
                chatContainer,
                false
        );

        TextView tvMessage = messageView.findViewById(R.id.tv_message);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }

        chatContainer.addView(messageView);
    }
}






