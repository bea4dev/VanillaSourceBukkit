package com.github.bea4dev.vanilla_source.api.util;

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class BlockStateUtil {
    public static void copyState(BlockState from, BlockState to) {
        if (from instanceof Sign fromSign) {
            if (to instanceof Sign toSign) {
                var i = 0;
                for (var line : fromSign.getSide(Side.BACK).lines()) {
                    toSign.getSide(Side.BACK).line(i, line);
                    i++;
                }
                i = 0;
                for (var line : fromSign.getSide(Side.FRONT).lines()) {
                    toSign.getSide(Side.FRONT).line(i, line);
                    i++;
                }
                to.update(true, true);
            }
        }
    }
}
