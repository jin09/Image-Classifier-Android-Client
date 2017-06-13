package com.example.gautam.imageclassifier;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button button, button2;
    private String encoded_string, image_name;
    private Bitmap bitmap;
    private File file;
    private Uri file_uri;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA}, 0);
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        getFileUri();
        i.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
        startActivityForResult(i, 10);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getFileUri();
                i.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
                startActivityForResult(i, 10);
            }
        });

    }

    private void getFileUri() {
        image_name = "file.jpg";
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + image_name
        );

        file_uri = Uri.fromFile(file);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 10 && resultCode == RESULT_OK) {
            new makeCompleteRequest().execute();
        }
    }

    class makeCompleteRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Uploading");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String BASE_URL = "http://139.59.69.31/classifier/";
            Log.d(TAG, BASE_URL);
            /**
             bitmap = BitmapFactory.decodeFile(file_uri.getPath());
             ByteArrayOutputStream stream = new ByteArrayOutputStream();
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
             bitmap.recycle();
             byte[] array = stream.toByteArray();
             encoded_string = Base64.encodeToString(array, 0);

             HashMap<String,String> map = new HashMap<>();
             map.put("encoded_string",encoded_string);
             map.put("image_name",image_name);

             String result = MakeRequest.PostRequest(BASE_URL, map);
             */
            String res = null;
            try {
                File sourceFile = new File(file_uri.getPath());

                Log.d(TAG, "File..." + sourceFile + " : " + sourceFile.exists());

                final MediaType MEDIA_TYPE_jpg = MediaType.parse("image/*");

                String filename = file_uri.getPath().substring(file_uri.getPath().lastIndexOf("/") + 1);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", filename, RequestBody.create(MEDIA_TYPE_jpg, sourceFile))
                        .addFormDataPart("result", "my_image")
                        .build();

                Request request = new Request.Builder()
                        .url(BASE_URL)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                okhttp3.Response response = client.newCall(request).execute();
                res = response.body().string();
                Log.e("TAG", "Response : " + res);
                return res;

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e("TAG", "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e("TAG", "Other Error: " + e.getLocalizedMessage());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
            if(s != null) {
                Log.d(TAG, s);
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(s)
                    .setPositiveButton("New Capture", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            getFileUri();
                            i.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
                            startActivityForResult(i, 10);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}
