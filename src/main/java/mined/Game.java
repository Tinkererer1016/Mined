package mined;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Game {
    private Player player;
    private boolean isGameReady = false;
    private long window;

    public Game(long windowHandle) {
        this.window = windowHandle;
        
        // Force cursor disable immediately
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        
        // Make sure the window is focused
        GLFW.glfwFocusWindow(window);
        
        // Set up escape key to exit
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }
        });

        initializeGame();
    }

    private void initializeGame() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float aspectRatio = 1920f / 1080f;
        float fov = 75.0f;
        float zNear = 0.1f;
        float zFar = 1000.0f;
        float fH = (float) (Math.tan(fov / 360.0 * Math.PI) * zNear);
        float fW = fH * aspectRatio;
        GL11.glFrustum(-fW, fW, -fH, fH, zNear, zFar);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        player = new Player(window);
        isGameReady = true;
    }

    public void update() {
        if (isGameReady && player != null) {
            player.update();
            
            GL11.glLoadIdentity();
            GL11.glRotatef(player.getRotationX(), 1, 0, 0);
            GL11.glRotatef(player.getRotationY(), 0, 1, 0);
            GL11.glTranslatef(-player.getX(), -player.getY(), -player.getZ());
        }
    }

    public void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (player != null) {
            player.cleanup();
        }
    }
}
