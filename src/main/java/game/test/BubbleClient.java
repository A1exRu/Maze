package game.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BubbleClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Context.udpClient.start();
        stage.setTitle("Shop Management");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        Pane myPane = loader.load();
        Scene myScene = new Scene(myPane);
        stage.setScene(myScene);
        stage.show();
        stage.setResizable(false);
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        Context.udpClient.close();
    }
    
    public static class Context {
        public static final UdpClient udpClient = new UdpClient("localhost", 9187);
    }
}
