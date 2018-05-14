package quizza.unullmass.net.quizza;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    private final int MAX_QUESTIONS = 10;
    ArrayList<HashMap<String, String>> qa = new ArrayList<HashMap<String, String>>();
    private int rightAnswers = 0;
    private int quesRemaining = MAX_QUESTIONS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * This method checks if the question was answered correctly
     *
     * @param view - reference to the radiobutton widget that was chosen as the answer
     */
    public void checkAnswer(View view) {

    }

    /**
     * Intializes the start state of the quiz
     * Gets the Q & A set for the quiz
     * Loads the first question
     */
    public void startQuiz() {

    }

    /**
     * This method resets the app to the default starting state
     *
     * @param view - this is the view reference of the Reset button that transmits the click event
     */
    public void resetQuiz(View view) {
        ArrayList<HashMap<String, String>> qa = new ArrayList<HashMap<String, String>>();
        rightAnswers = 0;
        quesRemaining = MAX_QUESTIONS;
        // reset the text views to their default state
        ((TextView) findViewById(R.id.textview_questremaining)).setText(R.string.ques_rem);
        ((TextView) findViewById(R.id.textview_answeredcorrectly)).setText(R.string.ans_correct);
        ((TextView) findViewById(R.id.textview_question)).setText(R.string.blank_text);

        // clear the radio groups
        RadioGroup ans_rg = ((RadioGroup) findViewById(R.id.rg_answerchoices));
        ans_rg.clearCheck();
        for (int i = 0; i < ans_rg.getChildCount(); i++) {
            ((RadioButton) ans_rg.getChildAt(i)).setText(R.string.blank_text);
        }
    }

    /**
     * This method loads the next question onto the screen
     */
    public void loadNextQuestion() {

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
                    String wrongans = oneObject.getString("incorrect_answers");
                    String asked = oneObject.getString("notyet");
                    qa.add(new HashMap<String, String>());
                    HashMap<String, String> h = qa.get(i);
                    h.put("question", question);
                    h.put("correct_answer", rightans);
                    h.put("incorrect_answers", wrongans);
                    h.put("asked", "notyet");

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
