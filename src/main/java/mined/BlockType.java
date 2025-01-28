package mined;

public enum BlockType {
    AIR(false), GRASS(false), DIRT(false), STONE(false), SAND(false), BEDROCK(false);

    private final boolean requiresSupport;

    BlockType(boolean requiresSupport) {
        this.requiresSupport = requiresSupport;
    }

    public boolean requiresSupport() {
        return requiresSupport;
    }
}
