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

        var startAssetStartPosition = startBlock.getLocation().toVector().subtract(startJigsaw.relativePosition());
        var startAssetSize = startJigsaw.worldAsset().getEndPosition().subtract(startJigsaw.worldAsset().getStartPosition());
        var startAssetEndPosition = startAssetStartPosition.clone().add(startAssetSize);

        var startAssetBoundingBox = BoundingBox.of(startAssetStartPosition, startAssetEndPosition);
        boundingBoxes.add(startAssetBoundingBox);
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

            for (var jigsaw : nextJigsaws) {
                var jigsawBlock = world.getBlockAt(startPosition.clone().add(jigsaw.relativePosition()).toLocation(world));
                var jointBlock = jigsawBlock.getRelative(jigsaw.jigsawState().direction());

                var candidateJigsaws = JigsawReferenceManager.getFromName(jigsaw.jigsawState().name());
                JigsawReference finalJigsaw = null;
                for (var candidateJigsaw : candidateJigsaws) {
                    var candidateAssetStartPosition = jointBlock.getLocation().toVector().subtract(candidateJigsaw.relativePosition());
                    var candidateAssetSize = candidateJigsaw.worldAsset().getEndPosition().subtract(candidateJigsaw.worldAsset().getStartPosition());
                    var candidateAssetEndPosition = candidateAssetStartPosition.clone().add(candidateAssetSize);

                    var candidateBoundingBox = BoundingBox.of(candidateAssetStartPosition, candidateAssetEndPosition);

                    var isCollide = false;
                    for (var existsBoundingBox : boundingBoxes) {
                        if (existsBoundingBox.overlaps(candidateBoundingBox)) {
                            isCollide = true;
                            break;
                        }
                    }

                    if (isCollide) {
                        continue;
                    }

                    boundingBoxes.add(candidateBoundingBox);
                    finalJigsaw = candidateJigsaw;
                    break;
                }

                if (finalJigsaw == null) {
                    continue;
                }

                queue.add(new Joint(jointBlock, finalJigsaw));
            }
        }
    }

    record Joint(Block block, JigsawReference jigsaw) {
    }
}
