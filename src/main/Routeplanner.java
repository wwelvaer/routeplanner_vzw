package main;

import com.byteowls.jopencage.model.JOpenCageLatLng;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Routeplanner {

    public TextField inputTextField;
    public Text outputText;
    public Button button;
    public ListView<Address> adreslijst;
    public Slider progresSlider;
    public TextArea infoField;

    Api api;
    FileProcessor fp;

    public void initialize(){
        api = new Api();
        fp = new FileProcessor();

        // Set the CellFactory for the ListView
        adreslijst.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Address item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle(null);
                    setText(null);
                } else {
                    switch (item.getResponseStatus()) {
                        case OK:
                            setStyle(item.getConfidence() < 10 ? "-fx-background-color: orange" : "");
                            setText("searched '" + item.getAdresQuery() + "', found '" + item.getFormattedAddress() + "' with confidence " + item.getConfidence());
                            break;
                        case NULL:
                            setText("Fetching coords for '" + item.getAdresQuery() + "'");
                            break;
                        case ERROR:
                            setStyle("-fx-background-color: red; -fx-text-fill: white");
                            setText("ERROR searching address '" + item.getAdresQuery() + "'");
                            break;
                    }
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if(selected) {
                    String o = "INFO:\n" + super.getItem().order.dumpInfo();
                    switch (super.getItem().getResponseStatus()) {
                        case ERROR:
                            o += "\n\nERROR:\n" + super.getItem().getErrMessage();
                            break;
                        case OK:
                            JOpenCageLatLng coords = super.getItem().getCoords();
                            o += "\n\nCoords:\nLat: " + coords.getLat() + "\nLng: " + coords.getLng();
                            break;
                        case NULL:
                            o += "\n\nStill fetching coords...";
                    }
                    infoField.setText(o);
                }
            }
        });


    }


    public void onClick(){
        if (inputTextField.getText().length() == 0)
            return;
        fp.processFile(inputTextField.getText(), this);
    }

    public void loadAddresses(){
        if (fp.getAddressAmount() == 0){
            System.out.println("FAIL");
            return;
        }
        button.setDisable(true);
        progresSlider.setVisible(true);
        progresSlider.setValue(0);
        adreslijst.getItems().clear();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            Address addr = fp.nextAddress();
            adreslijst.getItems().add(addr);
            Thread thread = new Thread(() -> addr.fetchCoords(api));
            thread.start();
            progresSlider.adjustValue(fp.progress());
        }));
        timeline.setCycleCount(fp.getAddressAmount());
        timeline.play();
        timeline.setOnFinished(event -> {button.setDisable(false);progresSlider.setVisible(false);});
    }


}
