package mined;

public class BiomeGenerator {
    private final NoiseGenerator temperatureNoise;
    private final NoiseGenerator rainfallNoise;
    
    public BiomeGenerator(long seed) {
        this.temperatureNoise = new NoiseGenerator(seed);
        this.rainfallNoise = new NoiseGenerator(seed + 1);
    }
    
    public Biome getBiomeAt(int x, int z) {
        double temperature = temperatureNoise.noise(x * 0.02, z * 0.02);
        double rainfall = rainfallNoise.noise(x * 0.02, z * 0.02);
        
        // Normalize noise values to 0-1 range
        temperature = (temperature + 1) * 0.5;
        rainfall = (rainfall + 1) * 0.5;
        
        if (temperature > 0.6 && rainfall < 0.2) {
            return Biome.DESERT;
        } else if (temperature > 0.3 && rainfall > 0.6) {
            return Biome.FOREST;
        } else if (temperature > 0.7 && rainfall > 0.4) {
            return Biome.MOUNTAINS;
        } else {
            return Biome.PLAINS;
        }
    }
}