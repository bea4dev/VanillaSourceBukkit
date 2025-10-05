package com.github.bea4dev.vanilla_source.api.asset;

import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class JigsawProcessor {
    private final List<BoundingBox> boundingBoxes = new ArrayList<>();
    private final Block startBlock;
    private final JigsawReference startJigsaw;
    private final int maxDepth;

    public JigsawProcessor(Block startBlock, JigsawReference startJigsaw, int maxDepth) {
        this.startBlock = startBlock;
        this.startJigsaw = startJigsaw;
        this.maxDepth = maxDepth;
    }

    public void start() {
        var queue = new ArrayDeque<Joint>();
        queue.add(new Joint(startBlock, startJigsaw));
        var depth = 0;
        var world = startBlock.getWorld();

        while (true) {
            var joint = queue.poll();
            if (joint == null || depth >= maxDepth) {
                break;
            }
            depth++;

            var startPosition = joint.block.getLocation().toVector().subtract(joint.jigsaw().relativePosition());
            joint.jigsaw().worldAsset().place(startPosition, (x, y, z, block, state) -> {
                world.setBlockData(x, y, z, block);
            });

            var nextJigsaws = JigsawReferenceManager.getFromAsset(joint.jigsaw().worldAsset().getAssetName());
        }
    }

    record Joint(Block block, JigsawReference jigsaw) {
    }
}
