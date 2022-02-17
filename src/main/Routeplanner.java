package main;

import com.byteowls.jopencage.model.JOpenCageLatLng;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Arrays;

public class Routeplanner {

    public TextField inputTextField;
    public Button button;
    public Button stopButton;
    public Slider progresSlider;
    public Text resultsText;

    public ListView<Address> adreslijst;

    public TextField addressQueryTextField;
    public AnchorPane changeDataAnchorPane;
    public TextField addressCoordTextField;
    public TextArea infoField;

    public AnchorPane filterAnchorPane;
    public ComboBox<String> paramComboBox;
    public ComboBox<String> valueComboBox;


    Api api;
    FileProcessor fp;
    Address shownAddress;

    Timeline timeline;

    public void initialize(){
        api = new Api();
        fp = new FileProcessor();

        paramComboBox.setId("dropdown");
        valueComboBox.setId("dropdown");

        changeDataAnchorPane.setDisable(true);
        filterAnchorPane.setVisible(false);

        // Set the CellFactory for the ListView
        adreslijst.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Address item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(null);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setId("empty");
                } else {
                    switch (item.getResponseStatus()) {
                        case OK:
                            if(item.getConfidence() < 10){
                                setStyle("-fx-background-color: rgb(255, "+ 25 * (item.getConfidence() - 1)+ ", 0)");
                                setId("WARNING");
                            } else
                                setId("OK");
                            setText("'" + item.getFormattedAddress() + "', confidence " + item.getConfidence());
                            break;
                        case NULL:
                            setId("NULL");
                            setText("Fetching coords for '" + item.getAdresQuery() + "'");
                            break;
                        case ERROR:
                            setId("ERROR");
                            setText("ERROR searching address '" + item.getAdresQuery() + "'");
                            break;
                        case SET:
                            setId("SET");
                            setText("SET coords for '" + item.getAdresQuery() + "'");
                            break;
                    }
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if(selected) {
                    boolean sameValue = super.getItem() == shownAddress;
                    shownAddress = super.getItem();
                    changeDataAnchorPane.setDisable(shownAddress.isFetchingData());
                    String o = "INFO:\n" + shownAddress.order.dumpInfo();
                    switch (super.getItem().getResponseStatus()) {
                        case ERROR:
                            o += "\n\nERROR:\n" + shownAddress.getErrMessage();
                            break;
                        case SET:
                        case OK:
                            JOpenCageLatLng coords = shownAddress.getCoords();
                            o += "\n\nCoords:\nLat: " + coords.getLat() + "\nLng: " + coords.getLng();
                            break;
                        case NULL:
                            o += "\n\nStill fetching coords...";
                            break;
                    }
                    if (!sameValue){
                        addressQueryTextField.setText(shownAddress.getAdresQuery());
                        addressCoordTextField.setText(shownAddress.getCoords() == null ? "" : shownAddress.getCoords().toString());
                    }
                    infoField.setText(o);
                }
            }
        });

        paramComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue != null && !newValue.equals(oldValue)){
                valueComboBox.getItems().clear();
                valueComboBox.getItems().addAll(fp.getPossibleParamValues(newValue));
            }
        });

        valueComboBox.valueProperty().addListener((obs, oldValue, newValue)-> {
            if(newValue != null && !newValue.equals(oldValue)){
                filterAnchorPane.setDisable(true);
                adreslijst.getItems().clear();
                adreslijst.getItems().addAll(fp.loadAddresses(this, paramComboBox.getValue(), valueComboBox.getValue()));
                resultsText.setText("" + adreslijst.getItems().size() + " results");
                filterAnchorPane.setDisable(false);
            }
        });
    }


    public void onClick(){
        if (inputTextField.getText().isEmpty())
            return;
        fp.processFile(inputTextField.getText());
        paramComboBox.getItems().clear();
        paramComboBox.getItems().addAll(fp.getOrderParams());
        filterAnchorPane.setVisible(true);

    }

    public void fetchAllCoords(){
        if (fp.getAddressAmount() == 0){
            System.out.println("FAIL");
            return;
        }
        button.setVisible(false);
        stopButton.setVisible(true);
        progresSlider.setVisible(true);
        progresSlider.setValue(0);
        filterAnchorPane.setDisable(true);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            Thread thread = new Thread(() -> {
                fp.nextAddress().fetchCoords(api);
                adreslijst.refresh();
            });
            thread.start();
            progresSlider.adjustValue(fp.progress());
        }));
        timeline.setCycleCount(fp.getAddressAmount());
        timeline.play();

        timeline.setOnFinished(event -> {
            button.setVisible(true);
            stopButton.setVisible(false);
            progresSlider.setVisible(false);
            filterAnchorPane.setDisable(false);
        });
    }

    public void stopLoadingAddresses(){
        if (timeline != null){
            timeline.stop();
            button.setVisible(true);
            stopButton.setVisible(false);
            progresSlider.setVisible(false);
            filterAnchorPane.setDisable(false);
        }
    }

    public void fetchAddressCoords(){
        if(addressQueryTextField.getText().isEmpty())
           return;
        changeDataAnchorPane.setDisable(true);
        shownAddress.setAdresQuery(addressQueryTextField.getText());
        Thread thread = new Thread(() -> {
            shownAddress.fetchCoords(api);
            adreslijst.refresh();
            changeDataAnchorPane.setDisable(false);
        });
        thread.start();
    }

    public void setCoords(){
        if(addressCoordTextField.getText().isEmpty())
            return;
        Double[] coords = Arrays.stream(addressCoordTextField.getText().replaceAll("\\s", "").split(",")).map(Double::parseDouble).toArray(Double[]::new);
        shownAddress.setCoords(coords[0], coords[1]);
        adreslijst.refresh();
    }


}
