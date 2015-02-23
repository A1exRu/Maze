package game.bubble.fx.controller;

import game.bubble.Context;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PlayerController {

    private static final Duration TRANSLATE_DURATION = Duration.seconds(5.25);
    private Map<Long, Player> players = new HashMap<>();
    
    @FXML
    private Pane battlefield;
    
    public PlayerController() {
        Context.udpClient.addMessageHandler(new AuthHandler());
        Context.udpClient.addMessageHandler(new MoveHandler());
    }
    
    public void onClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        System.out.println(x + "x" + y);
        ByteBuffer buff = ByteBuffer.allocate(28);
        buff.putInt(3);
        buff.putLong(1L);
        buff.putDouble(x);
        buff.putDouble(y);
        Context.udpClient.send(buff.array());
    }
    
    public void onMove() {
        
        
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

    public void move(long playerId, double dx, double dy) {
        Player player = players.get(playerId);
        if (player != null) {
            player.transition.setToX(dx - player.circle.getCenterX());
            player.transition.setToY(dy - player.circle.getCenterY());
            player.transition.playFromStart();
        }
    }
    
    private class AuthHandler implements game.test.client.MessageHandler {
        @Override
        public int getCode() {
            return 2;
        }

        @Override
        public void handle(ByteBuffer response) {
            long playerId = response.getLong();
            int x = response.getInt();
            int y = response.getInt();
            int red = response.getInt();
            int green = response.getInt();
            int blue = response.getInt();
            double opacity = response.getDouble();

            Color color = Color.rgb(red, green, blue, opacity);
            Circle circle = createCircle(x, y, color);
            final TranslateTransition transition = createTranslateTransition(circle);
            Player player = new Player(circle, transition);
            players.put(playerId, player);
            Platform.runLater(() -> battlefield.getChildren().add(circle));

//                moveCircleOnKeyPress(scene, circle, transition);
//                moveCircleOnMousePress(scene, circle, transition);
        }
    }
    
    private class MoveHandler implements game.test.client.MessageHandler {
        @Override
        public int getCode() {
            return 3;
        }

        @Override
        public void handle(ByteBuffer response) {
            long playerId = response.getLong();
            double dx = response.getDouble();
            double dy = response.getDouble();
            move(playerId, dx, dy);
        }
    }
    
}
