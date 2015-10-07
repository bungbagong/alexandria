package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private String eanString;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    private final String LOG_TAG = AddBook.class.getSimpleName();

    public BookResult bookResult;
    static final int SCAN_RESULT= 12;


    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if(resultCode == Activity.RESULT_OK){

                ean.setText(data.getStringExtra("scanResult"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean =s.toString();
                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean="978"+ean;
                }
                if(ean.length()<13){
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent

                /*
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
                */

                AddBookQuery bookQuery = new AddBookQuery();
                bookQuery.execute(ean);
                eanString=ean;



            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                /*
                Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
*/
                Intent scanIntent = new Intent(getActivity(), SimpleZBarActivity.class);
                startActivityForResult(scanIntent, SCAN_RESULT);

            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanString);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }



        /*
        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);

        */
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }


    public class AddBookQuery extends AsyncTask<String, Void, BookResult>{


        ArrayList<String> bookAuthors = new ArrayList<String>();
        ArrayList<String> bookCategories = new ArrayList<String>();


        @Override
        protected void onPostExecute(BookResult bookResult) {

            if(bookResult == null ) {
                Toast.makeText
                        (getActivity(),"No book with this ISBN number can be found",
                                Toast.LENGTH_LONG).show();

            }
            else {

                String bookTitle = bookResult.getTitle();
                ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

                String bookSubTitle = bookResult.getSubtitle();
                ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);


                ArrayList<String> authorArray = bookResult.getAuthors();
                String authors="";
                for(String i :authorArray){
                    authors+=i;
                    authors+=",";
                }

                ((TextView) rootView.findViewById(R.id.authors)).setLines(authorArray.size());
                ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));


                String imgUrl = bookResult.getImgUrl();
                if(Patterns.WEB_URL.matcher(imgUrl).matches()){
                    new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
                    rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
                }


                ArrayList<String> categoryArray = bookResult.getCategories();
                String categories ="";
                for(String i: categoryArray){
                    categories+=i;
                    categories+=", ";
                }

                ((TextView) rootView.findViewById(R.id.categories)).setLines(categoryArray.size());
                ((TextView) rootView.findViewById(R.id.categories)).setText(categories.replace(",","\n"));



                rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);


            }
        }

        @Override
        protected BookResult doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String bookJsonString = null;

            try {
                final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
                final String QUERY_PARAM = "q";

                final String ISBN_PARAM = "isbn:" + params[0];

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                bookJsonString = buffer.toString();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }

            }

            final String ITEMS = "items";

            final String VOLUME_INFO = "volumeInfo";

            final String TITLE = "title";
            final String SUBTITLE = "subtitle";
            final String AUTHORS = "authors";
            final String DESC = "description";
            final String CATEGORIES = "categories";
            final String IMG_URL_PATH = "imageLinks";
            final String IMG_URL = "thumbnail";

            try {
                JSONObject bookJson = new JSONObject(bookJsonString);
                JSONArray bookArray;
                if(bookJson.has(ITEMS)){
                    bookArray = bookJson.getJSONArray(ITEMS);
                }else{
                    //Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                    //messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
                    //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                    return null;
                }

                JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

                String title = bookInfo.getString(TITLE);

                String subtitle = "";
                if(bookInfo.has(SUBTITLE)) {
                    subtitle = bookInfo.getString(SUBTITLE);
                }

                String desc="";
                if(bookInfo.has(DESC)){
                    desc = bookInfo.getString(DESC);
                }

                String imgUrl = "";
                if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                    imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
                }





                if(bookInfo.has(AUTHORS)) {

                    JSONArray jsonArray = bookInfo.getJSONArray(AUTHORS);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        bookAuthors.add(jsonArray.getString(i));
                    }
                }

                if(bookInfo.has(CATEGORIES)) {

                    JSONArray jsonArray = bookInfo.getJSONArray(CATEGORIES);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        bookCategories.add(jsonArray.getString(i));
                    }
                }

                bookResult = new BookResult(title,subtitle,desc,imgUrl,bookCategories, bookAuthors);

                Log.d("bungbagong",bookResult.toString());


                } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            }






            return bookResult;
        }


    }



}
