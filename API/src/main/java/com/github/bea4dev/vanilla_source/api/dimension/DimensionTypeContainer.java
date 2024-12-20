package com.github.bea4dev.vanilla_source.api.dimension;

import java.util.OptionalLong;

public record DimensionTypeContainer(
        OptionalLong fixedTime,
        boolean hasSkyLight,
        boolean hasCeiling,
        boolean ultraWarm,
        boolean natural,
        double coordinateScale,
        boolean bedWorks,
        boolean respawnAnchorWorks,
        int minY,
        int height,
        int logicalHeight,
        InfiniburnType infiniburn,
        EffectsType effects,
        float ambientLight,
        MonsterSettings monsterSettings
) {

    private static final int DEFAULT_MIN_Y = -64;
    private static final int DEFAULT_MAX_Y = 319;

    public enum InfiniburnType {
        OVERWORLD,
        THE_NETHER,
        THE_END,
    }

    public enum EffectsType {
        OVERWORLD,
        THE_NETHER,
        THE_END,
    }

    public record MonsterSettings(
            boolean piglinSafe,
            boolean hasRaids,
            int monsterSpawnBlockLightLimit
    ) {}

    public static class DimensionTypeContainerBuilder {
        private Long fixedTime = null;
        private boolean hasSkyLight;
        private boolean hasCeiling;
        private boolean ultraWarm;
        private boolean natural;
        private double coordinateScale = 1.0;
        private boolean bedWorks;
        private boolean respawnAnchorWorks;
        private int minY = DEFAULT_MIN_Y;
        private int height = DEFAULT_MAX_Y - DEFAULT_MIN_Y + 1;
        private int logicalHeight = DEFAULT_MAX_Y - DEFAULT_MIN_Y + 1;
        private DimensionTypeContainer.InfiniburnType infiniburn = DimensionTypeContainer.InfiniburnType.OVERWORLD;
        private EffectsType effects = EffectsType.OVERWORLD;
        private float ambientLight;
        private DimensionTypeContainer.MonsterSettings monsterSettings = new DimensionTypeContainer.MonsterSettings(
                false,
                false,
                0
        );

        public DimensionTypeContainerBuilder fixedTime(long fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        public DimensionTypeContainerBuilder hasSkyLight(boolean hasSkyLight) {
            this.hasSkyLight = hasSkyLight;
            return this;
        }

        public DimensionTypeContainerBuilder hasCeiling(boolean hasCeiling) {
            this.hasCeiling = hasCeiling;
            return this;
        }

        public DimensionTypeContainerBuilder ultraWarm(boolean ultraWarm) {
            this.ultraWarm = ultraWarm;
            return this;
        }

        public DimensionTypeContainerBuilder natural(boolean natural) {
            this.natural = natural;
            return this;
        }

        public DimensionTypeContainerBuilder coordinateScale(double coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        public DimensionTypeContainerBuilder bedWorks(boolean bedWorks) {
            this.bedWorks = bedWorks;
            return this;
        }

        public DimensionTypeContainerBuilder respawnAnchorWorks(boolean respawnAnchorWorks) {
            this.respawnAnchorWorks = respawnAnchorWorks;
            return this;
        }

        public DimensionTypeContainerBuilder minY(int minY) {
            this.minY = minY;
            return this;
        }

        public DimensionTypeContainerBuilder height(int height) {
            this.height = height;
            return this;
        }

        public DimensionTypeContainerBuilder logicalHeight(int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        public DimensionTypeContainerBuilder infiniburn(DimensionTypeContainer.InfiniburnType infiniburn) {
            this.infiniburn = infiniburn;
            return this;
        }

        public DimensionTypeContainerBuilder effects(EffectsType effects) {
            this.effects = effects;
            return this;
        }

        public DimensionTypeContainerBuilder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        public DimensionTypeContainerBuilder monsterSettings(DimensionTypeContainer.MonsterSettings monsterSettings) {
            this.monsterSettings = monsterSettings;
            return this;
        }

        public DimensionTypeContainer build() {
            return new DimensionTypeContainer(
                    fixedTime == null ? OptionalLong.empty() : OptionalLong.of(fixedTime),
                    hasSkyLight,
                    hasCeiling,
                    ultraWarm,
                    natural,
                    coordinateScale,
                    bedWorks,
                    respawnAnchorWorks,
                    minY,
                    height,
                    logicalHeight,
                    infiniburn,
                    effects,
                    ambientLight,
                    monsterSettings
            );
        }
    }

}
