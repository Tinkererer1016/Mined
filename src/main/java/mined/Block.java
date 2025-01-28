package mined;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

public class Block {
    private static Map<String, Block> blockRegistry;
    private static long lastBreakTime = 0;
    private static final long BREAK_COOLDOWN = 100;
    
    private Material material;
    private Vector3f position;
    private BlockType type;
    private Geometry geometry;
    private Block[] neighbors;
    private static final int NEIGHBOR_COUNT = 6; // N, S, E, W, UP, DOWN

    public Block(Material material, Vector3f position, BlockType type) {
        this.material = material;
        this.position = position;
        this.type = type;
        this.neighbors = new Block[NEIGHBOR_COUNT];
        setupMaterial();
        if (blockRegistry != null) {
            blockRegistry.put(getKey(position), this);
        }
    }

    private void setupMaterial() {
        if (material != null) {
            material = material.clone();
            material.setBoolean("UseMaterialColors", true);
            material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
        }
    }

    public static Block placeBlock(Vector3f position, BlockType type, Node parentNode, Material material) {
        Block block = new Block(material, position, type);
        block.attachTo(parentNode);
        
        for (Direction dir : Direction.values()) {
            Vector3f neighborPos = position.add(dir.getNormal());
            String key = block.getKey(neighborPos);
            Block neighbor = blockRegistry.get(key);
            if (neighbor != null) {
                block.setNeighbor(dir, neighbor);
            }
        }
        return block;
    }

    private float[] getFaceVertices(float x, float y, float z, float hs, Direction direction) {
        switch (direction) {
            case NORTH:
                return new float[] {
                    x - hs, y - hs, z + hs,
                    x + hs, y - hs, z + hs,
                    x + hs, y + hs, z + hs,
                    x - hs, y + hs, z + hs
                };
            case SOUTH:
                return new float[] {
                    x + hs, y - hs, z - hs,
                    x - hs, y - hs, z - hs,
                    x - hs, y + hs, z - hs,
                    x + hs, y + hs, z - hs
                };
            case EAST:
                return new float[] {
                    x + hs, y - hs, z + hs,
                    x + hs, y - hs, z - hs,
                    x + hs, y + hs, z - hs,
                    x + hs, y + hs, z + hs
                };
            case WEST:
                return new float[] {
                    x - hs, y - hs, z - hs,
                    x - hs, y - hs, z + hs,
                    x - hs, y + hs, z + hs,
                    x - hs, y + hs, z - hs
                };
            case UP:
                return new float[] {
                    x - hs, y + hs, z + hs,
                    x + hs, y + hs, z + hs,
                    x + hs, y + hs, z - hs,
                    x - hs, y + hs, z - hs
                };
            case DOWN:
                return new float[] {
                    x - hs, y - hs, z - hs,
                    x + hs, y - hs, z - hs,
                    x + hs, y - hs, z + hs,
                    x - hs, y - hs, z + hs
                };
            default:
                return new float[12];
        }
    }
        private float[] getTextureCoordinates(Direction direction) {
        // For a 128x96 texture atlas
        float tileSize = 32f;
        float atlasWidth = 128f;
        float atlasHeight = 96f;
        
        float tileU = tileSize / atlasWidth;   // 0.25
        float tileV = tileSize / atlasHeight;  // 0.333...

        switch (direction) {
            case UP:    // Grass top
                return new float[] {
                    0f, 0f,           // Top-left
                    tileU, 0f,        // Top-right
                    tileU, tileV,     // Bottom-right
                    0f, tileV         // Bottom-left
                };
            case DOWN:  // Dirt bottom
                return new float[] {
                    tileU, 0f,        // Top-left
                    tileU * 2, 0f,    // Top-right
                    tileU * 2, tileV, // Bottom-right
                    tileU, tileV      // Bottom-left
                };
            default:    // Stone sides
                return new float[] {
                    tileU * 2, 0f,    // Top-left
                    tileU * 3, 0f,    // Top-right
                    tileU * 3, tileV, // Bottom-right
                    tileU * 2, tileV  // Bottom-left
                };
        }
    }

    public void createOptimizedMesh(boolean showNorth, boolean showSouth, boolean showEast, 
                                  boolean showWest, boolean showTop, boolean showBottom) {
        // Count visible faces
        int visibleFaces = 0;
        if (showNorth) visibleFaces++;
        if (showSouth) visibleFaces++;
        if (showEast) visibleFaces++;
        if (showWest) visibleFaces++;
        if (showTop) visibleFaces++;
        if (showBottom) visibleFaces++;

        if (visibleFaces == 0) {
            if (geometry != null) {
                geometry.removeFromParent();
                geometry = null;
            }
            return;
        }

        // Create arrays for mesh data
        float[] vertices = new float[visibleFaces * 12];  // 4 vertices per face, 3 coords each
        float[] texCoords = new float[visibleFaces * 8];  // 4 vertices per face, 2 coords each
        int[] indices = new int[visibleFaces * 6];        // 6 indices per face

        int faceIndex = 0;

        // Add faces based on visibility
        if (showNorth) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.NORTH);
        if (showSouth) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.SOUTH);
        if (showEast) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.EAST);
        if (showWest) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.WEST);
        if (showTop) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.UP);
        if (showBottom) addFace(vertices, indices, texCoords, position.x, position.y, position.z, faceIndex++, Direction.DOWN);

        // Create and setup mesh
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();

        // Create or update geometry
        if (geometry == null) {
            geometry = new Geometry("Block", mesh);
            geometry.setMaterial(material);
        } else {
            geometry.setMesh(mesh);
        }

        // Debug output
        System.out.println("Created block mesh with " + visibleFaces + " faces");
        logMeshInfo(mesh);
    }

    private void addFace(float[] vertices, int[] indices, float[] texCoords, 
                        float x, float y, float z, int faceIndex, Direction dir) {
        // Get vertices for this face
        float[] faceVerts = getFaceVertices(x, y, z, 0.5f, dir);
        System.arraycopy(faceVerts, 0, vertices, faceIndex * 12, 12);

        // Get texture coordinates for this face
        float[] faceTexCoords = getTextureCoordinates(dir);
        System.out.println("Adding face for direction " + dir + " with tex coords:");
        for (int i = 0; i < faceTexCoords.length; i += 2) {
            System.out.printf("(%.3f, %.3f)%n", faceTexCoords[i], faceTexCoords[i+1]);
        }
        System.arraycopy(faceTexCoords, 0, texCoords, faceIndex * 8, 8);

        // Set up indices for this face
        int baseVertex = faceIndex * 4;
        indices[faceIndex * 6] = baseVertex;
        indices[faceIndex * 6 + 1] = baseVertex + 1;
        indices[faceIndex * 6 + 2] = baseVertex + 2;
        indices[faceIndex * 6 + 3] = baseVertex;
        indices[faceIndex * 6 + 4] = baseVertex + 2;
        indices[faceIndex * 6 + 5] = baseVertex + 3;
    }
        private void logMeshInfo(Mesh mesh) {
        VertexBuffer texCoordBuffer = mesh.getBuffer(VertexBuffer.Type.TexCoord);
        if (texCoordBuffer != null) {
            FloatBuffer texCoords = (FloatBuffer) texCoordBuffer.getData();
            System.out.println("Mesh texture coordinates:");
            texCoords.rewind();
            while (texCoords.hasRemaining()) {
                float u = texCoords.get();
                float v = texCoords.get();
                System.out.printf("(%.3f, %.3f)%n", u, v);
            }
        } else {
            System.out.println("No texture coordinates found in mesh!");
        }
    }

    private void updateVisibility(Block block, Node parent) {
        if (block == null || block.geometry == null) return;
        
        boolean showNorth = block.neighbors[0] == null || block.neighbors[0].geometry == null;
        boolean showSouth = block.neighbors[1] == null || block.neighbors[1].geometry == null;
        boolean showEast = block.neighbors[2] == null || block.neighbors[2].geometry == null;
        boolean showWest = block.neighbors[3] == null || block.neighbors[3].geometry == null;
        boolean showTop = block.neighbors[4] == null || block.neighbors[4].geometry == null;
        boolean showBottom = block.neighbors[5] == null || block.neighbors[5].geometry == null;

        block.createOptimizedMesh(showNorth, showSouth, showEast, showWest, showTop, showBottom);
        
        if (parent != null && block.geometry != null && block.geometry.getParent() == null) {
            block.attachTo(parent);
        }
    }

    private int getOppositeIndex(int index) {
        switch (index) {
            case 0: return 1; // North <-> South
            case 1: return 0;
            case 2: return 3; // East <-> West
            case 3: return 2;
            case 4: return 5; // Up <-> Down
            case 5: return 4;
            default: return -1;
        }
    }

    private int getDirectionIndex(Direction direction) {
        switch (direction) {
            case NORTH: return 0;
            case SOUTH: return 1;
            case EAST: return 2;
            case WEST: return 3;
            case UP: return 4;
            case DOWN: return 5;
            default: return -1;
        }
    }

    private String getKey(Vector3f pos) {
        return Math.round(pos.x) + "," + Math.round(pos.y) + "," + Math.round(pos.z);
    }

    // Getters and utility methods
    public Vector3f getPosition() { return position; }
    public Geometry getGeometry() { return geometry; }
    public BlockType getType() { return type; }

    public void attachTo(Node parent) {
        if (geometry != null && geometry.getParent() == null) {
            parent.attachChild(geometry);
        }
    }

    public static void setBlockRegistry(Map<String, Block> registry) {
        blockRegistry = registry;
    }

    public boolean intersects(Vector3f point, float radius) {
        float halfSize = 0.5f;
        float minX = position.x - halfSize;
        float maxX = position.x + halfSize;
        float minY = position.y - halfSize;
        float maxY = position.y + halfSize;
        float minZ = position.z - halfSize;
        float maxZ = position.z + halfSize;

        float closestX = Math.max(minX, Math.min(point.x, maxX));
        float closestY = Math.max(minY, Math.min(point.y, maxY));
        float closestZ = Math.max(minZ, Math.min(point.z, maxZ));

        float dx = point.x - closestX;
        float dy = point.y - closestY;
        float dz = point.z - closestZ;

        return (dx * dx + dy * dy + dz * dz) <= radius * radius;
    }

    public static Map<String, Block> getBlockRegistry() {
        return blockRegistry;
    }

    public void scheduleBreak() {
        cleanup();
    }

    public void setNeighbor(Direction direction, Block neighbor) {
        int index = getDirectionIndex(direction);
        if (index != -1) {
            if (neighbors[index] != null) {
                neighbors[index].neighbors[getOppositeIndex(index)] = null;
            }
            neighbors[index] = neighbor;
            if (neighbor != null) {
                neighbor.neighbors[getOppositeIndex(index)] = this;
            }
            if (geometry != null && geometry.getParent() != null) {
                updateVisibility(this, geometry.getParent());
                if (neighbor != null) {
                    updateVisibility(neighbor, geometry.getParent());
                }
            }
        }
    }

    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBreakTime < BREAK_COOLDOWN) {
            return;
        }
        lastBreakTime = currentTime;

        final Node parent = geometry != null ? geometry.getParent() : null;
        final Vector3f pos = position.clone();
        final List<Block> neighborsToUpdate = new ArrayList<>();

        for (int i = 0; i < NEIGHBOR_COUNT; i++) {
            if (neighbors[i] != null) {
                neighborsToUpdate.add(neighbors[i]);
                Block neighbor = neighbors[i];
                neighbor.neighbors[getOppositeIndex(i)] = null;
            }
        }

        if (geometry != null) {
            geometry.removeFromParent();
            geometry = null;
        }

        if (blockRegistry != null) {
            blockRegistry.remove(getKey(position));
        }

        neighbors = new Block[NEIGHBOR_COUNT];

        if (parent != null && !neighborsToUpdate.isEmpty()) {
            Main.getInstance().enqueue(() -> {
                for (Block neighbor : neighborsToUpdate) {
                    if (neighbor != null) {
                        neighbor.createOptimizedMesh(
                            neighbor.checkNeighborVisibility(Direction.NORTH),
                            neighbor.checkNeighborVisibility(Direction.SOUTH),
                            neighbor.checkNeighborVisibility(Direction.EAST),
                            neighbor.checkNeighborVisibility(Direction.WEST),
                            neighbor.checkNeighborVisibility(Direction.UP),
                            neighbor.checkNeighborVisibility(Direction.DOWN)
                        );
                    }
                }
                return null;
            });
        }
    }

    private boolean checkNeighborVisibility(Direction direction) {
        int index = getDirectionIndex(direction);
        return neighbors[index] == null || neighbors[index].geometry == null;
    }
    //test
}