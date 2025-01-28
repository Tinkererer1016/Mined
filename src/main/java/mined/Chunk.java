package mined;

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Chunk {
    private final Node chunkNode;
    private final int x, z;
    private final int size;
    private final int height;
    private final Material dirtMaterial;
    private final Material grassMaterial;
    private final Material stoneMaterial;
    private final BlockType[][][] blocks;

    public Chunk(int x, int z, int size, int height,
                Material dirtMaterial, Material grassMaterial, Material stoneMaterial) {
        this.x = x;
        this.z = z;
        this.size = size;
        this.height = height;
        this.dirtMaterial = dirtMaterial;
        this.grassMaterial = grassMaterial;
        this.stoneMaterial = stoneMaterial;
        this.blocks = new BlockType[size][height][size];
        this.chunkNode = new Node("Chunk_" + x + "_" + z);
        
        // Initialize all blocks to AIR
        for (int bx = 0; bx < size; bx++) {
            for (int by = 0; by < height; by++) {
                for (int bz = 0; bz < size; bz++) {
                    blocks[bx][by][bz] = BlockType.AIR;
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (isInBounds(x, y, z)) {
            blocks[x][y][z] = type;
        }
    }

    public BlockType getBlock(int x, int y, int z) {
        if (isInBounds(x, y, z)) {
            return blocks[x][y][z];
        }
        return BlockType.AIR;
    }

    private boolean isInBounds(int x, int y, int z) {
        return x >= 0 && x < size && y >= 0 && y < height && z >= 0 && z < size;
    }

    public void updateMesh() {
        chunkNode.detachAllChildren();
        
        // Separate meshes for different materials
        List<Vector3f> dirtVertices = new ArrayList<>();
        List<Vector3f> grassVertices = new ArrayList<>();
        List<Vector3f> stoneVertices = new ArrayList<>();
        
        // Generate mesh data
        for (int bx = 0; bx < size; bx++) {
            for (int by = 0; by < height; by++) {
                for (int bz = 0; bz < size; bz++) {
                    if (blocks[bx][by][bz] != BlockType.AIR) {
                        addBlockToMesh(bx, by, bz, blocks[bx][by][bz],
                                     dirtVertices, grassVertices, stoneVertices);
                    }
                }
            }
        }
        
        // Create and attach meshes
        if (!dirtVertices.isEmpty()) {
            attachMesh(dirtVertices, dirtMaterial, "DirtMesh");
        }
        if (!grassVertices.isEmpty()) {
            attachMesh(grassVertices, grassMaterial, "GrassMesh");
        }
        if (!stoneVertices.isEmpty()) {
            attachMesh(stoneVertices, stoneMaterial, "StoneMesh");
        }
    }

    private void addBlockToMesh(int x, int y, int z, BlockType type,
                              List<Vector3f> dirtVerts,
                              List<Vector3f> grassVerts,
                              List<Vector3f> stoneVerts) {
        float wx = this.x + x;
        float wy = y;
        float wz = this.z + z;

        List<Vector3f> targetList;
        switch (type) {
            case DIRT:
                targetList = dirtVerts;
                break;
            case GRASS:
                targetList = grassVerts;
                break;
            case STONE:
                targetList = stoneVerts;
                break;
            default:
                return;
        }

        // Only add faces that are exposed to air
        // Front face
        if (z == size - 1 || getBlock(x, y, z + 1) == BlockType.AIR) {
            addFace(targetList, wx, wy, wz, 1, 0, 0, 0, 1, 0);
        }
        // Back face
        if (z == 0 || getBlock(x, y, z - 1) == BlockType.AIR) {
            addFace(targetList, wx + 1, wy, wz, -1, 0, 0, 0, 1, 0);
        }
        // Right face
        if (x == size - 1 || getBlock(x + 1, y, z) == BlockType.AIR) {
            addFace(targetList, wx + 1, wy, wz, 0, 0, 1, 0, 1, 0);
        }
        // Left face
        if (x == 0 || getBlock(x - 1, y, z) == BlockType.AIR) {
            addFace(targetList, wx, wy, wz, 0, 0, -1, 0, 1, 0);
        }
        // Top face
        if (y == height - 1 || getBlock(x, y + 1, z) == BlockType.AIR) {
            addFace(targetList, wx, wy + 1, wz, 1, 0, 0, 0, 0, 1);
        }
        // Bottom face
        if (y == 0 || getBlock(x, y - 1, z) == BlockType.AIR) {
            addFace(targetList, wx, wy, wz, 1, 0, 0, 0, 0, 1);
        }
    }

    private void addFace(List<Vector3f> verts, float x, float y, float z,
                        float dx, float dy, float dz,
                        float ux, float uy, float uz) {
        verts.add(new Vector3f(x, y, z));
        verts.add(new Vector3f(x + dx, y + dy, z + dz));
        verts.add(new Vector3f(x + dx + ux, y + dy + uy, z + dz + uz));
        verts.add(new Vector3f(x + ux, y + uy, z + uz));
    }

private void attachMesh(List<Vector3f> vertices, Material material, String name) {
    if (vertices.isEmpty()) return;

    Mesh mesh = new Mesh();
    Vector3f[] vertexArray = vertices.toArray(new Vector3f[0]);
    
    // Generate normals (one normal per vertex)
    Vector3f[] normals = new Vector3f[vertexArray.length];
    for (int i = 0; i < vertices.size(); i += 4) {
        Vector3f v1 = vertexArray[i];
        Vector3f v2 = vertexArray[i + 1];
        Vector3f v3 = vertexArray[i + 2];
        
        Vector3f edge1 = v2.subtract(v1);
        Vector3f edge2 = v3.subtract(v1);
        Vector3f normal = edge1.cross(edge2).normalizeLocal();
        
        normals[i] = normal;
        normals[i + 1] = normal;
        normals[i + 2] = normal;
        normals[i + 3] = normal;
    }
    
    // Generate indices for quads
    int[] indices = new int[vertices.size() * 6 / 4];
    for (int i = 0, j = 0; i < vertices.size(); i += 4, j += 6) {
        indices[j] = i;
        indices[j + 1] = i + 1;
        indices[j + 2] = i + 2;
        indices[j + 3] = i;
        indices[j + 4] = i + 2;
        indices[j + 5] = i + 3;
    }

    mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertexArray));
    mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
    mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indices));
    mesh.updateBound();

    Geometry geo = new Geometry(name, mesh);
    geo.setMaterial(material);
    chunkNode.attachChild(geo);
}

    public Node getNode() {
        return chunkNode;
    }

    public int getHighestBlock(int x, int z) {
        if (x < 0 || x >= size || z < 0 || z >= size) {
            return -1;
        }
        
        for (int y = height - 1; y >= 0; y--) {
            if (blocks[x][y][z] != BlockType.AIR) {
                return y;
            }
        }
        return -1;
    }
}