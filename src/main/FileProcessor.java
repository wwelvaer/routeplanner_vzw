package main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileProcessor {

    ArrayList<Address> addresses;
    int currIndex = -1;

    public void processFile (String fileName, Routeplanner rp){
        currIndex = -1;
        addresses = new ArrayList<>();
        // process xml file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try{
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(fileName));
            doc.getDocumentElement().normalize();

            // get <order>
            NodeList list = doc.getElementsByTagName("Order");
            System.out.println("List length: " + list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Order order = new Order();
                    order.orderNumber = element.getElementsByTagName("Order_Number").item(0).getTextContent();
                    order.orderStatus = element.getElementsByTagName("Order_Status").item(0).getTextContent();
                    order.orderDate = element.getElementsByTagName("Order_Date").item(0).getTextContent();
                    order.customerNote = element.getElementsByTagName("Customer_Note").item(0).getTextContent();
                    order.firstName = element.getElementsByTagName("Shipping_First_Name").item(0).getTextContent();
                    order.lastName = element.getElementsByTagName("Shipping_Last_Name").item(0).getTextContent();
                    order.address = element.getElementsByTagName("Shipping_Address").item(0).getTextContent();
                    order.city = element.getElementsByTagName("Shipping_City").item(0).getTextContent();
                    order.postcode = element.getElementsByTagName("Shipping_Postcode").item(0).getTextContent();
                    order.email = element.getElementsByTagName("Billing_Email").item(0).getTextContent();
                    order.phone = element.getElementsByTagName("Billing_Phone").item(0).getTextContent();
                    order.shippingMethodTitle = element.getElementsByTagName("Shipping_Method_Title").item(0).getTextContent();
                    //order.productQty = element.getElementsByTagName("Order_Number").item(0).getTextContent();
                    addresses.add(new Address(order));
                }
            }
            rp.loadAddresses();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public Address nextAddress(){
        currIndex++;
        if (currIndex > getAddressAmount()-1)
            throw new AssertionError("No next address!");
        return addresses.get(currIndex);
    }

    public int getAddressAmount(){
        return addresses.size();
    }

    public double progress(){
        return currIndex < 0 ? 0 : (double)(currIndex+1) / getAddressAmount();
    }

}
