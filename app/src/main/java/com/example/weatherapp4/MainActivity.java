package com.example.weatherapp4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    TextView textResult;
    EditText editText;
    Button searchBtn;
    ImageView imgWeather;
    final static int REQUEST = 112;
    LocationManager locationManager;


    static class Weather extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... address) {

            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int pom1 = inputStreamReader.read();
                String jsonContent = "";
                char pom2;
                while (pom1 != -1) {
                    pom2 = (char) pom1;
                    jsonContent = jsonContent + pom2;
                    pom1 = inputStreamReader.read();
                }
                return jsonContent;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public void search() {


        try {
            String cName = editText.getText().toString();

            String jsonContent;
            Weather weather = new Weather();

            jsonContent = weather.execute("https://openweathermap.org/data/2.5/weather?q=" +
                    cName + "&appid=b6907d289e10d714a6e88b30761fae22").get();

            JSONObject jsonObject = new JSONObject(jsonContent);
            String weatherInfo = jsonObject.getString("weather");
            String mainTemp = jsonObject.getString("main");
            double visibility;

            JSONArray array = new JSONArray(weatherInfo);

            String main = "";
            String description = "";
            String temperature = "";
            String icon = "";

            for (int i = 0; i < array.length(); i++) {
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString("main");
                description = weatherPart.getString("description");
                icon = weatherPart.getString("icon");

            }

            JSONObject mainPart = new JSONObject(mainTemp);
            temperature = mainPart.getString("temp");

            visibility = Double.parseDouble(jsonObject.getString("visibility"));
            int visKM = (int) visibility / 1000;

            String iconPom = "https://openweathermap.org/img/w/" + icon + ".png";


            String resultText = "Main: " + main +
                    "\nDescription: " + description +
                    "\nTemperature: " + temperature + (char) 0x00B0 + "C" +
                    "\nVisibility: " + visKM + " km";
            Glide
                    .with(MainActivity.this)
                    .load(iconPom)
                    .centerCrop()
                    .into(imgWeather);

            textResult.setText(resultText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setComponents();
        getLocation();
        search();
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    private void setComponents() {

        editText = findViewById(R.id.edit_search);
        searchBtn = findViewById(R.id.btn_search);
        textResult = findViewById(R.id.txt_results);
        imgWeather = findViewById(R.id.img_weather);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    private void getLocation() {

        String pomS;
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {

                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        pomS = addresses.get(0).getLocality();
                        editText.setText(pomS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    search();
                    return;
                } else {
                    search();
                    Toast.makeText(this, "Required permissions are not granted!", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }
}

