package org.docbook.controllers.records;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class MapController {

    @FXML private WebView mapWebView;
    private String selectedAddress = "";
    private java.util.function.Consumer<String> onAddressSelected;
    private String previousView = "/fxml/doctor/DoctorDashboard.fxml";

    public void setPreviousView(String view) {
        this.previousView = view;
    }

    public void setOnAddressSelected(java.util.function.Consumer<String> callback) {
        this.onAddressSelected = callback;
    }

    @FXML
    public void initialize() {
        WebEngine engine = mapWebView.getEngine();
        
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <style>#map { height: 100vh; width: 100%; margin: 0; padding: 0; cursor: crosshair; }</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([36.8065, 10.1815], 13);\n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);\n" +
                "        var marker;\n" +
                "        map.on('click', function(e) {\n" +
                "            if (marker) map.removeLayer(marker);\n" +
                "            marker = L.marker(e.latlng).addTo(map);\n" +
                "            fetch(`https://nominatim.openstreetmap.org/reverse?lat=${e.latlng.lat}&lon=${e.latlng.lng}&format=json`)\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    window.javaConnector.setAddress(data.display_name);\n" +
                "                });\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });

        engine.loadContent(html);
    }

    public class JavaConnector {
        public void setAddress(String address) {
            selectedAddress = address;
        }
    }

    @FXML
    private void confirmAddress() {
        if (onAddressSelected != null && !selectedAddress.isEmpty()) {
            onAddressSelected.accept(selectedAddress);
        }
        goBack(null);
    }

    @FXML
    private void close() {
        goBack(null);
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(previousView));
            Stage stage = (Stage) mapWebView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            ((Stage) mapWebView.getScene().getWindow()).close();
        }
    }
}