package quizza.unullmass.net.quizza;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private final int MAX_QUESTIONS = 10;
    private final int MAX_CHOICES = 4;
    private boolean quizRunning = false;
    private ArrayList<HashMap<String, String>> qa = new ArrayList<HashMap<String, String>>();
    private int rightAnswers = 0;
    private int quesRemaining = MAX_QUESTIONS;
    private boolean[] askedQ = new boolean[MAX_QUESTIONS];
    private String correctans;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * This method checks if the question was answered correctly
     *
     * @param ansRadio - reference to the radiobutton widget that was chosen as the answer
     */
    public void checkAnswer(View ansRadio) {
        quesRemaining--;

        // which radio button was clicked - get the answer chosen
        String anschoice = ((RadioButton) ansRadio).getText().toString();

        if (anschoice.toLowerCase().equals(correctans.toLowerCase())) {
            // answer is correct
            rightAnswers++;
            Toast.makeText(this.getApplicationContext(), "Right Answer!", Toast.LENGTH_SHORT).show();
        } else {
            //answer wrong
            Toast.makeText(this.getApplicationContext(), "Wrong Answer!", Toast.LENGTH_SHORT).show();
        }

        // load remaining questions
        if (quesRemaining > 0) {
            loadNextQuestion();
        }
    }

    /**
     * Intializes the start state of the quiz
     * Gets the Q & A set for the quiz
     * Loads the first question
     */
    public void startQuiz() {
        getQandA();
        loadNextQuestion();

        for (boolean q : askedQ) {
            q = false;
        }


    }

    /**
     * This method resets the app to the default starting state
     *
     * @param view - this is the view reference of the Reset button that transmits the click event
     */
    public void resetQuiz(View view) {
        Button resetBtn = (Button) findViewById(R.id.btn_resetnew);

        // check if the quiz is running
        if (quizRunning) {
            if (resetBtn.getText().equals(getString(R.string.new_quiz))) {
                startQuiz();
            }
        } else {
            ArrayList<HashMap<String, String>> qa = new ArrayList<HashMap<String, String>>();
            rightAnswers = 0;
            quesRemaining = MAX_QUESTIONS;
            // reset the text views to their default state
            updateMetrics();
            ((TextView) findViewById(R.id.textview_question)).setText(getString(R.string.blank_text));

            // clear the radio groups
            RadioGroup ans_rg = ((RadioGroup) findViewById(R.id.rg_answerchoices));
            ans_rg.clearCheck();
            for (int i = 0; i < ans_rg.getChildCount(); i++) {
                ((RadioButton) ans_rg.getChildAt(i)).setText(getString(R.string.blank_text));
            }
        }

    }

    /**
     * This method updates the metrics on the layout
     */
    public void updateMetrics() {
        // set the metrics in the layout
        ((TextView) findViewById(R.id.textview_questremaining)).setText(getString(R.string.ques_rem) + quesRemaining);
        ((TextView) findViewById(R.id.textview_answeredcorrectly)).setText(getString(R.string.ans_correct) + rightAnswers);
    }

    /**
     * This method loads the next question onto the screen
     */
    public void loadNextQuestion() {
        Random r = new java.util.Random();

        // pick a question among the ones that haven't been asked
        int nextqindex = r.nextInt(MAX_QUESTIONS);
        while (askedQ[nextqindex]) {
            nextqindex = r.nextInt(MAX_QUESTIONS);
        }

        // load question
        HashMap<String, String> nextq = qa.get(nextqindex);

        String question = nextq.get("question");
        correctans = nextq.get("correct_answer");

        TreeMap<Integer, String> choices = new TreeMap<Integer, String>();

        boolean[] setC = new boolean[MAX_CHOICES];
        for (boolean c : setC) {
            c = false;
        }

        // we need to randomly sort the options
        int nextcindex = r.nextInt(MAX_CHOICES);
        int qset = 0;
        while (qset < MAX_CHOICES) {
            if (!setC[nextcindex]) {
                choices.put(nextcindex, nextq.get("choice" + nextcindex));
                qset++;
            }
        }


        updateMetrics();

        // set the question text in the UI
        ((TextView) findViewById(R.id.textview_question)).setText(question);

        // set the choices in the radio group
        RadioGroup ans_rg = ((RadioGroup) findViewById(R.id.rg_answerchoices));
        for (int i = 0; i < ans_rg.getChildCount(); i++) {
            ((RadioButton) ans_rg.getChildAt(i)).setText(choices.get(i));
        }




    }

    private void getQandA() {
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(getString(R.string.get_ques_url));

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);
            BufferedReader bf = new BufferedReader(isw);

            StringBuilder data = new StringBuilder();
            String line = "";

            while ((line = bf.readLine()) != null) {
                data.append(line);
            }

            JSONObject jo = new JSONObject(data.toString());

            JSONArray jArray = jo.getJSONArray("results");

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject oneObject = jArray.getJSONObject(i);
                    // Pulling items from the array
                    String question = oneObject.getString("question");
                    String rightans = oneObject.getString("correct_answer");
                    // parse the array containing wrong answers
                    JSONArray jArray_wrongans = oneObject.getJSONArray("incorrect_answers");

                    qa.add(new HashMap<String, String>());
                    HashMap<String, String> h = qa.get(i);
                    h.put("question", question);
                    h.put("correct_answer", rightans);

                    for (int j = 0; j < jArray_wrongans.length(); j++) {
                        h.put("choice" + (j + 1), jArray_wrongans.getString(j));
                    }
                    h.put("choice0", rightans);

                } catch (JSONException e) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
