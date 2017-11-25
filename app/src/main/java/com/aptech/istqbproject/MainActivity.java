package com.aptech.istqbproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.aptech.istqbproject.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.aptech.istqbproject.config.ConstValue.PREFS_DATA;

public class MainActivity extends AppCompatActivity {
    private JSONArray questionList;
    private ListView lvQuiz;
    private ArrayAdapter<String> adapter;
    private String[] quizItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            init();
            getWidgets();
            setWidgets();
            setWidgetsListener();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void makeQuiz(int length) throws JSONException {
        // Random question list
        JSONArray shuffledList = JsonUtil.shuffleJsonArray(questionList, length);

        // Make data
        Bundle data = new Bundle();

        data.putInt(getString(R.string.quiz_num), 1);
        data.putString(getString(R.string.question_list), shuffledList.toString());
        // Make the intent
        Intent intent = new Intent(this, QuizActivity.class);

        // Add data
        intent.putExtras(data);
        startActivity(intent);
    }

    private void init() throws IOException, JSONException {
        questionList = JsonUtil.loadJsonArrayFile(this, R.raw.file);
        // Init items
        quizItems = getResources().getStringArray(R.array.quiz_items);
        // Init adapter
        adapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, quizItems);
    }

    private void getWidgets() {
        lvQuiz = (ListView) findViewById(R.id.lv_quiz);
    }

    private void setWidgets() {
        lvQuiz.setAdapter(adapter);
    }

    private void setWidgetsListener() {
        lvQuiz.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    makeQuiz((i + 1) * 10);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void changeLanguage(View view) {
        SharedPreferences sp = getSharedPreferences(PREFS_DATA, 0);
        SharedPreferences.Editor editor = sp.edit();
        String languageToLoad = "vi";

        editor.putString(getString(R.string.default_language), languageToLoad);
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
