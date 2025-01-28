package mined;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class World {
    private final Map<String, Block> blocks;
    private final Node worldNode;
    private static final float MAX_DISTANCE = 20f;

    public World(Node worldNode) {
        this.worldNode = worldNode;
        this.blocks = new ConcurrentHashMap<>();
    }

    public void addBlock(Block block, int x, int y, int z) {
        blocks.put(getBlockKey(x, y, z), block);
        updateNeighbors(x, y, z);
    }

    public Block getBlock(int x, int y, int z) {
        return blocks.get(getBlockKey(x, y, z));
    }

    public void removeBlock(int x, int y, int z) {
        Block block = blocks.remove(getBlockKey(x, y, z));
        if (block != null) {
            block.cleanup();
            updateNeighborsOnRemove(x, y, z);
        }
    }

    public Block raycast(Vector3f origin, Vector3f direction) {
        Ray ray = new Ray(origin, direction);
        float closestDistance = Float.MAX_VALUE;
        Block closestBlock = null;

        for (Block block : blocks.values()) {
            Geometry geometry = block.getGeometry();
            if (geometry != null) {
                float distance = origin.distance(block.getPosition());
                if (distance <= MAX_DISTANCE) {
                    CollisionResults results = new CollisionResults();
                    geometry.collideWith(ray, results);
                    if (results.size() > 0) {
                        CollisionResult closest = results.getClosestCollision();
                        if (closest.getDistance() < closestDistance) {
                            closestDistance = closest.getDistance();
                            closestBlock = block;
                        }
                    }
                }
            }
        }
        return closestBlock;
    }

    private String getBlockKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private void updateNeighbors(int x, int y, int z) {
        Block current = getBlock(x, y, z);
        if (current == null) return;

        updateNeighborInDirection(current, x, y, z + 1, Direction.NORTH);
        updateNeighborInDirection(current, x, y, z - 1, Direction.SOUTH);
        updateNeighborInDirection(current, x + 1, y, z, Direction.EAST);
        updateNeighborInDirection(current, x - 1, y, z, Direction.WEST);
        updateNeighborInDirection(current, x, y + 1, z, Direction.UP);
        updateNeighborInDirection(current, x, y - 1, z, Direction.DOWN);
    }

    private void updateNeighborInDirection(Block current, int nx, int ny, int nz, Direction direction) {
        Block neighbor = getBlock(nx, ny, nz);
        if (neighbor != null) {
            current.setNeighbor(direction, neighbor);
        }
    }

    private void updateNeighborsOnRemove(int x, int y, int z) {
        checkAndUpdateBlock(x, y, z + 1);
        checkAndUpdateBlock(x, y, z - 1);
        checkAndUpdateBlock(x + 1, y, z);
        checkAndUpdateBlock(x - 1, y, z);
        checkAndUpdateBlock(x, y + 1, z);
        checkAndUpdateBlock(x, y - 1, z);
    }

    private void checkAndUpdateBlock(int x, int y, int z) {
        Block block = getBlock(x, y, z);
        if (block != null) {
            updateBlockVisibility(block);
        }
    }

    private void updateBlockVisibility(Block block) {
        if (block != null && block.getGeometry() != null) {
            Vector3f pos = block.getPosition();
            int x = Math.round(pos.x);
            int y = Math.round(pos.y);
            int z = Math.round(pos.z);

            boolean showNorth = !hasNeighbor(x, y, z + 1);
            boolean showSouth = !hasNeighbor(x, y, z - 1);
            boolean showEast = !hasNeighbor(x + 1, y, z);
            boolean showWest = !hasNeighbor(x - 1, y, z);
            boolean showTop = !hasNeighbor(x, y + 1, z);
            boolean showBottom = !hasNeighbor(x, y - 1, z);

            block.createOptimizedMesh(showNorth, showSouth, showEast, showWest, showTop, showBottom);
        }
    }

    private boolean hasNeighbor(int x, int y, int z) {
        String key = getBlockKey(x, y, z);
        Block neighbor = blocks.get(key);
        return neighbor != null && neighbor.getGeometry() != null;
    }

    public void clear() {
        blocks.values().stream()
            .map(Block::getGeometry)
            .filter(Objects::nonNull)
            .forEach(Geometry::removeFromParent);
        blocks.clear();
    }

    public Node getWorldNode() {
        return worldNode;
    }
}
