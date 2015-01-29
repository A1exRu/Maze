package game.bubble;

import game.world.Point;

import java.util.HashMap;
import java.util.Map;

public class BubbleWorld {
    
    public final float width = 800;
    public final float height = 600;
    
    private Map<Long, BubbleHero> heroes = new HashMap<>();
    private long heroIndex;
    
    public void tick() {
        
    }
    
    public BubbleHero newHero() {
        heroIndex++;
        float startPoint = (heroIndex * 50) % 500;
        BubbleHero bubbleHero = new BubbleHero(heroIndex, (int)heroIndex, new Point(startPoint, startPoint));
        heroes.put(heroIndex, bubbleHero);
        return bubbleHero;
    }
    

}
