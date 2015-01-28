package game.test;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BubbleClient extends Application {

    private Stage stage;
    
    private static final Duration TRANSLATE_DURATION = Duration.seconds(5.25);
    private HBox header;
    private HBox configPanel;

    private Map<Long, Player> players = new ConcurrentHashMap<>();
    private Group main;
    private Group config;
    private Scene scene;
    private Settings settings = new Settings();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Context.udpClient.start();
        this.stage = stage;
//        stage.setMaxWidth(600);
//        stage.setMaxHeight(400);
//        stage.setResizable(false);
//
//        header = createHeader();
//        main = new Group(header);
//        scene = new Scene(main, 600, 400, Color.CORNSILK);
//
//        stage.setScene(scene);
//        stage.show();
//        udpClient.start();
        stage.setTitle("Shop Management");

        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("home.fxml"));
        Pane myPane = (Pane)myLoader.load();

//        HeaderController controller = (HeaderController) myLoader.getController();
//
//        controller.setStage(stage);

        Scene myScene = new Scene(myPane);
        stage.setScene(myScene);
        stage.show();
        stage.setResizable(false);
    }

    private Circle createCircle(double x, double y, Color color) {
        final Circle circle = new Circle(x, y, 25, color);
        circle.setOpacity(0.7);
        return circle;
    }

    private TranslateTransition createTranslateTransition(final Circle circle) {
        final TranslateTransition transition = new TranslateTransition(TRANSLATE_DURATION, circle);
        transition.setOnFinished(t -> {
            circle.setCenterX(circle.getTranslateX() + circle.getCenterX());
            circle.setCenterY(circle.getTranslateY() + circle.getCenterY());
            circle.setTranslateX(0);
            circle.setTranslateY(0);
        });
        return transition;
    }

    private void moveCircleOnKeyPress(Scene scene, final TranslateTransition transition) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE:
                    transition.stop();
                    break;
            }
        });
    }

    private void moveCircleOnMousePress(Scene scene, final Circle circle) {
        scene.setOnMousePressed(event -> onMessage(1, event.getSceneX(), event.getScreenY(), circle.getCenterX(), circle.getCenterY()));
    }
    
    public void onAuth(final long playerId) {

        Platform.runLater(() -> {
            System.out.println("add player " + playerId);
            long delta = 50 + 50 * playerId;
            Color color = Color.YELLOW;
            switch ((int)playerId) {
                case 0: break;
                case 1: color = Color.RED; break;
                case 2: color = Color.DARKGREEN; break;
                case 3: color = Color.PURPLE; break;
            }

            Circle circle = createCircle(delta, delta, color);
            final TranslateTransition transition = createTranslateTransition(circle);
            moveCircleOnKeyPress(scene,  transition);
            moveCircleOnMousePress(scene, circle);
            Player player = new Player(circle, transition);
            players.put(playerId, player);
            main.getChildren().add(circle);
        });
    }
    
    public void onMessage(long playerId, double x, double y, double cx, double cy) {
        Player player = players.get(playerId);
        if (player != null) {
            player.transition.setToX(x - cx);
            player.transition.setToY(y - cy);
            player.transition.playFromStart();
        }
    }

    private static class Player {
        Circle circle;
        TranslateTransition transition;

        public Player(Circle circle, TranslateTransition transition) {
            this.circle = circle;
            this.transition = transition;
        }
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
