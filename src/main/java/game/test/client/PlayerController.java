package game.test.client;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerController {

    private static final Duration TRANSLATE_DURATION = Duration.seconds(5.25);
    private Map<Long, Player> players = new ConcurrentHashMap<>();

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

//    private void moveCircleOnMousePress(Scene scene, final Circle circle) {
//        scene.setOnMousePressed(event -> onMessage(1, event.getSceneX(), event.getScreenY(), circle.getCenterX(), circle.getCenterY()));
//    }

    public void addPlayer(final long playerId) {

        Platform.runLater(() -> {
            System.out.println("add player " + playerId);
            long delta = 50 + 50 * playerId;
            Color color = Color.YELLOW;
            switch ((int) playerId) {
                case 0:
                    break;
                case 1:
                    color = Color.RED;
                    break;
                case 2:
                    color = Color.DARKGREEN;
                    break;
                case 3:
                    color = Color.PURPLE;
                    break;
            }

            Circle circle = createCircle(delta, delta, color);
            final TranslateTransition transition = createTranslateTransition(circle);
//            moveCircleOnKeyPress(scene, transition);
//            moveCircleOnMousePress(scene, circle);
//            Player player = new Player(circle, transition);
//            players.put(playerId, player);
//            main.getChildren().add(circle);
        });
    }

    public void move(long playerId, double x, double y, double cx, double cy) {
        Player player = players.get(playerId);
        if (player != null) {
            player.transition.setToX(x - cx);
            player.transition.setToY(y - cy);
            player.transition.playFromStart();
        }
    }
    
}
