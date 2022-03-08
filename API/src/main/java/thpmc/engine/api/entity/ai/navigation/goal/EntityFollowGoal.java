package thpmc.engine.api.entity.ai.navigation.goal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.entity.ai.navigation.GoalSelector;
import thpmc.engine.api.entity.ai.navigation.Navigator;
import thpmc.engine.api.entity.ai.pathfinding.BlockPosition;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.Random;

public class EntityFollowGoal implements PathfindingGoal{
    
    private static final int TRACK_INTERVAL = 10;
    
    private final Entity target;
    
    public EntityFollowGoal(@NotNull Entity target) {
        this.target = target;
    }
    
    public Entity getTarget() {return target;}
    
    private int tick = new Random().nextInt(TRACK_INTERVAL);
    
    @Override
    public void run(GoalSelector goalSelector, Navigator navigator) {
        tick++;
        
        if(tick % TRACK_INTERVAL != 0){
            goalSelector.setFinished(true);
            return;
        }
        
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
    
        EngineWorld world = navigator.getEntity().getWorld();
        if(!world.getName().equals(target.getWorld().getName())) return;
        
        Location location = target.getLocation();
        for (int dy = 0; dy < 5; dy++) {
            Location l = location.clone().add(new Vector(0, -dy, 0));
            Object nmsBlockData = world.getNMSBlockData(l.getBlockX(), l.getBlockY(), l.getBlockZ());
            if(nmsBlockData == null) continue;
            
            if (nmsHandler.hasCollision(nmsBlockData)) {
                //Goal set
                navigator.setNavigationGoal(new BlockPosition(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ()));
                break;
            }
        }
        
        goalSelector.setFinished(true);
    }
    
}