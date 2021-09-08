package org.tensorflow.serving.examples.resnet;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "TFServingDemo";
    private final int INPUT_IMG_HEIGHT = 720;
    private final int INPUT_IMG_WIDTH = 498;
    private final String TEST_IMG_NAME = "cat.jpg";
    private final String LABEL_FILE_NAME = "labels.txt";
    private final int NUMBER_OF_LABELS = 1001;

    private final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    private Button predictButton;
    private TextView responseTextView;
    private ImageView inputImageView;
    private Bitmap inputImgBitmap;

    Request createRequest() {
        int[] inputImg = new int[INPUT_IMG_HEIGHT * INPUT_IMG_WIDTH];
        double[][][][] inputImgRGB = new double[1][INPUT_IMG_HEIGHT][INPUT_IMG_WIDTH][3];
        inputImgBitmap.getPixels(inputImg, 0, INPUT_IMG_WIDTH, 0, 0, INPUT_IMG_WIDTH, INPUT_IMG_HEIGHT);
        int pixel;
        for(int i=0; i<INPUT_IMG_HEIGHT; i++) {
            for (int j = 0; j < INPUT_IMG_WIDTH; j++) {
                // Extract RBG values from each pixel; alpha is ignored
                    pixel = inputImg[i * INPUT_IMG_WIDTH + j];
                    inputImgRGB[0][i][j][0] = ((pixel >> 16) & 0xff) / 255.0;
                    inputImgRGB[0][i][j][1] = ((pixel >> 8) & 0xff) / 255.0;
                    inputImgRGB[0][i][j][2] = ((pixel) & 0xff) / 255.0;
            }
        }
        RequestBody requestBody = RequestBody.create("{\"instances\": " + Arrays.deepToString(inputImgRGB) + "}", JSON);

        // Using Android emulator so we use 10.0.2.2 here, which is the special alias to your host loopback interface
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8501/v1/models/resnet:predict")
                .post(requestBody)
                .build();
        return request;
    }

    String lookupLabel(int maxIndex) {
        String[] labels = new String[NUMBER_OF_LABELS];
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(LABEL_FILE_NAME);
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferReader.readLine();
            int i = 0;
            while(line != null) {
                labels[i] = line;
                line = bufferReader.readLine();
                i++;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading label file");
            return null;
        }
        return labels[maxIndex+1];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        predictButton = findViewById(R.id.predict_button);
        responseTextView = findViewById(R.id.response_tv);
        inputImageView = findViewById(R.id.input_iv);
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(TEST_IMG_NAME);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        inputImgBitmap = BitmapFactory.decodeStream(inputStream);
        inputImageView.setImageBitmap(inputImgBitmap);

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Request request= createRequest();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = jsonObject.getJSONArray("predictions");

                    // We are sending only one image
                    int maxIndex = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // Argmax
                        JSONArray probabilities = jsonArray.getJSONArray(0);
                        for (int j = 1; j < probabilities.length(); j++) {
                            maxIndex = probabilities.getDouble(j) >  probabilities.getDouble(maxIndex+1) ? j : maxIndex;
                        }
                    }
                    responseTextView.setText("Predicted label:\n\n" + String.valueOf(maxIndex) + " - " + lookupLabel(maxIndex));
                } catch (IOException | JSONException e) {
                    Log.e(TAG, e.getMessage());
                    responseTextView.setText(e.getMessage());
                    return;
                }
            }
        });
    }
}