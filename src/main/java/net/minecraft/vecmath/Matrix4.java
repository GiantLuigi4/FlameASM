package net.minecraft.vecmath;

import java.nio.FloatBuffer;

public class Matrix4 {
	public Matrix4() {
	}
	
	public Matrix4(Matrix4 parent) {
	}
	
	// TODO: quaternion constructor
	
	/**
	 * Method is only present client side
	 */
	public void toFloatBuffer(FloatBuffer buffer) {
	}
	
	/**
	 * Method is only present client side
	 */
	public void identity() {
	}
	
	/**
	 * Method is only present client side
	 */
	public void scale(float amplifier) {
	}
	
	/**
	 * Method is only present client side
	 */
	public void multiply(Matrix4 matrix) {
	}
	
	// TODO: rotate method
	
	/**
	 * Method is only present client side
	 */
	public static Matrix4 perspectiveMatrix(double fov, float aspect, float near, float far) {
		return new Matrix4();
	}
	
	/**
	 * Method is only present client side
	 */
	public static Matrix4 orthographicMatrix(float width, float height, float near, float far) {
		return new Matrix4();
	}
	
	public Matrix4 copy() {
		return new Matrix4();
	}
	
	public static Matrix4 translationMatrix(float x, float y, float z) {
		return new Matrix4();
	}
	
	public static Matrix4 scaleMatrix(float x, float y, float z) {
		return new Matrix4();
	}
}
