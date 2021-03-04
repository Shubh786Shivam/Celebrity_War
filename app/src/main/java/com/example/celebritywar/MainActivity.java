package com.example.celebritywar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> celebUrl = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    ImageView imageView;
    int choosenCeleb = 0;
    String[] answers = new String[4];
    int locationOfAnswer = 0;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
               URL url = new URL(urls[0]);
               HttpURLConnection connection = (HttpURLConnection) url.openConnection();
               connection.connect();
               InputStream inputStream = connection.getInputStream();
               Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
               return myBitmap;
            }catch(Exception e){
               e.printStackTrace();
               return null;
            }
        }
    }
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Mozilla");
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    public void celebChosen(View view){
        if(Integer.toString(locationOfAnswer).equals(view.getTag().toString())){
            Toast.makeText(this, "Correct:)", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Incorrect:(", Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }
    public void newQuestion(){
        Random rand = new Random();
        choosenCeleb = rand.nextInt(celebUrl.size());
        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage = null;
        try {
            celebImage = imageTask.execute(celebUrl.get(choosenCeleb)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(celebImage);
        locationOfAnswer = rand.nextInt(4);
        int locationOfIncorrect;
        for(int i = 0; i < 4; i++){
            if(i == locationOfAnswer){
                answers[i] = celebNames.get(choosenCeleb);
            }
            else{
                locationOfIncorrect = rand.nextInt(celebUrl.size());
                while(locationOfIncorrect == choosenCeleb){
                    locationOfIncorrect = rand.nextInt(celebUrl.size());
                }
                answers[i] = celebNames.get(locationOfIncorrect);
            }
        }
        button0.setText(answers[0]);
        button1.setText(answers[1]);
        button2.setText(answers[2]);
        button3.setText(answers[3]);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadTask task = new DownloadTask();
        String result = null;
        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        try{
            result = task.execute("https://www.imdb.com/list/ls052283250").get();
            String[] splitResult = result.split("<div class=\"footer filmosearch\">");
            Pattern p = Pattern.compile("<img alt=\"(.*?) src");  // Search Names
            Matcher m = p.matcher(splitResult[0]);
            while(m.find()){
                  celebNames.add(m.group(1));
            }
            Pattern p3 = Pattern.compile("src=\"\"(.*?)\" width");  //Searches URLS
            Matcher m3 = p3.matcher(splitResult[0]);
            while(m3.find()){
                 celebUrl.add(m3.group(1));
            }
            newQuestion();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}