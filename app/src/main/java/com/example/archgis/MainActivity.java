package com.example.archgis;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = findViewById(R.id.mapView);
//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);


        Basemap.Type basemapType = Basemap.Type.TOPOGRAPHIC_VECTOR;
        final ArcGISMap map = new ArcGISMap(basemapType, 40.72, -74.00, 11);

        // Add the overlay to map view
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        // Create the geographic location for New York, New York, USA
        Point marker = new Point(-74, 40.72, SpatialReferences.getWgs84());

        // Create the marker symbol to be displayed at the location
        SimpleMarkerSymbol sms =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,  Color.RED, 20);

        // Create attributes for the graphic
        Map attributes = new HashMap();
        attributes.put("city","New York");
        attributes.put("country","USA");


        // Add the point, symbol and attributes to the graphics overlay
        Graphic g = new Graphic(marker, attributes, sms );
        graphicsOverlay.getGraphics().add(g);

        mMapView.setMap(map);

        //Display information by Callout
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mMapView){
            public boolean onSingleTapConfirmed(MotionEvent e) {
                final android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());


                // identify graphics on the graphics overlay
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic =
                        mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false, 2);

                identifyGraphic.addDoneListener(new Runnable() {

                    public void run() {
                        try {
                            IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
                            // get the list of graphics returned by identify graphic overlay
                            List<Graphic> graphics = grOverlayResult.getGraphics();
                            Callout mCallout = mMapView.getCallout();

                            if (mCallout.isShowing()) {
                                mCallout.dismiss();
                            }

                            if (!graphics.isEmpty()) {
                                // get callout, set content and show
                                String city = graphics.get(0).getAttributes().get("city").toString();
                                String country = graphics.get(0).getAttributes().get("country").toString();
                                TextView calloutContent = new TextView(getApplicationContext());
                                calloutContent.setText(city + ", " + country);
                                Point mapPoint = mMapView.screenToLocation(screenPoint);

                                mCallout.setLocation(mapPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();

                            }
                        } catch (InterruptedException | ExecutionException ie) {
                            ie.printStackTrace();
                        }

                    }
                });

                return super.onSingleTapConfirmed(e);
            }
        });
    }

    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}
