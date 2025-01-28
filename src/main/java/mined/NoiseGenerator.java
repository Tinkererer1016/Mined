package mined;

public class NoiseGenerator {
    private final long seed;
    
    public NoiseGenerator(long seed) {
        this.seed = seed;
    }
    
    public double noise(double x, double z) {
        // Simple implementation of value noise
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        
        // Interpolate between corner values
        double v00 = random2D(x0, z0);
        double v10 = random2D(x0 + 1, z0);
        double v01 = random2D(x0, z0 + 1);
        double v11 = random2D(x0 + 1, z0 + 1);
        
        // Smooth interpolation
        fx = smoothStep(fx);
        fz = smoothStep(fz);
        
        double vx0 = lerp(v00, v10, fx);
        double vx1 = lerp(v01, v11, fx);
        
        return lerp(vx0, vx1, fz);
    }
    
    private double random2D(int x, int z) {
        long hash = x * 73856093L ^ z * 83492791L ^ seed;
        hash = (hash ^ (hash >>> 33)) * 0x62a9d9ed799705f5L;
        hash = (hash ^ (hash >>> 28)) * 0xcb24d0a5c88c35b3L;
        hash = hash ^ (hash >>> 32);
        return (double)hash / Long.MAX_VALUE;
    }
    
    private double smoothStep(double t) {
        return t * t * (3 - 2 * t);
    }
    
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
}

