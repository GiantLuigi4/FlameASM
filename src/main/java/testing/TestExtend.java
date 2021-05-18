package testing;

import net.minecraft.util.collision.voxel.VoxelShape;
import net.minecraft.util.collision.voxel.VoxelShapeHelper;
import net.minecraft.util.context.ISelectionContext;
import net.minecraft.util.vecmath.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.blocks.Block;
import net.minecraft.world.blocks.BlockProperties;
import net.minecraft.world.blocks.BlockState;
import net.minecraft.world.entities.AbstractEntity;
import net.minecraft.world.interfaces.IBlockContainer;
import net.minecraft.world.position.BlockPosition;

public class TestExtend extends Block {
	public TestExtend(BlockProperties properties) {
		super(properties);
	}
	
	@Override
	public float speedFactor() {
		return 0.4f;
	}
	
	@Override
	public float jumpFactor() {
		return 0.5f;
	}
	
	@Override
	public void entityCollision(BlockState state, World world, BlockPosition pos, AbstractEntity entity) {
		Vector3d vector3d = entity.getMot();
		if (vector3d.y > 0) return;
		if (vector3d.y < -0.13D) {
			double d0 = -0.05D / vector3d.y;
			entity.setMot(vector3d.x * d0, Math.max(-0.05D, vector3d.y), vector3d.z * d0);
		} else {
			entity.setMot(vector3d.x, Math.max(-0.05D, vector3d.y), vector3d.z);
		}
	}
	
	private static final VoxelShape shape = VoxelShapeHelper.createShape(0.0625, 0, 0.0625, 0.9375, 0.9375, 0.9375);
	
	@Override
	public VoxelShape collisionShape(BlockState state, IBlockContainer reader, BlockPosition position, ISelectionContext context) {
		return shape;
	}
	
	@Override
	public VoxelShape shape(BlockState state, IBlockContainer reader, BlockPosition position) {
		return shape;
	}
}
