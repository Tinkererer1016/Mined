package mined;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Player {
    private float rotationX = 0;
    private float rotationY = 0;
    private float x = 0;
    private float y = 100;
    private float z = 0;
    private double lastX;
    private double lastY;
    private long windowHandle;
    private GLFWCursorPosCallback cursorCallback;
    private boolean isMouseLocked = false;

    public Player(long window) {
        this.windowHandle = window;
        
        // Get window center position
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(windowHandle, width, height);
        lastX = width[0] / 2.0;
        lastY = height[0] / 2.0;
        
        // Setup mouse movement callback
        cursorCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (isMouseLocked) {
                    handleMouseMovement(xpos, ypos);
                }
            }
        };
        
        GLFW.glfwSetCursorPosCallback(windowHandle, cursorCallback);
        
        // Setup key callback to lock/unlock mouse
        GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    isMouseLocked = false;
                    GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                } else if (key == GLFW.GLFW_KEY_ENTER && action == GLFW.GLFW_PRESS) {
                    isMouseLocked = true;
                    GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                }
            }
        };
        
        GLFW.glfwSetKeyCallback(windowHandle, keyCallback);
        
        // Force cursor disable immediately
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    private void handleMouseMovement(double xpos, double ypos) {
        float mouseSensitivity = 0.1f;
        
        double dx = xpos - lastX;
        double dy = lastY - ypos;
        
        rotationY += dx * mouseSensitivity;
        rotationX += dy * mouseSensitivity;
        
        // Clamp vertical rotation
        if(rotationX > 89.0f)
            rotationX = 89.0f;
        if(rotationX < -89.0f)
            rotationX = -89.0f;
            
        lastX = xpos;
        lastY = ypos;
    }

    public void update() {
        if (isMouseLocked) {
            // Center cursor each frame when locked
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetWindowSize(windowHandle, width, height);
            GLFW.glfwSetCursorPos(windowHandle, width[0] / 2.0, height[0] / 2.0);
        }
    }

    public void setMouseLocked(boolean locked) {
        this.isMouseLocked = locked;
        if (locked) {
            // Reset last position when locking to prevent jump
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetWindowSize(windowHandle, width, height);
            lastX = width[0] / 2.0;
            lastY = height[0] / 2.0;
        }
    }

    public void cleanup() {
        if (cursorCallback != null) {
            cursorCallback.free();
        }
    }

    public float getRotationX() { return rotationX; }
    public float getRotationY() { return rotationY; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
}
