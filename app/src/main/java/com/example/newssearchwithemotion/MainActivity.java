package com.example.newssearchwithemotion;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //region API KEYS
    private static final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private final String API_TOKEN = "hf_XXXXXXXXXXXXXXXXXXXXXXXX";
    //endregion

    private static final String BASE_URL = "https://content.guardianapis.com/search?api-key=" + API_KEY + "&show-fields=body";
    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 20;
    EditText topicSearch;
    String topic;
    TextView articleList;
    String tempArticleList = "Title\n";
    TextView topicList;
    String tempTopicList = "Topic\n";
    TextView emotionList;
    String tempEmotionList = "Emotion\n";
    Handler h;
    DisplayMetrics dm;
    ArrayList<String> articleArrayList;
    ArrayAdapter<String> arr;
    ListView listView;
    ArrayList<String> articleBodyArrayList;
    ArrayList<String> titleArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        topicSearch = findViewById(R.id.topicSearch);
        listView = findViewById(R.id.itemList);
        articleArrayList = new ArrayList<>();
        articleBodyArrayList = new ArrayList<>();
        titleArrayList = new ArrayList<>();
        /*
        articleList = findViewById(R.id.titleList);
        topicList = findViewById(R.id.topicList);
        emotionList = findViewById(R.id.emotionList);
        */
        dm = this.getResources().getDisplayMetrics();
        //articleList.setWidth(dm.widthPixels/5*3);
        //topicList.setWidth(dm.widthPixels/5);
        //emotionList.setWidth(dm.widthPixels/5);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        h = new Handler(){
            @Override
            public void handleMessage(Message msg){
                /*
                articleList.setText(tempArticleList);
                topicList.setText(tempTopicList);
                emotionList.setText(tempEmotionList);

                 */

            }
        };
    }


    private void parseJSON(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject article = results.getJSONObject(i);
                String title = article.getString("webTitle");
                String webUrl = article.getString("webUrl");
                String sectionName = article.getString("sectionName");

                topic = topic.trim();
                boolean test = !topic.equalsIgnoreCase("");
                if (!sectionName.equalsIgnoreCase(topic) && test && !sectionName.toUpperCase().contains(topic.toUpperCase())) {
                    continue;
                }

                // Extract the body content
                JSONObject fields = article.getJSONObject("fields");
                String body = fields.getString("body");

                String emotion = "neutral";

                emotion = detectEmotion(body.substring(0,300));

                titleArrayList.add(title);
                if (title.length()>30){
                    title = title.substring(0,28) + "...";
                }
                tempArticleList += title + "\n";
                tempTopicList += sectionName + "\n";
                tempEmotionList += emotion + "\n";
                if (sectionName.length()>=15){
                    tempArticleList +="\n";
                    tempEmotionList += "\n";
                }
                articleArrayList.add(title + "   " + sectionName + "    " + emotion);
                articleBodyArrayList.add(body);

            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing JSON", e);
        }
        Message message = new Message();
        message.arg1 = 1;
        h.sendMessage(message);
    }


    public void onFetch(View view) {
        tempArticleList = "Title\n";
        tempTopicList = "Topic\n";
        tempEmotionList = "Emotion\n";
        topic = topicSearch.getText().toString();
        Thread r = new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i<MAX_PAGES; i++) {
                        URL url = new URL(BASE_URL + "&page-size=" + PAGE_SIZE + "&page=" + i);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();
                            parseJSON(response.toString());
                        } finally {
                            urlConnection.disconnect();
                        }
                    }
                    arr = new ArrayAdapter<String>(MainActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, articleArrayList);
                    runOnUiThread(() -> listView.setAdapter(arr));

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                            intent.putExtra("title", titleArrayList.get(position));
                            intent.putExtra("text",articleBodyArrayList.get(position));
                            startActivity(intent);
                        }
                    });

                } catch (Exception e) {
                    Log.e("MainActivity", "Error fetching articles", e);
                }
            }
        };
        if (!r.isAlive()) {
            r.start();
        }

    }


    public String detectEmotion(String text){
        try {
            String[] command = {
                    "curl",
                    "https://api-inference.huggingface.co/models/michellejieli/emotion_text_classifier",
                    "-X", "POST",
                    "-d", "{\"inputs\":" + text+ "}",
                    "-H", "Content-Type: application/json",
                    "-H", "Authorization: Bearer " + API_TOKEN};
            Process execute = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(execute.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = stdInput.readLine()) != null) {
                output.append(line);
            }
            System.out.println(output);
            if (text.length()>500){
                text = text.substring(0,500);
            }
            String outputString = output.toString();
            outputString = outputString.substring(12);
            outputString = outputString.substring(0,outputString.indexOf("\""));
            return(outputString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}