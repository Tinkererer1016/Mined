package mined;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.shader.VarType;

public class Shader {
    private Material material;
    private AssetManager assetManager;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public Shader(AssetManager assetManager, String vertexPath, String fragmentPath) {
        this.assetManager = assetManager;
        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    }

    public void setUniform(String name, Matrix4f value) {
        material.setParam(name, VarType.Matrix4, value);
    }

    public void setUniform(String name, float value) {
        material.setParam(name, VarType.Float, value);
    }

    public void setUniform(String name, int value) {
        material.setParam(name, VarType.Int, value);
    }

    public void setUniform(String name, Vector3f value) {
        material.setParam(name, VarType.Vector3, value);
    }

    public Material getMaterial() {
        return material;
    }
}
