package mined;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.jme3.material.Material;
import com.jme3.scene.Node;

public class ChunkManager {
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int SPAWN_CHUNK_RADIUS = 8;
    private static final long WORLD_SEED;
    private static final boolean DEBUG = true;
    
    private final Main app;
    private final Node worldNode;
    private final int renderDistance;
    private final Material dirtMaterial, grassMaterial, stoneMaterial;
    private final Map<String, Chunk> loadedChunks;
    private final Set<String> spawnChunks;
    private final NoiseGenerator terrainNoise;
    private final BiomeGenerator biomeGenerator;
    private final Set<String> generatingChunks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;
    
    static {
        WORLD_SEED = new Random().nextLong();
    }

    public ChunkManager(Main app, Node worldNode, int renderDistance,
                       Material dirtMaterial, Material grassMaterial, Material stoneMaterial) {
        this.app = app;
        this.worldNode = worldNode;
        this.renderDistance = renderDistance;
        this.dirtMaterial = dirtMaterial;
        this.grassMaterial = grassMaterial;
        this.stoneMaterial = stoneMaterial;
        this.loadedChunks = new ConcurrentHashMap<>();
        this.spawnChunks = new HashSet<>();
        this.terrainNoise = new NoiseGenerator(WORLD_SEED);
        this.biomeGenerator = new BiomeGenerator(WORLD_SEED);
    }

    public void generateSpawnChunks() {
        System.out.println("Generating spawn chunks...");
        int spawnX = 0;
        int spawnZ = 0;

        // Generate spawn chunks in a spiral pattern
        for (int layer = 0; layer <= SPAWN_CHUNK_RADIUS; layer++) {
            for (int x = -layer; x <= layer; x++) {
                for (int z = -layer; z <= layer; z++) {
                    if (Math.abs(x) == layer || Math.abs(z) == layer) {
                        String chunkKey = getChunkKey(x, z);
                        generateChunk(x, z);
                        spawnChunks.add(chunkKey);
                        System.out.printf("Generated spawn chunk at %d, %d%n", x, z);
                    }
                }
            }
        }
        System.out.println("Spawn chunks generation complete!");
    }

    public void generateChunk(int chunkX, int chunkZ) {
        String chunkKey = getChunkKey(chunkX, chunkZ);
        
        // Check if chunk is already loaded or being generated
        if (loadedChunks.containsKey(chunkKey) || generatingChunks.contains(chunkKey)) {
            return;
        }

        generatingChunks.add(chunkKey);
        
        try {
            Chunk chunk = new Chunk(
                chunkX * CHUNK_SIZE,
                chunkZ * CHUNK_SIZE,
                CHUNK_SIZE,
                CHUNK_HEIGHT,
                dirtMaterial,
                grassMaterial,
                stoneMaterial
            );
            
            generateTerrainForChunk(chunk, chunkX, chunkZ);
            worldNode.attachChild(chunk.getNode());
            loadedChunks.put(chunkKey, chunk);
        } finally {
            generatingChunks.remove(chunkKey);
        }
    }

    private void generateTerrainForChunk(Chunk chunk, int chunkX, int chunkZ) {
        if (DEBUG) System.out.println("Generating terrain for chunk: " + chunkX + ", " + chunkZ);
        
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = chunkX * CHUNK_SIZE + x;
                int worldZ = chunkZ * CHUNK_SIZE + z;
                
                // Simplified height generation for testing
                int baseHeight = 64; // Start with a fixed base height
                double noiseValue = terrainNoise.noise(worldX * 0.05, worldZ * 0.05);
                int finalHeight = baseHeight + (int)(noiseValue * 10);
                
                if (DEBUG && x == 0 && z == 0) {
                    System.out.printf("Generating column at (%d,%d) with height %d%n",
                        worldX, worldZ, finalHeight);
                }

                // Generate blocks
                for (int y = 0; y < finalHeight && y < CHUNK_HEIGHT; y++) {
                    BlockType blockType;
                    if (y == finalHeight - 1) {
                        blockType = BlockType.GRASS;
                    } else if (y > finalHeight - 4) {
                        blockType = BlockType.DIRT;
                    } else if (y == 0) {
                        blockType = BlockType.BEDROCK;
                    } else {
                        blockType = BlockType.STONE;
                    }
                    chunk.setBlock(x, y, z, blockType);
                }
            }
        }
        
        if (DEBUG) System.out.println("Updating mesh for chunk: " + chunkX + ", " + chunkZ);
        chunk.updateMesh();
    }

    private void generateColumn(Chunk chunk, int x, int z, int height, Biome biome) {
        // Bedrock layer
        for (int y = 0; y < 5; y++) {
            if (Math.random() > y * 0.3) {
                chunk.setBlock(x, y, z, BlockType.BEDROCK);
            }
        }

        // Stone base
        for (int y = 5; y < height - 4; y++) {
            chunk.setBlock(x, y, z, BlockType.STONE);
        }

        // Biome-specific layers
        for (int y = height - 4; y < height; y++) {
            chunk.setBlock(x, y, z, biome.getSubsurfaceBlock());
        }

        // Surface block
        chunk.setBlock(x, height, z, biome.getSurfaceBlock());

        // Add features based on biome (trees, etc)
        if (biome.shouldGenerateFeature(x, z)) {
            generateBiomeFeatures(chunk, x, height + 1, z, biome);
        }
    }

    private void generateBiomeFeatures(Chunk chunk, int x, int y, int z, Biome biome) {
        // Implement feature generation (trees, plants, etc.) based on biome
    }

    public void updateChunks(int playerChunkX, int playerChunkZ) {
        // Only update if player has moved to a different chunk
        if (playerChunkX == lastPlayerChunkX && playerChunkZ == lastPlayerChunkZ) {
            return;
        }

        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;

        int renderDistance = this.renderDistance;
        
        // Generate new chunks
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                int chunkX = playerChunkX + x;
                int chunkZ = playerChunkZ + z;
                generateChunk(chunkX, chunkZ);
            }
        }
        
        // Remove chunks outside render distance
        Set<String> chunksToRemove = new HashSet<>();
        for (String key : loadedChunks.keySet()) {
            String[] coords = key.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            
            if (Math.abs(chunkX - playerChunkX) > renderDistance + 1 ||
                Math.abs(chunkZ - playerChunkZ) > renderDistance + 1) {
                chunksToRemove.add(key);
            }
        }
        
        for (String key : chunksToRemove) {
            unloadChunk(key);
        }
    }

    private void unloadChunk(String chunkKey) {
        Chunk chunk = loadedChunks.remove(chunkKey);
        if (chunk != null) {
            chunk.getNode().removeFromParent();
        }
    }

    private String getChunkKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return loadedChunks.get(getChunkKey(chunkX, chunkZ));
    }
}
