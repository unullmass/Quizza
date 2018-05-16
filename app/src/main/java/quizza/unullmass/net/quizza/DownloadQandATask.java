package quizza.unullmass.net.quizza;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by unullmass on 16/05/18.
 */


class DownloadQandATask extends AsyncTask<URL, Integer, ArrayList<HashMap<String, String>>> {
    protected ArrayList<HashMap<String, String>> doInBackground(URL... urls) {
        URL url = null;
        if (urls.length == 1) {
            url = urls[0];
        }
        ArrayList<HashMap<String, String>> qa = new ArrayList<>();

        HttpsURLConnection urlConnection = null;
        try {

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

                        publishProgress((int) ((i / (float) jArray.length()) * 100));


                    } catch (JSONException e) {
                        Log.e(this.getClass().getName(), "Error parsing JSON!");
                        return null;
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
                Log.e(this.getClass().getName(), data.toString());
            }


        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Error parsing JSON QA service.");
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return qa;
    }

    protected void onProgressUpdate(Integer... progress) {
        Log.v(getClass().getCanonicalName(), "Retrieving Q&A " + progress + "%");
    }

    protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
        Log.i(getClass().getCanonicalName(), "Completed Q&A fetch");
    }

}
