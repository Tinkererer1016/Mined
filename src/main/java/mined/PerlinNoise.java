package mined;

import java.util.Random;

public class PerlinNoise {
    private final int[] permutation;
    private final int[] p;

    public PerlinNoise() {
        this(new Random().nextInt());
    }

    public PerlinNoise(int seed) {
        permutation = new int[256];
        p = new int[512];
        Random random = new Random(seed);
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int tmp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i % 256];
        }
    }

    public float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        float u = fade(x);
        float v = fade(y);
        int A = p[X] + Y;
        int B = p[X + 1] + Y;
        return lerp(v, lerp(u, grad(p[A], x, y), grad(p[B], x - 1, y)), lerp(u, grad(p[A + 1], x, y - 1), grad(p[B + 1], x - 1, y - 1))) * 2.0f;
    }

    private float fade(float t) {
        return t * t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}