package com.github.bea4dev.vanilla_source.api.entity.ai.navigation.goal;

import com.github.bea4dev.vanilla_source.api.entity.ai.navigation.GoalSelector;
import com.github.bea4dev.vanilla_source.api.entity.ai.navigation.Navigator;

public interface PathfindingGoal {
    
    void run(GoalSelector goalSelector, Navigator navigator);
    
}
