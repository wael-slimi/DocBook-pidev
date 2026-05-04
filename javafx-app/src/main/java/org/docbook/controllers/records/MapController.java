package org.docbook.controllers.records;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class MapController {

    @FXML private WebView mapWebView;
    private String selectedAddress = "";
    private java.util.function.Consumer<String> onAddressSelected;

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
        close();
    }

    @FXML
    private void close() {
        ((Stage) mapWebView.getScene().getWindow()).close();
    }
}