package com.example.ecare_client;

import android.location.Location;
import com.example.ecare_client.Googlemaps.DirectionFinder;
import com.example.ecare_client.Googlemaps.DirectionFinderListener;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class MapFragmentTesting {
    private static final String GOOGLE_API_KEY = "AIzaSyCzIe_THjJzrwkBEhBnQlQFpq510_wTR88";

    @Test
    public void getDirectionsURLShouldReturnTrueIfCorrectURL() throws Exception {

        String correctURL = "https://maps.googleapis.com/maps/api/directions/json?origin=-37.8,144.959&destination=-37.81,144.9593&sensor=false&mode=walking"
                + "&key=" + GOOGLE_API_KEY;



        Location location1 = Mockito.mock(Location.class);
        Mockito.when(location1.getLatitude()).thenReturn(-37.8);
        Mockito.when(location1.getLongitude()).thenReturn(144.959);
        LatLng origin=new LatLng(location1.getLatitude(),location1.getLongitude());

        Location location2 = Mockito.mock(Location.class);
        Mockito.when(location2.getLatitude()).thenReturn(-37.81);
        Mockito.when(location2.getLongitude()).thenReturn(144.9593);
        LatLng destination=new LatLng(location2.getLatitude(),location2.getLongitude());

        DirectionFinder routingActivity = new DirectionFinder(null,origin,destination);
        String testURL=routingActivity.getUrl(origin,destination);

        assertEquals(correctURL,testURL);

    }
}