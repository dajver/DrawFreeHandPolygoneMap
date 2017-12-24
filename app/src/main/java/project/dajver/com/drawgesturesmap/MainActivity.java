package project.dajver.com.drawgesturesmap;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import project.dajver.com.drawgesturesmap.map.MySupportMapFragment;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnTouchListener {

    private GoogleMap googleMap;

    private int source = 0;
    private int destination = 1;

    private boolean isMapMoveable = false;
    private boolean screenLeave = false;

    private ArrayList<LatLng> latLngArrayList = new ArrayList<>();

    @BindView(R.id.fram_map)
    FrameLayout framMap;
    @BindView(R.id.drawBtn)
    Button drawBtn;
    @BindColor(R.color.colorPrimary)
    int colorPrimary;
    @BindColor(R.color.transparentGray)
    int transparentGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MySupportMapFragment customMapFragment = ((MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        customMapFragment.getMapAsync(this);
    }

    @OnClick(R.id.drawBtn)
    public void onDrawClick() {
        isMapMoveable = true;
        drawBtn.setVisibility(View.GONE);
        latLngArrayList.removeAll(latLngArrayList);
        googleMap.clear();
    }

    public void drawMap() {
        if (latLngArrayList.size() > 1) {
            googleMap.addPolyline(new PolylineOptions().add(
                    latLngArrayList.get(source),
                    latLngArrayList.get(destination))
                    .width(20)
                    .color(colorPrimary)
            );
            source++;
            destination++;
        }
    }

    private List<LatLng> createOuterBounds() {
        final float delta = 0.01f;

        return new ArrayList<LatLng>() {{
            add(new LatLng(90 - delta, -180 + delta));
            add(new LatLng(0, -180 + delta));
            add(new LatLng(-90 + delta, -180 + delta));
            add(new LatLng(-90 + delta, 0));
            add(new LatLng(-90 + delta, 180 - delta));
            add(new LatLng(0, 180 - delta));
            add(new LatLng(90 - delta, 180 - delta));
            add(new LatLng(90 - delta, 0));
            add(new LatLng(90 - delta, -180 + delta));
        }};
    }


    private void drawFinalPolygon() {
        latLngArrayList.add(latLngArrayList.get(0));

        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(transparentGray);
        polygonOptions.addAll(createOuterBounds());
        polygonOptions.strokeColor(colorPrimary);
        polygonOptions.strokeWidth(20);
        polygonOptions.addHole(latLngArrayList);

        Polygon polygon = googleMap.addPolygon(polygonOptions);

        for(LatLng latLng : polygon.getPoints()) {
            Log.e("latitude", "" + latLng.latitude);
            Log.e("longitude", "" + latLng.longitude);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        framMap.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (isMapMoveable) {
            Point point = new Point(Math.round(event.getX()), Math.round(event.getY()));
            LatLng latLng = googleMap.getProjection().fromScreenLocation(point);
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;

            int eventaction = event.getAction();
            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:
                    screenLeave = false;
                case MotionEvent.ACTION_MOVE:
                    latLngArrayList.add(new LatLng(latitude, longitude));
                    screenLeave = false;
                    drawMap();
                case MotionEvent.ACTION_UP:
                    if (!screenLeave) {
                        screenLeave = true;
                    } else {
                        isMapMoveable = false;
                        source = 0;
                        destination = 1;
                        drawBtn.setVisibility(View.VISIBLE);
                        drawFinalPolygon();
                    }
                    break;
                default:
                    break;
            }

            if (isMapMoveable) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
