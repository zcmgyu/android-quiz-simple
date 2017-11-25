package com.aptech.istqbproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class QuizActivity extends AppCompatActivity {
    private int questionNo;
    private JSONArray questionList;
    private int quizNum;
    private HashMap<String, Integer> userAnswerList;
    private Button btnPrevious, btnNext;
    private LinearLayout llDirectionQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            init();
            getWidgets();
            setWidgets();
            setWidgetsListener();
            // Display list quiz
            displayQuiz(quizNum);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void init() throws IOException, JSONException {
        // Init list user answer list
        userAnswerList = new HashMap<>();
        //
        setContentView(R.layout.activity_quiz);
        Bundle data = getIntent().getExtras();
        quizNum = data.getInt(getString(R.string.quiz_num));
        questionList = new JSONArray(data.getString(getString(R.string.question_list)));
    }

    private void getWidgets() {
         btnPrevious = (Button) findViewById(R.id.btn_previous);
         btnNext = (Button) findViewById(R.id.btn_next);
        llDirectionQuiz = (LinearLayout) findViewById(R.id.ll_direction_quiz);
    }

    private void setWidgets() {

    }

    private void setWidgetsListener() {

    }

    private void displayQuiz(int questionNo) throws IOException, JSONException {
        this.questionNo = questionNo;

        // Get Radio Group
        final RadioGroup rgAnswer = (RadioGroup) findViewById(R.id.rg_answer);
        rgAnswer.removeAllViews();
        rgAnswer.clearCheck();

        if (questionNo == 1) {
            llDirectionQuiz.setWeightSum(1);
            btnPrevious.setVisibility(View.GONE);
        } else {
            llDirectionQuiz.setWeightSum(2);
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
            btnNext.setText(R.string.btn_submit);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (rgAnswer.getCheckedRadioButtonId() == -1) {
                        Toast.makeText(getApplicationContext(), getString(R.string.required_answer_submit), Toast.LENGTH_SHORT).show();
                    } else {
                        submitQuiz();
                    }
                }
            });
        } else {
            btnNext.setText(getString(R.string.btn_next));
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (rgAnswer.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(getApplicationContext(), getString(R.string.required_answer_next), Toast.LENGTH_SHORT).show();
                        } else {
                            next(view);
                        }
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
        // Set answer radio
        JSONObject answerObj = questionObj.getJSONObject("answers");
        Iterator<String> answerIter = answerObj.keys();
        while (answerIter.hasNext()) {
            String key = answerIter.next();
            String answer = answerObj.getString(key);
            RadioButton rbAnswer = new RadioButton(this);
            rbAnswer.setId(key.charAt(0));
            rbAnswer.setText(answer);
            rgAnswer.addView(rbAnswer);
        }

        // check either saved option
        String questionKey = String.format(getString(R.string.question_key), questionNo);
        Integer selectedRadio = userAnswerList.get(questionKey);
        if (selectedRadio != null) {
            RadioButton r = rgAnswer.findViewById(selectedRadio);
            r.setChecked(true);
        }

    }

    public void previous(View view) throws IOException, JSONException {
        saveAnswer(questionNo);
        displayQuiz(questionNo - 1);
    }

    public void next(View view) throws IOException, JSONException {
        saveAnswer(questionNo);
        displayQuiz(questionNo + 1);
    }

    private void submitQuiz() {
        saveAnswer(questionNo);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.submit_confirm_title))
                .setMessage(getString(R.string.submit_on_firm_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Make data
                        Bundle data = new Bundle();

                        data.putString(getString(R.string.question_list), questionList.toString());
                        data.putSerializable(getString(R.string.user_answer_list), userAnswerList);
                        // Make the intent
                        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);

                        intent.putExtras(data);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void saveAnswer(int questionNo) {
        RadioGroup rg = (RadioGroup) findViewById(R.id.rg_answer);
        int checkedRadio = rg.getCheckedRadioButtonId();
        String questionKey = String.format(getString(R.string.question_key), questionNo);
        userAnswerList.put(questionKey, checkedRadio);
    }
}
