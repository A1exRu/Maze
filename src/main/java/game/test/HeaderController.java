package game.test;

import game.test.client.Context;
import game.test.client.MessageHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

public class HeaderController {
    
    private static final UdpClient udpClient = Context.udpClient;
    
    private MessageHandler authHandler;
    
    private Node settingsNode;
    private Collection<Node> prevNodes;

    @FXML
    private TextField notification;

    @FXML
    public void onAuth(ActionEvent event) {
        if (authHandler == null) {
            authHandler = new MessageHandler() {
                @Override
                public int getCode() {
                    return 1;
                }

                @Override
                public void handle(ByteBuffer response) {
                    final int status = response.getInt();
                    Platform.runLater(() -> notification.setText("Auth status: " + status));
                }
            };
            udpClient.addMessageHandler(authHandler);
        }
        udpClient.auth(Context.authToken);
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
        Pane main = (Pane)((Node) event.getSource()).getScene().lookup("#main");
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
