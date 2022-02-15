package main;

import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import com.byteowls.jopencage.model.JOpenCageStatus;

public class Address {
    private String adresQuery;
    private JOpenCageResponse adresResult;
    public Order order;


    public Address(Order order){
        this.order = order;
        this.adresQuery = order.address + " " + order.city + " " + order.postcode + " Belgium";
    }

    public String getAdresQuery() {
        return adresQuery;
    }

    public ResponseStatus getResponseStatus(){
        return adresResult == null ? ResponseStatus.NULL : adresResult.getStatus().getCode() == 200 ? ResponseStatus.OK : ResponseStatus.ERROR;
    }

    public String getFormattedAddress(){
        return adresResult == null ? "" : adresResult.getResults().get(0).getFormatted();
    }

    public int getConfidence(){
        return adresResult == null ? 0 : adresResult.getResults().get(0).getConfidence();
    }

    public String getErrMessage(){
        return adresResult == null || adresResult.getTotalResults() > 0 ? "" : adresResult.getStatus().getMessage();
    }

    public JOpenCageLatLng getCoords(){
        return adresResult == null ? null : adresResult.getFirstPosition();
    }

    public void fetchCoords(Api api){
        adresResult = api.getCoord(adresQuery);
        adresResult.orderResultByConfidence();
    }
}

enum ResponseStatus{
    ERROR,
    OK,
    NULL
}

class Order {
    public String orderNumber;
    public String orderStatus;
    public String orderDate;
    public String customerNote;
    public String firstName;
    public String lastName;

    public String address;
    public String city;
    public String postcode;

    public String email;
    public String phone;

    public String shippingMethodTitle;
    public int productQty;

    public String dumpInfo(){
        return orderNumber + "\n" + orderStatus + "\n" + orderDate + "\n" + customerNote + "\n" + firstName + "\n" + lastName + "\n" + address + "\n" + city + "\n" + postcode + "\n" + email + "\n" + phone + "\n" + shippingMethodTitle;
    }
}
