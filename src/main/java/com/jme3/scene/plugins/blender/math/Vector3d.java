package com.jme3.scene.plugins.blender.math;

public class Vector3d {
    public double x, y, z;

    public Vector3d() {
        this(0, 0, 0);
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d add(Vector3d other) {
        return new Vector3d(x + other.x, y + other.y, z + other.z);
    }

    public Vector3d subtract(Vector3d other) {
        return new Vector3d(x - other.x, y - other.y, z - other.z);
    }

    public Vector3d multiply(double scalar) {
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    public double distance(Vector3d other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3d normalize() {
        double len = length();
        if (len != 0) {
            return new Vector3d(x / len, y / len, z / len);
        }
        return new Vector3d(x, y, z);
    }
}
