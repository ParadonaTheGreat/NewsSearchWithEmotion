package com.example.newssearchwithemotion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ArticleActivity extends AppCompatActivity {

    TextView titleText, articleText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article);

        titleText = findViewById(R.id.titleText);
        articleText = findViewById(R.id.articleText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null){
            String title = intent.getStringExtra("title");
            String article = intent.getStringExtra("text");
            titleText.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT));
            System.out.println(article);
            articleText.setText(article);
        }
    }

    public void backPressed(View view) {
        Intent intent = new Intent(ArticleActivity.this, MainActivity.class);
        startActivity(intent);
    }
}