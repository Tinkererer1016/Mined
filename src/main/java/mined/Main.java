package mined;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

public class Main extends SimpleApplication {
    private static Main instance;
    private static final float MOVE_SPEED = 10f;
    private static final float PLAYER_HEIGHT = 1.8f;
    private static final int GROUND_HEIGHT = 64;
    private static final float MAX_REACH = 5.0f;
    private static final float MOUSE_SENSITIVITY = 0.7f;  // Adjust this value if mouse look is too slow/fast
    private static final float MAX_VERTICAL_ANGLE = (float)(Math.PI/2 - 0.1f); 
    private static final float MAX_FREE_LOOK_ANGLE = FastMath.PI / 2; // 90 degrees
    private static final float TURN_SPEED = 5f; // Adjust for faster/slower turning
    private static final boolean MOUSE_INVERTED = false;

    private float rotationX = 0;
    private float rotationY = 0;
    private float targetYaw = 0f;  // Where we want the player to face
    private float currentYaw = 0f; // Current player facing direction
    private Node worldNode;
    private Node playerNode;
    private Material dirtMaterial, grassMaterial, stoneMaterial, outlineMaterial;
    private DirectionalLight sun;
    private AmbientLight ambient;
    private float timeOfDay = 6f; // Start at 6am
    private static final float DAY_CYCLE_SPEED = 0.001f; // Adjust for faster/slower days
    private ChunkManager chunkManager;
    private Map<String, Block> blocks = new HashMap<>();
    private BitmapText loadingText;
    private boolean isLoading = true;
    private boolean forward = false;
    private boolean backward = false;
    private boolean left = false;
    private boolean right = false;
    private boolean jump = false;
    private boolean isPaused = false;
    private boolean isMoving = false;
    private float chunkUpdateTimer = 0f;
    private boolean togglePause = false;
    private static final float CHUNK_UPDATE_INTERVAL = 0.5f; // Update every half second

    @Override
    public void simpleInitApp() {
        System.out.println("Starting application initialization...");
        instance = this;
        debugResourceLoading();
        Block.setBlockRegistry(blocks);
        
        // Set up lighting first
        initLighting();
        
        // Initialize materials
        initMaterials();
        
        // Initialize player node first
        playerNode = new Node("PlayerNode");
        rootNode.attachChild(playerNode);

        // Setup world node
        worldNode = new Node("World");
        rootNode.attachChild(worldNode);
        
        // Setup chunk manager
        chunkManager = new ChunkManager(
            this,
            worldNode,
            2,  // render distance
            dirtMaterial,
            grassMaterial,
            stoneMaterial
        );
        
        // Initialize camera with proper settings
        initCameraControls();
        
        // Initialize input
        initKeys();
        
        // Setup loading screen
        initLoadingScreen();
        
        // Start chunk loading
        initChunkLoading();
    }

private void initLighting() {
    // Ambient light for base illumination
    ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.2f));
    rootNode.addLight(ambient);
    
    // Sun for dynamic lighting
    sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
    sun.setColor(ColorRGBA.White.mult(0.7f));
    rootNode.addLight(sun);
}

private void initCameraControls() {
    flyCam.setEnabled(false);
    flyCam.unregisterInput();
    
    // Don't create playerNode here, it should already exist
    if (playerNode == null) {
        System.err.println("WARNING: PlayerNode not initialized!");
        playerNode = new Node("PlayerNode");
        rootNode.attachChild(playerNode);
    }
    
    // Set initial camera position
    cam.setLocation(new Vector3f(0, 100, 0));
    cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
    
    viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
    inputManager.setCursorVisible(false);
    
    // Reset rotation
    rotationX = 0;
    rotationY = 0;
}

private void initKeys() {
    inputManager.clearMappings();
    
    // Movement bindings
    inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addMapping("TogglePause", new KeyTrigger(KeyInput.KEY_ESCAPE));
// Update the mouse input part of initKeys()
    inputManager.addMapping("LookLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
    inputManager.addMapping("LookRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
    inputManager.addMapping("LookUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
    inputManager.addMapping("LookDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

    // Mouse movement listener
    inputManager.addListener((AnalogListener) (name, value, tpf) -> {
        if (!isPaused) {
            switch(name) {
                case "LookLeft":
                    rotationY += value * MOUSE_SENSITIVITY;
                    break;
                case "LookRight":
                    rotationY -= value * MOUSE_SENSITIVITY;
                    break;
                case "LookUp":
                    // Swapped sign for LookUp
                    rotationX = Math.max(-MAX_VERTICAL_ANGLE, 
                                       Math.min(MAX_VERTICAL_ANGLE, 
                                       rotationX + value * MOUSE_SENSITIVITY));
                    break;
                case "LookDown":
                    // Swapped sign for LookDown
                    rotationX = Math.max(-MAX_VERTICAL_ANGLE, 
                                       Math.min(MAX_VERTICAL_ANGLE, 
                                       rotationX - value * MOUSE_SENSITIVITY));
                    break;
            }
            
            rotationY = rotationY % (2 * FastMath.PI);
            
            Quaternion rotation = new Quaternion();
            rotation.fromAngles(rotationX, rotationY, 0);
            cam.setRotation(rotation);
        }
    }, "LookLeft", "LookRight", "LookUp", "LookDown");
    // Make sure your key bindings in initKeys include:
inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
    if (!isPaused) {
        switch (name) {
            case "Forward": forward = isPressed; break;
            case "Backward": backward = isPressed; break;
            case "Left": left = isPressed; break;
            case "Right": right = isPressed; break;
            case "Jump": jump = isPressed; break;
            
        }
    }
}, "Forward", "Backward", "Left", "Right", "Jump", "Pause");
    //inputManager.addListener((AnalogListener) (name, value, tpf) -> {
    //if (!isPaused) {
       // System.out.println("Input name: " + name + " Raw value: " + value);
        
        //if (name.equals("MouseX")) {
            // Don't modify the value yet, just print current state
            //System.out.println("Before X rotation: " + rotationY);
       // } else if (name.equals("MouseY")) {
            // Don't modify the value yet, just print current state
           // System.out.println("Before Y rotation: " + rotationX);
        //}
    //}
//}, "MouseX", "MouseY");

    // Hide and capture the cursor
inputManager.setCursorVisible(false);
getContext().getMouseInput().setCursorVisible(false);
// If you specifically need to center the cursor, you can use:
Robot robot;
try {
    robot = new Robot();
    robot.mouseMove(
        (int)(getContext().getSettings().getWidth() / 2),
        (int)(getContext().getSettings().getHeight() / 2)
    );
} catch (AWTException e) {
    e.printStackTrace();
}
}

    private ActionListener actionListener = (String name, boolean isPressed, float tpf) -> {
        if (isPressed && !isLoading) {
            switch (name) {
                case "Break": handleBlockBreak(); break;
                case "Place": handleBlockPlace(); break;
                case "ToggleMouse": togglePause(); break;
            }
        }
    };

private void togglePause() {
    isPaused = !isPaused;
    inputManager.setCursorVisible(isPaused);
    if (!isPaused) {
        // Reset rotation to current camera angles when unpausing
        rotationX = cam.getRotation().toAngles(null)[0];
        rotationY = cam.getRotation().toAngles(null)[1];
    }
}
private void updateDayNightCycle(float tpf) {
    timeOfDay += tpf * DAY_CYCLE_SPEED;
    if (timeOfDay >= 24f) {
        timeOfDay = 0f;
    }

    // Calculate sun angle (0 = midnight, 6 = sunrise, 12 = noon, 18 = sunset)
    float angleRad = ((timeOfDay - 6) / 24f) * FastMath.TWO_PI;
    
    // Update sun direction
    Vector3f sunDirection = new Vector3f();
    sunDirection.y = FastMath.sin(angleRad);
    sunDirection.x = FastMath.cos(angleRad);
    sunDirection.z = -0.5f;
    sun.setDirection(sunDirection.normalizeLocal());

    // Calculate light intensity
    float intensity = Math.max(sunDirection.y + 0.3f, 0.0f);
    sun.setColor(ColorRGBA.White.mult(intensity * 0.7f));
    ambient.setColor(ColorRGBA.White.mult(0.2f + (intensity * 0.3f)));

    // Update sky color
    float skyBrightness = Math.max(intensity, 0.1f);
    float redTint = Math.max(0f, -sunDirection.y * 0.5f); // Red tint at sunset/sunrise
    ColorRGBA skyColor = new ColorRGBA(
        0.5f + (redTint * 0.5f),
        0.6f * skyBrightness,
        1.0f * skyBrightness,
        1.0f
    );
    viewPort.setBackgroundColor(skyColor);
}
    private void handleBlockBreak() {
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        Block targetBlock = raycastBlock(ray, MAX_REACH);
        if (targetBlock != null) {
            targetBlock.scheduleBreak();
        }
    }

    private void handleBlockPlace() {
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        Block hitBlock = raycastBlock(ray, MAX_REACH);
        if (hitBlock != null) {
            Vector3f blockCenter = hitBlock.getPosition();
            float dx = ray.origin.x - blockCenter.x;
            float dy = ray.origin.y - blockCenter.y;
            float dz = ray.origin.z - blockCenter.z;
            Vector3f placePos = blockCenter.clone();

            if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz)) {
                placePos.x += (dx > 0) ? 1 : -1;
            } else if (Math.abs(dy) > Math.abs(dz)) {
                placePos.y += (dy > 0) ? 1 : -1;
            } else {
                placePos.z += (dz > 0) ? 1 : -1;
            }

            String key = Math.round(placePos.x) + "," + Math.round(placePos.y) + "," + Math.round(placePos.z);
            if (!blocks.containsKey(key)) {
                Block.placeBlock(placePos, BlockType.DIRT, worldNode, dirtMaterial);
            }
        }
    }

    private Block raycastBlock(Ray ray, float maxDistance) {
        Block closestBlock = null;
        float closestDistance = maxDistance;

        for (Block block : blocks.values()) {
            if (block.getGeometry() != null && block.intersects(ray.origin, maxDistance)) {
                float distance = block.getPosition().distance(ray.origin);
                if (distance < closestDistance) {
                    closestBlock = block;
                    closestDistance = distance;
                }
            }
        }
        return closestBlock;
    }
    private void updateCameraRotation() {
    // Create quaternion for rotation
    Quaternion rotation = new Quaternion();
    rotation.fromAngles(rotationX, rotationY, 0);
    cam.setRotation(rotation);
}
    private int findHighestBlock(int x, int z, int radius) {
        System.out.println("Searching for highest block at " + x + ", " + z + " with radius " + radius);
        int highest = -1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int worldX = x + dx;
                int worldZ = z + dz;
                int chunkX = Math.floorDiv(worldX, 16);
                int chunkZ = Math.floorDiv(worldZ, 16);

                Chunk chunk = chunkManager.getChunk(chunkX, chunkZ);
                if (chunk != null) {
                    // Convert world coordinates to chunk-local coordinates
                    int localX = Math.floorMod(worldX, 16);
                    int localZ = Math.floorMod(worldZ, 16);

                    int height = chunk.getHighestBlock(localX, localZ);
                    System.out.printf("Chunk(%d,%d) Local(%d,%d) Height: %d%n", chunkX, chunkZ, localX, localZ, height);

                    if (height > highest) {
                        highest = height;
                    }
                }
            }
        }

        System.out.println("Final highest block found: " + highest);
        return highest;
    }
    
private void initChunkLoading() {
    enqueue(() -> {
        try {
            System.out.println("Starting world generation...");
            int spawnRadius = 2;
            
            // Generate chunks first
            for (int r = 0; r <= spawnRadius; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        if (Math.abs(x) == r || Math.abs(z) == r) {
                            chunkManager.generateChunk(x, z);
                            System.out.println(String.format("Generating chunk at %d, %d", x, z));
                        }
                    }
                }
            }

            // Find spawn position
            int highestY = findHighestBlock(0, 0, 2);
            Vector3f spawnPos = new Vector3f(0, GROUND_HEIGHT + 10, 0);
            if (highestY != -1) {
                spawnPos.y = highestY + 5; // Spawn slightly above highest block
            } else {
                System.err.println("WARNING: No valid spawn position found! Using default height.");
            }

            // Set player and camera position
            if (playerNode != null) {
                playerNode.setLocalTranslation(spawnPos);
                cam.setLocation(spawnPos.add(0, PLAYER_HEIGHT, 0));
                // Reset rotation
                rotationX = 0;
                rotationY = 0;
                cam.setRotation(new Quaternion().fromAngles(0, 0, 0));
            } else {
                System.err.println("ERROR: PlayerNode is null!");
            }

            // Clean up loading screen
            guiNode.detachAllChildren();
            isLoading = false;
            setUpPaused(false);
        } catch (Exception e) {
            System.err.println("Error during chunk generation: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    });
}

    private void setUpPaused(boolean paused) {
        this.isPaused = paused;
        inputManager.setCursorVisible(paused);
        if (!paused) {
            rotationX = 0;
            rotationY = 0;
            cam.setRotation(new Quaternion().fromAngles(0, 0, 0));
        }
    }

    private void updateChunks() {
        if (!isLoading) {
            Vector3f playerPos = cam.getLocation();
            int playerChunkX = Math.floorDiv((int)playerPos.x, 16);
            int playerChunkZ = Math.floorDiv((int)playerPos.z, 16);
            
            chunkManager.updateChunks(playerChunkX, playerChunkZ);
        }
    }

@Override
public void simpleUpdate(float tpf) {
    super.simpleUpdate(tpf);
    if (!isLoading && !isPaused) {
        // Get screen width and calculate turn zones
        float screenWidth = getContext().getSettings().getWidth();
        float mouseX = inputManager.getCursorPosition().x;
        float turnSpeed = 0.55f * tpf;

        // Check if player is walking (only forward/backward, not strafing)
        boolean isWalking = forward || backward;

        // Only turn if walking - single rotation check
        if (isWalking) {
            if (mouseX < screenWidth / 12) {
                rotationY += turnSpeed;
            } else if (mouseX > (screenWidth * 11) / 12) {
                rotationY -= turnSpeed;
            }
        }

        // Update camera rotation
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(rotationX, rotationY, 0);
        cam.setRotation(rotation);

        // Rest of your existing movement code
        Vector3f moveDir = new Vector3f();
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0;
        camDir.normalizeLocal();
        camLeft.y = 0;
        camLeft.normalizeLocal();

        if (forward) moveDir.addLocal(camDir);
        if (backward) moveDir.addLocal(camDir.negate());
        if (left) moveDir.addLocal(camLeft);
        if (right) moveDir.addLocal(camLeft.negate());

        if (moveDir.lengthSquared() > 0) {
            moveDir.normalizeLocal();
            Vector3f pos = cam.getLocation();
            Vector3f newPos = pos.add(moveDir.mult(MOVE_SPEED * tpf));
            System.out.println("Moving from " + pos + " to " + newPos);

            // Ground collision check
            int highestY = findHighestBlock((int) newPos.x, (int) newPos.z, 1);
            if (highestY != -1) {
                float minHeight = highestY + 2;
                if (newPos.y < minHeight) {
                    newPos.y = minHeight;
                }
            }
            cam.setLocation(newPos);
        }

        // Debug camera position periodically
        if (System.currentTimeMillis() % 1000 < 16) {
            System.out.println("Camera position: " + cam.getLocation());
            System.out.println("Rotation: X=" + rotationX + " Y=" + rotationY);
        }
                // Add this line before the end of the method
        updateDayNightCycle(tpf);
    }
    
}

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.WARNING);
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Mined");
        settings.setResolution(1024, 768);
        settings.setVSync(true);
        app.setSettings(settings);
        app.start();
    }

    public static Main getInstance() {
        return instance;
    }

    private void debugResourceLoading() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Asset Manager Class = " + assetManager.getClass().getName());
    }

    private void addCoordinateAxes() {
        Arrow arrow_x = new Arrow(Vector3f.UNIT_X);
        Arrow arrow_y = new Arrow(Vector3f.UNIT_Y);
        Arrow arrow_z = new Arrow(Vector3f.UNIT_Z);

        Geometry x_geo = new Geometry("X_AXIS", arrow_x);
        Geometry y_geo = new Geometry("Y_AXIS", arrow_y);
        Geometry z_geo = new Geometry("Z_AXIS", arrow_z);

        Material red = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material blue = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material green = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        red.setColor("Color", ColorRGBA.Red);
        blue.setColor("Color", ColorRGBA.Blue);
        green.setColor("Color", ColorRGBA.Green);

        x_geo.setMaterial(red);
        y_geo.setMaterial(blue);
        z_geo.setMaterial(green);

        rootNode.attachChild(x_geo);
        rootNode.attachChild(y_geo);
        rootNode.attachChild(z_geo);
    }

private void initMaterials() {
    MaterialDef unshadedDef = (MaterialDef) assetManager.loadAsset("Common/MatDefs/Misc/Unshaded.j3md");
    System.out.println("\n=== Starting Material Initialization ===");
    
    // Dirt material
    dirtMaterial = new Material(unshadedDef);
    try {
        Texture dirtTex = assetManager.loadTexture("Textures/dirt.png");
        dirtTex.setWrap(Texture.WrapMode.Repeat);
        dirtTex.setMagFilter(Texture.MagFilter.Nearest);
        dirtTex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        dirtMaterial.setTexture("ColorMap", dirtTex);
        System.out.println("\nDirt texture info:");
        System.out.println("  Size: " + dirtTex.getImage().getWidth() + "x" + dirtTex.getImage().getHeight());
        System.out.println("  Format: " + dirtTex.getImage().getFormat());
        System.out.println("  Mag filter: " + dirtTex.getMagFilter());
        System.out.println("  Min filter: " + dirtTex.getMinFilter());
        // Add a very slight tint to verify material is being applied
        dirtMaterial.setColor("Color", new ColorRGBA(1f, 0.95f, 0.95f, 1f));
    } catch (Exception e) {
        System.err.println("Dirt texture error: " + e);
        e.printStackTrace();
        dirtMaterial.setColor("Color", ColorRGBA.Red);
    }

    // Grass material
    grassMaterial = new Material(unshadedDef);
    try {
        Texture grassTex = assetManager.loadTexture("Textures/grass.png");
        grassTex.setWrap(Texture.WrapMode.Repeat);
        grassTex.setMagFilter(Texture.MagFilter.Nearest);
        grassTex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        grassMaterial.setTexture("ColorMap", grassTex);
        System.out.println("\nGrass texture info:");
        System.out.println("  Size: " + grassTex.getImage().getWidth() + "x" + grassTex.getImage().getHeight());
        System.out.println("  Format: " + grassTex.getImage().getFormat());
        System.out.println("  Mag filter: " + grassTex.getMagFilter());
        System.out.println("  Min filter: " + grassTex.getMinFilter());
        // Add a very slight tint to verify material is being applied
        grassMaterial.setColor("Color", new ColorRGBA(0.95f, 1f, 0.95f, 1f));
    } catch (Exception e) {
        System.err.println("Grass texture error: " + e);
        e.printStackTrace();
        grassMaterial.setColor("Color", ColorRGBA.Green);
    }

    // Stone material
    stoneMaterial = new Material(unshadedDef);
    try {
        Texture stoneTex = assetManager.loadTexture("Textures/stone.png");
        stoneTex.setWrap(Texture.WrapMode.Repeat);
        stoneTex.setMagFilter(Texture.MagFilter.Nearest);
        stoneTex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        stoneMaterial.setTexture("ColorMap", stoneTex);
        System.out.println("\nStone texture info:");
        System.out.println("  Size: " + stoneTex.getImage().getWidth() + "x" + stoneTex.getImage().getHeight());
        System.out.println("  Format: " + stoneTex.getImage().getFormat());
        System.out.println("  Mag filter: " + stoneTex.getMagFilter());
        System.out.println("  Min filter: " + stoneTex.getMinFilter());
        // Add a very slight tint to verify material is being applied
        stoneMaterial.setColor("Color", new ColorRGBA(0.95f, 0.95f, 1f, 1f));
    } catch (Exception e) {
        System.err.println("Stone texture error: " + e);
        e.printStackTrace();
        stoneMaterial.setColor("Color", ColorRGBA.Blue);
    }

    // Print final material states
    System.out.println("\nFinal material states:");
    System.out.println("Dirt material params: " + dirtMaterial.getParamsMap());
    System.out.println("Grass material params: " + grassMaterial.getParamsMap());
    System.out.println("Stone material params: " + stoneMaterial.getParamsMap());
    System.out.println("\n=== Material Initialization Complete ===\n");
}

    private void initLoadingScreen() {
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        loadingText = new BitmapText(guiFont, false);
        loadingText.setSize(guiFont.getCharSet().getRenderedSize() * 2); // Make text bigger
        loadingText.setText("Generating World...");
        loadingText.setColor(ColorRGBA.White); // Make text white
        // Center the text
        loadingText.setLocalTranslation(
            (settings.getWidth() - loadingText.getLineWidth()) / 2,
            (settings.getHeight() + loadingText.getLineHeight()) / 2,
            0
        );
        guiNode.attachChild(loadingText);

        // Add a black background
        Material blackBg = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blackBg.setColor("Color", ColorRGBA.Black);
        Geometry bgGeom = new Geometry("LoadingBG", new Quad(settings.getWidth(), settings.getHeight()));
        bgGeom.setMaterial(blackBg);
        bgGeom.setLocalTranslation(0, 0, -1);
        guiNode.attachChild(bgGeom);
    }
}
