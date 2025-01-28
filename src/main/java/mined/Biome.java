package mined;

import static mined.BlockType.DIRT;
import static mined.BlockType.GRASS;
import static mined.BlockType.SAND;
import static mined.BlockType.STONE;

public enum Biome {
    PLAINS(64, 0.4f, GRASS, DIRT),
    FOREST(68, 0.6f, GRASS, DIRT),
    DESERT(60, 0.3f, SAND, SAND),
    MOUNTAINS(80, 1.0f, STONE, STONE);

    private final int baseHeight;
    private final float heightVariation;
    private final BlockType surfaceBlock;
    private final BlockType subsurfaceBlock;

    Biome(int baseHeight, float heightVariation, BlockType surfaceBlock, BlockType subsurfaceBlock) {
        this.baseHeight = baseHeight;
        this.heightVariation = heightVariation;
        this.surfaceBlock = surfaceBlock;
        this.subsurfaceBlock = subsurfaceBlock;
    }

    public int getBaseHeight() { return baseHeight; }
    public float getHeightVariation() { return heightVariation; }
    public BlockType getSurfaceBlock() { return surfaceBlock; }
    public BlockType getSubsurfaceBlock() { return subsurfaceBlock; }
    
    public boolean shouldGenerateFeature(int x, int z) {
        if (this == FOREST) {
            return Math.random() < 0.1; // 10% chance for trees in forest
        }
        return false;
    }
}