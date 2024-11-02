package com.github.bea4dev.vanilla_source.api.util;

@FunctionalInterface
public interface ThreadsafeIteration<V> {
    
    void accept(int x, int y, int z, V value);
    
}
