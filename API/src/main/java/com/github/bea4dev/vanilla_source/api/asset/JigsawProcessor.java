package com.github.bea4dev.vanilla_source.api.asset;

import com.github.bea4dev.vanilla_source.api.util.BlockStateUtil;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncWorldCache;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JigsawProcessor {
    private final List<BoundingBox> boundingBoxes = new ArrayList<>();
    private final Block startBlock;
    private final JigsawReference startJigsaw;
    private final int maxDepth;
    private final JNoise noise;
    private @Nullable BlockProcessor blockProcessor;

    public JigsawProcessor(Block startBlock, JigsawReference startJigsaw, int maxDepth, long seed) {
        this.startBlock = startBlock;
        this.startJigsaw = startJigsaw;
        this.maxDepth = maxDepth;

        var startAssetStartPosition = startBlock.getLocation().toVector().subtract(startJigsaw.relativePosition());
        var startAssetSize = startJigsaw.worldAsset().getEndPosition().subtract(startJigsaw.worldAsset().getStartPosition());
        var startAssetEndPosition = startAssetStartPosition.clone().add(startAssetSize);

        var startAssetBoundingBox = BoundingBox.of(startAssetStartPosition, startAssetEndPosition);
        boundingBoxes.add(startAssetBoundingBox);

        this.noise = JNoise.newBuilder()
                .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build())
                .scale(0.005)
                .build();
    }

    public void start() {
        var queue = new ArrayDeque<Joint>();
        queue.add(new Joint(startBlock, startJigsaw));
        var depth = 0;
        var world = startBlock.getWorld();
        var chunks = new HashSet<Chunk>();

        while (true) {
            var joint = queue.poll();
            if (joint == null || depth >= maxDepth) {
                break;
            }
            depth++;

            var startPosition = joint.block.getLocation().toVector().subtract(joint.jigsaw().relativePosition());
            final var blockProcessor = this.blockProcessor;
            joint.jigsaw().worldAsset().place(startPosition, (x, y, z, blockData, state) -> {
                var block = world.getBlockAt(x, y, z);

                if (blockData.getMaterial() == Material.JIGSAW) {
                    block.setType(Material.AIR);
                    return;
                }

                chunks.add(block.getChunk());

                block.setBlockData(blockData);
                BlockStateUtil.copyState(state, block.getState());

                if (blockProcessor != null) {
                    blockProcessor.processBlock(block);
                }
            });

            var nextJigsaws = JigsawReferenceManager.getFromAsset(joint.jigsaw().worldAsset().getAssetName());

            for (var jigsaw : nextJigsaws) {
                var jigsawBlock = world.getBlockAt(startPosition.clone().add(jigsaw.relativePosition()).toLocation(world));
                var jointBlock = jigsawBlock.getRelative(jigsaw.jigsawState().direction());

                var candidateJigsaws = new ArrayList<>(JigsawReferenceManager.getFromName(jigsaw.jigsawState().next()));
                biasedShuffle(candidateJigsaws, noise.evaluateNoise(jointBlock.getX(), jointBlock.getY(), jointBlock.getZ()));
                JigsawReference finalJigsaw = null;
                for (var candidateJigsaw : candidateJigsaws) {
                    var candidateAssetStartPosition = jointBlock.getLocation().toVector().subtract(candidateJigsaw.relativePosition());
                    var candidateAssetSize = candidateJigsaw.worldAsset().getEndPosition().subtract(candidateJigsaw.worldAsset().getStartPosition());
                    var candidateAssetEndPosition = candidateAssetStartPosition.clone().add(candidateAssetSize);

                    var candidateBoundingBox = BoundingBox.of(candidateAssetStartPosition, candidateAssetEndPosition.clone().add(new Vector(1.0, 1.0, 1.0)));

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

        for (var chunk : chunks) {
            AsyncWorldCache.update(chunk);
        }
    }

    public static void biasedShuffle(List<JigsawReference> list, double seed) {
        Objects.requireNonNull(list, "list");
        if (list.size() <= 1) return;

        // クランプ＆乱数シード化
        if (seed > 1.0) seed = 1.0;
        if (seed < -1.0) seed = -1.0;
        Random rng = new Random(mixSeed(seed, list.size()));

        // 優先度の範囲を取得
        double minP = Double.POSITIVE_INFINITY, maxP = Double.NEGATIVE_INFINITY;
        for (JigsawReference r : list) {
            double p = r.jigsawState().priority();
            if (p < minP) minP = p;
            if (p > maxP) maxP = p;
        }
        double span = maxP - minP;

        // 範囲がゼロなら普通にシャッフル
        if (span == 0.0) {
            Collections.shuffle(list, rng);
            return;
        }

        final double PRIORITY_WEIGHT = 0.25;
        final double RANDOM_WEIGHT = 1.0 - PRIORITY_WEIGHT;

        record Entry(JigsawReference ref, double key, long tiebreak) {
        }
        List<Entry> keyed = new ArrayList<>(list.size());

        for (JigsawReference ref : list) {
            double norm = (ref.jigsawState().priority() - minP) / span; // 0..1（高いほど前に来やすい）
            double rand = rng.nextDouble();               // 0..1

            // 高優先度寄りの混合キー（常に同じ向き）
            double key = RANDOM_WEIGHT * rand + PRIORITY_WEIGHT * norm;

            // 厳密なタイブレーク
            long tiebreak = rng.nextLong();
            keyed.add(new Entry(ref, key, tiebreak));
        }

        // key 降順
        keyed.sort((a, b) -> {
            int cmp = Double.compare(b.key, a.key);
            if (cmp != 0) return cmp;
            return Long.compare(b.tiebreak, a.tiebreak);
        });

        for (int i = 0; i < list.size(); i++) {
            list.set(i, keyed.get(i).ref());
        }
    }

    private static long mixSeed(double seed, int size) {
        long x = Double.doubleToLongBits(seed);
        x ^= 0x9E3779B97F4A7C15L;
        x ^= (long) size * 0x85EBCA6BL;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x;
    }

    record Joint(Block block, JigsawReference jigsaw) {
    }

    public JigsawProcessor blockProcessor(@Nullable BlockProcessor blockProcessor) {
        this.blockProcessor = blockProcessor;
        return this;
    }
}
