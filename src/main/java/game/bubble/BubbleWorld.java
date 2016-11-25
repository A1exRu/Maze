package game.bubble;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import game.server.Game;
import game.server.ServerContext;
import game.world.Point;

public class BubbleWorld extends Game {
    
    public final float width = 800;
    public final float height = 600;
    
    private Map<Long, BubbleHero> heroes = new HashMap<>();
    private long heroIndex;

    
    int temp;
    
    @Autowired
    private ServerContext context;
    
    @PostConstruct
    public void start() {
        context.add(this);
    }
    
    public BubbleWorld(long gameId) {
        super(gameId);
    }

    @Override
    public void tick() {
        System.out.println("Bubble juggle " + temp);
        temp++;
        if (temp > 1) {
            stop();
        }
    }
    
    public BubbleHero newHero() {
        heroIndex++;
        float startPoint = (heroIndex * 50) % 500;
        BubbleHero bubbleHero = new BubbleHero(heroIndex, (int)heroIndex, new Point(startPoint, startPoint));
        heroes.put(heroIndex, bubbleHero);
        return bubbleHero;
    }
    

}
