package com.example.abdulqani.newsreaderapp1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //array-list of strings for titles and contents
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();


    ArrayAdapter arrayAdapter;
    SQLiteDatabase articlesDb;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.stories) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            startActivity(intent);
            return true;

        } else if (item.getItemId() == R.id.savedStories) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            startActivity(intent);
            return true;
        }

        return false;
    }


    //Dialog box




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //listView
        ListView listView = (ListView) findViewById(R.id.listView);
        //set it up here.using main activity(this)
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);

        //set listview to array adpater
        listView.setAdapter(arrayAdapter);

        //set on item click listener of webView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("content", content.get(i));

                startActivity(intent);
            }
        });

        //set  the database
        articlesDb = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articlesDb.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");
        //update listView
        updateListView();

        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //update listView
    //it is called in on create method and when the download is finished
    public void updateListView() {


        Cursor c = articlesDb.rawQuery("SELECT * FROM articles", null);
        //indexes
        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        //if the database return anything and if does,clear title and content
        if (c.moveToFirst()) {
            titles.clear();
            content.clear();

            //add title and content

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));
            }
            //keep doing until we can go to the next item
            while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
    }



    //Set up a download task.
    public class DownloadTask extends AsyncTask<String, Void, String> {


        //this method is implemented
        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;


            //Create url method from string in the class
            try {
                //Create url method from string in the method
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();


                //inputStream  is a bridge from byte streams to character streams:
                // It reads bytes and decodes them into characters using a specified
                // charset. The charset that it uses may be specified by name or may be given explicitly,
                // or the platform's default charset may be accepted.

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }
                Log.i("URLContent", result);

                //after downloading url content
                //next task convert it a form that can be used
                JSONArray jsonArray = new JSONArray(result);

                int numberOfItems = 20;

                if (jsonArray.length() < 20) {

                    numberOfItems = jsonArray.length();

                }

                //clear the table before any data is added to it
                articlesDb.execSQL("DELETE FROM articles");
                for (int i = 0; i < numberOfItems; i++) {

                    String articleId = jsonArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");

                    urlConnection = (HttpURLConnection) url.openConnection();

                    in = urlConnection.getInputStream();

                    reader = new InputStreamReader(in);

                    data = reader.read();

                    String articleInfo = "";

                    while (data != -1) {
                        char current = (char) data;

                        articleInfo += current;

                        data = reader.read();
                    }
                    Log.i("articleInfo", articleInfo);


                    //using JsonObject to get title and url
                    JSONObject jsonObject = new JSONObject(articleInfo);

                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {

                        String articleTitle = jsonObject.getString("title");

                        String articleURL = jsonObject.getString("url");

                        url = new URL(articleURL);

                        urlConnection = (HttpURLConnection) url.openConnection();

                        in = urlConnection.getInputStream();

                        reader = new InputStreamReader(in);

                        data = reader.read();

                        String articleContent = "";

                        while (data != -1) {
                            char current = (char) data;

                            articleInfo += current;

                            data = reader.read();
                        }

                        Log.i("articleContent", articleContent);

                        String sql = "INSERT INTO articles (articleId, title, content) VALUES (?, ?, ?)";

                        //set a statement and bind a string to it.
                        SQLiteStatement statement = articlesDb.compileStatement(sql);

                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleContent);

                        statement.execute();

                    }

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        //when the process in download task is completed
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }

    }
}


