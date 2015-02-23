package game.bubble.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Settings {

    TextField hostField = new TextField();
    TextField portField = new TextField();
    TextField tokenField = new TextField();

    HBox settingsPanel;

    public String host() {
        return hostField.getText();
    }

    public int port() {
        String portText = portField.getText();
        return portText.matches("^\\d{1,5}$") ? Integer.valueOf(portText) : 0;
    }

    public String token() {
        return tokenField.getText();
    }

    public HBox getPanel() {
        if (settingsPanel == null) {
            settingsPanel = init();
        }

        return settingsPanel;
    }

    public HBox init() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(50, 25, 25, 25));

        Text title = new Text("Config");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(title, 0, 0, 2, 1);

        Label hostLabel = new Label("Host:");
        grid.add(hostLabel, 0, 1);

        grid.add(hostField, 1, 1);

        Label portLabel = new Label("Port:");
        grid.add(portLabel, 0, 2);

        grid.add(portField, 1, 2);

        Label tokenLabel = new Label("Token:");
        grid.add(tokenLabel, 0, 3);

        grid.add(tokenField, 1, 3);
        return new HBox(grid);
    }

}
