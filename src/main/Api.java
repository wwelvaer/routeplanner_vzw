package main;

import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageResponse;

public class Api {

    //String openCageApiURL = "https://api.opencagedata.com/geocode/v1/json";
    String openCageApiKey = "d36914a9023c42958e5c501a3c49aeec";

    //String routeXLApiURL = "https://api.routexl.com/";

    JOpenCageGeocoder geoEncoder;


    public Api(){
        geoEncoder = new JOpenCageGeocoder(openCageApiKey);
    }

    public JOpenCageResponse getCoord(String addr){
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(addr);
        request.setMinConfidence(1);
        return geoEncoder.forward(request);
    }
}
