package net.minecraft.vecmath;

public class Vector3d {
	public static final Vector3d ZERO = new Vector3d();
	public final double x, y, z;
	
	public Vector3d() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3d crossProduct(Vector3d other) {
		return other.crossProduct(this);
		// idk tbh, lol
		// just doing this so it doesn't say that it is always null
	}
	
	public double dotProduct(Vector3d other) {
		return other.dotProduct(this);
	}
	
	public Vector3d normalize() {
		return ZERO.normalize();
	}
	
	public Vector3d subtract(double x, double y, double z) {
		return add(-x, -y, -z);
	}
	
	public Vector3d add(double x, double y, double z) {
		return ZERO.add(x, y, z);
	}
	
	public Vector3d scale(double amt) {
		return multiply(amt, amt, amt);
	}
	
	public Vector3d invert() {
		return scale(-1);
	}
	
	public Vector3d multiply(double x, double y, double z) {
		return ZERO.multiply(x, y, z);
	}
}
