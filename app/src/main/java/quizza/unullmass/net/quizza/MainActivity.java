package quizza.unullmass.net.quizza;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private final int MAX_QUESTIONS = 10;
    private final int MAX_CHOICES = 4;
    private boolean quizRunning = false;
    private ArrayList<HashMap<String, String>> qa = new ArrayList<>();
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
        } else {
            endQuiz();
        }

    }

    /**
     * Intializes the start state of the quiz
     * Gets the Q & A set for the quiz
     * Loads the first question
     */
    public void startQuiz() {
        if (getQandA()) {
            quizRunning = true;
            for (boolean q : askedQ) {
                q = false;
            }

            loadNextQuestion();
        } else {
            Toast.makeText(this, "Failed to fetch questions.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method resets the app to the default starting state
     *
     * @param view - this is the view reference of the Reset button that transmits the click event
     */
    public void resetQuiz(View view) {
        doResetQuiz();
    }

    public void doResetQuiz() {
        Button resetBtn = findViewById(R.id.btn_resetnew);

        // check if the quiz is running
        if (!quizRunning) {
            if (resetBtn.getText().equals(getString(R.string.new_quiz))) {
                startQuiz();
                quizRunning = true;
                resetBtn.setText(getString(R.string.reset_quiz));
            }
        } else {
            ArrayList<HashMap<String, String>> qa = new ArrayList<>();
            rightAnswers = 0;
            quesRemaining = MAX_QUESTIONS;
            qa = new ArrayList<>();
            askedQ = new boolean[MAX_QUESTIONS];
            correctans = "";
            quizRunning = false;

            // reset the text views to their default state
            updateMetrics();
            ((TextView) findViewById(R.id.textview_question)).setText("");

            // clear the radio groups
            RadioGroup ans_rg = findViewById(R.id.rg_answerchoices);
            ans_rg.clearCheck();
            for (int i = 0; i < ans_rg.getChildCount(); i++) {
                RadioButton rb = ((RadioButton) ans_rg.getChildAt(i));
                rb.setText("");
                rb.setEnabled(false);
            }
            resetBtn.setText(getString(R.string.new_quiz));

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
     * This method ends the quiz and gives the user their final score
     */
    public void endQuiz() {
        // display a toast with the final score
        if (rightAnswers == 10) {
            Toast.makeText(this, "You answered ALL questions correctly! A TOAST to you!", Toast.LENGTH_LONG).show();
        } else if (rightAnswers > 1) {
            Toast.makeText(this, "You answered " + rightAnswers + " questions correctly! Keep going!", Toast.LENGTH_LONG).show();
        } else if (rightAnswers == 1) {
            Toast.makeText(this, "You answered 1 question correctly! Try harder!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "You answered no questions correctly! Don't give up!", Toast.LENGTH_LONG).show();
        }

        // Disable the choices so no more questions can be answered
        doResetQuiz();
    }

    /**
     * This method loads the next question onto the screen
     */
    public void loadNextQuestion() {
        if (quesRemaining == 0) {
            endQuiz();
        } else {
            Random r = new java.util.Random();

            // pick a question among the ones that haven't been asked
            int nextqindex = r.nextInt(MAX_QUESTIONS);
            while (askedQ[nextqindex]) {
                nextqindex = r.nextInt(MAX_QUESTIONS);
            }

            // load question
            HashMap<String, String> nextq = qa.get(nextqindex);

            // set question as asked
            askedQ[nextqindex] = true;

            String question = nextq.get("question");
            correctans = nextq.get("correct_answer");

            TreeMap<Integer, String> choices = new TreeMap<>();

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
                    setC[nextcindex] = true;
                    qset++;
                }
                nextcindex = r.nextInt(MAX_CHOICES);
            }

            // update questions remaining
            quesRemaining--;
            updateMetrics();

            // set the question text in the UI
            ((TextView) findViewById(R.id.textview_question)).setText(question);

            // set the choices in the radio group
            RadioGroup ans_rg = findViewById(R.id.rg_answerchoices);
            ans_rg.clearCheck();
            for (int i = 0; i < ans_rg.getChildCount(); i++) {
                RadioButton rb = ((RadioButton) ans_rg.getChildAt(i));
                rb.setText(choices.get(i));
                rb.setEnabled(true);

            }
        }
    }

    private boolean getQandA() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(URLDecoder.decode(getString(R.string.get_ques_url)));


                if (url.getProtocol().toLowerCase().equals("https")) {
                    urlConnection = (HttpsURLConnection) url
                            .openConnection();
                }

                InputStream in;
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    in = urlConnection.getInputStream();
                    InputStreamReader isw = new InputStreamReader(in);
                    BufferedReader bf = new BufferedReader(isw);

                    StringBuilder data = new StringBuilder();
                    String line;

                    while ((line = bf.readLine()) != null) {
                        data.append(line);
                    }

                    JSONObject jo = new JSONObject(data.toString());

                    JSONArray jArray = jo.getJSONArray("results");

                    for (int i = 0; i < jArray.length(); i++) {
                        try {
                            JSONObject oneObject = jArray.getJSONObject(i);
                            // Pulling items from the array
                            String question = Html.fromHtml(oneObject.getString("question")).toString();
                            String rightans = Html.fromHtml(oneObject.getString("correct_answer")).toString();
                            // parse the array containing wrong answers
                            JSONArray jArray_wrongans = oneObject.getJSONArray("incorrect_answers");

                            qa.add(new HashMap<String, String>());
                            HashMap<String, String> h = qa.get(i);
                            h.put("question", question);
                            h.put("correct_answer", rightans);

                            for (int j = 0; j < jArray_wrongans.length(); j++) {
                                h.put("choice" + (j + 1), Html.fromHtml(jArray_wrongans.getString(j)).toString());
                            }
                            h.put("choice0", rightans);

                        } catch (JSONException e) {
                            Log.e(this.getLocalClassName(), "Error parsing JSON!");
                            return false;
                        }
                    }

                } else {
                    in = urlConnection.getErrorStream();
                    InputStreamReader isw = new InputStreamReader(in);
                    BufferedReader bf = new BufferedReader(isw);

                    StringBuilder data = new StringBuilder();
                    String line;

                    while ((line = bf.readLine()) != null) {
                        data.append(line);
                    }
                    Log.e(this.getLocalClassName(), data.toString());
                }


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return true;
    }
}
