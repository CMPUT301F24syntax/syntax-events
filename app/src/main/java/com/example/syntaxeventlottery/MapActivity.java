package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private MapView mapView;
    private GoogleMap googleMap;
    private String eventID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 获取从 EventDetailActivity 传递的 eventID
        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null || eventID.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化 MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); // 异步加载地图
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "Map is ready");

        // 加载参与者位置
        loadParticipantLocations();
    }

    private void loadParticipantLocations() {
        // 假设有一个控制器从数据库获取位置数据
        List<LatLng> participantLocations = getDummyParticipantLocations();

        for (LatLng location : participantLocations) {
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Participant"));
        }

        // 将相机移动到第一个参与者的位置
        if (!participantLocations.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(participantLocations.get(0), 10));
        }
    }

    private List<LatLng> getDummyParticipantLocations() {
        // 假设数据来自数据库或 API
        List<LatLng> locations = new ArrayList<>();
        locations.add(new LatLng(53.5461, -113.4938)); // 示例位置
        locations.add(new LatLng(51.0447, -114.0719)); // 示例位置
        return locations;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
