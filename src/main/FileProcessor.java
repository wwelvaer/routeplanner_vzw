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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FileProcessor {

    ArrayList<Address> addresses;
    int currIndex = -1;
    NodeList orders;

    public void processFile (String fileName){
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
            orders = doc.getElementsByTagName("Order");
            System.out.println("List length: " + orders.getLength());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getOrderParams(){
        if (orders == null)
            return null;
        int i = 0;
        while(orders.item(i).getNodeType() != Node.ELEMENT_NODE)
            i++;
        NodeList l = orders.item(i).getChildNodes();
        String[] s = new String[l.getLength()];
        for(int j = 0; j < l.getLength(); j++)
            s[j] = l.item(j).getNodeName();
        return s;
    }

    public String[] getPossibleParamValues(String param){
        Set<String> set = new HashSet<>();
        set.add("");
        for (int i = 0; i < orders.getLength(); i++) {
            Node node = orders.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                set.add(((Element) node).getElementsByTagName(param).item(0).getTextContent());
        }
        return  set.toArray(String[]::new);
    }

    public ArrayList<Address> loadAddresses(Routeplanner rp, String param, String value){
        addresses.clear();
        for (int i = 0; i < orders.getLength(); i++) {
            Node node = orders.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (!param.isEmpty() && !element.getElementsByTagName(param).item(0).getTextContent().equals(value))
                    continue;
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

                NodeList products = element.getElementsByTagName("Products");
                for(int j = 0; j < products.getLength(); j++) if(products.item(j).getNodeType() == Node.ELEMENT_NODE){
                    Element el = (Element) products.item(j);
                    Product p = new Product(Integer.parseInt(el.getElementsByTagName("Qty").item(0).getTextContent()), el.getElementsByTagName("Name").item(0).getTextContent());
                    order.products.add(p);
                }
                addresses.add(new Address(order));
            }
        }
        return addresses;
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
