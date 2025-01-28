// Direction.java
package mined;

import com.jme3.math.Vector3f;

public enum Direction {
    NORTH(new Vector3f(0, 0, 1)),
    SOUTH(new Vector3f(0, 0, -1)),
    EAST(new Vector3f(1, 0, 0)),
    WEST(new Vector3f(-1, 0, 0)),
    UP(new Vector3f(0, 1, 0)),
    DOWN(new Vector3f(0, -1, 0));

    private final Vector3f normal;

    Direction(Vector3f normal) {
        this.normal = normal;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            case UP: return DOWN;
            case DOWN: return UP;
            default: return null;
        }
    }
}