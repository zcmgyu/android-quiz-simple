package com.aptech.istqbproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import static com.aptech.istqbproject.config.ConstValue.PREFS_DATA;

public class ResultActivity extends AppCompatActivity {
    private int questionNo;
    private JSONArray questionList;
    private Button btnHome, btnDetail;
    private TextView tvHighScore, tvScore, tvYourAns, tvCorrectAns;
    private HashMap<String, Integer> userAnswerList, correctAnswerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        try {
            init();
            getWidgets();
            setWidgets();
            setWidgetsListener();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void init() throws JSONException {
        Bundle bundle = getIntent().getExtras();
        questionList = new JSONArray(bundle.getString(getString(R.string.question_list)));
        userAnswerList = (HashMap<String, Integer>) bundle.getSerializable(getString(R.string.user_answer_list));

        correctAnswerList = new HashMap<>();
        for (int i = 0; i < questionList.length(); i++) {
            JSONObject quesObj = questionList.getJSONObject(i);
            String correctAnsKey = String.format(getString(R.string.correct_ans_key), i + 1);
            String correctAns = quesObj.getString("true_answer");
            int correctAnsInt = correctAns.charAt(0);
            correctAnswerList.put(correctAnsKey, correctAnsInt);
        }
    }

    private void getWidgets() {
        btnHome = (Button) findViewById(R.id.btn_home);
        btnDetail = (Button) findViewById(R.id.btn_details);

        tvHighScore = (TextView) findViewById(R.id.tv_new_high_score);
        tvScore = (TextView) findViewById(R.id.tv_score);
    }



    private void setWidgets() {
        displayHighScore();
    }

    private void setWidgetsListener() {
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    displayDetails(1);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayHighScore() {
        int correctCount = 0;
        int length = questionList.length();
        for (int i = 0; i < length; i++) {
            Integer correctAns = correctAnswerList.get(String.format(getString(R.string.correct_ans_key), i + 1));
            Integer yourAnswer = userAnswerList.get(String.format(getString(R.string.question_key), i + 1));
            if (correctAns == yourAnswer) {
                correctCount++;
            }
        }

        float currentScore = correctCount / length;

        SharedPreferences preferences = getSharedPreferences(PREFS_DATA, 0);
        float highScore = preferences.getFloat(getString(R.string.high_score_key), 0);
        tvScore.setText(String.format(getString(R.string.your_score), correctCount, length));
        if (highScore >= currentScore) {
            tvHighScore.setVisibility(View.GONE);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(getString(R.string.high_score_key), currentScore);
            editor.apply();
        }
    }

    private void displayDetails(int questionNo) throws IOException, JSONException {
        setContentView(R.layout.activity_result_detail);

        this.questionNo = questionNo;
        Button btnPrevious = (Button) findViewById(R.id.btn_previous);
        Button btnNext = (Button) findViewById(R.id.btn_next);
        LinearLayout direction_quiz = (LinearLayout) findViewById(R.id.ll_direction_quiz);

        if (questionNo == 1) {
            direction_quiz.setWeightSum(1);
            btnPrevious.setVisibility(View.GONE);
        } else {
            direction_quiz.setWeightSum(2);
            btnPrevious.setVisibility(View.VISIBLE);
            btnPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        previous(view);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            btnPrevious.setText(getString(R.string.btn_previous));
        }
        if (questionNo == questionList.length()) {
            btnNext.setText(R.string.btn_done);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            });
        } else {
            btnNext.setText(getString(R.string.btn_next));
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        next(view);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Set question number
        TextView tvQuestionNum = (TextView) findViewById(R.id.question_number);
        tvQuestionNum.setText(String.format(getString(R.string.question_num), this.questionNo, questionList.length()));

        // Get question obj by index
        JSONObject questionObj = questionList.getJSONObject(questionNo - 1);

        // Set question description
        TextView tvQuestion = (TextView) findViewById(R.id.tv_question);
        String question = questionObj.getString("question");
        tvQuestion.setText(question);

        // Set sub question description
        TextView tvSubQuestion = (TextView) findViewById(R.id.tv_sub_question);
        String subQuestion = questionObj.getString("sub_question");
        if (subQuestion.isEmpty() || subQuestion == null) {
            tvSubQuestion.setVisibility(View.GONE);
        } else {
            tvSubQuestion.setVisibility(View.VISIBLE);
            tvSubQuestion.setText(subQuestion);
        }
        // Get answer
        JSONObject answerObj = questionObj.getJSONObject("answers");

        // check either saved option
        int selectedRadio = userAnswerList.get(String.format(getString(R.string.question_key), questionNo));
        // Get widget
        tvYourAns = (TextView) findViewById(R.id.tv_your_answer);
        tvCorrectAns = (TextView) findViewById(R.id.tv_correct_answer);

        // Get answer key and correct answer key
        String answerKey = Character.toString((char) selectedRadio);
        String correctAnswerKey = questionObj.getString("true_answer");

        String yourAnswer = answerObj.getString(answerKey);
        tvYourAns.setText(String.format(getString(R.string.your_answer), yourAnswer));

        if (answerKey.equalsIgnoreCase(correctAnswerKey)) {
            tvCorrectAns.setText(R.string.answer_correct);
        } else {
            String correctAnswer = answerObj.getString(correctAnswerKey);
            tvCorrectAns.setText(String.format(getString(R.string.correct_answer), correctAnswer));
        }
    }


    public void next(View view) throws IOException, JSONException {
        displayDetails(questionNo + 1);
    }

    public void previous(View view) throws IOException, JSONException {
        displayDetails(questionNo - 1);
    }
}
