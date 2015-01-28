package game.test.client;

import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;

public class Player {

    Circle circle;
    TranslateTransition transition;

    public Player(Circle circle, TranslateTransition transition) {
        this.circle = circle;
        this.transition = transition;
    }
}
