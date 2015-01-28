package game.test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class HeaderController {
    
    private static final UdpClient udpClient = BubbleClient.Context.udpClient;
    
    private Node settingsNode;
    private Collection<Node> prevNodes;

    @FXML
    private TextField notification;

    @FXML
    public void onAuth(ActionEvent event) {
        notification.setText("Auth button");
    }
    
    @FXML
    public void onPing(ActionEvent event) {
        udpClient.ping((p, o) -> {
            long res = o - p;
            Platform.runLater(() -> notification.setText("Ping time: " + res));
        });
    }

    @FXML
    public void onSettings(ActionEvent event) throws IOException {
        Pane main = (Pane)((Node) event.getSource()).getScene().lookup("#battlefield");
        if (prevNodes == null) {
            prevNodes = new ArrayList<>(main.getChildren());
            main.getChildren().clear();
            main.getChildren().add(getSettingsNode());
        } else {
            main.getChildren().clear();
            main.getChildren().addAll(prevNodes);
            prevNodes = null;
        }
    }
    
    private Node getSettingsNode() throws IOException {
        if (settingsNode == null) {
            settingsNode = FXMLLoader.load(getClass().getResource("settings.fxml"));
        }
        
        return settingsNode;
    }

}
