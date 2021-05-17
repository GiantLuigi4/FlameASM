package testing;

import net.minecraft.world.blocks.Block;
import net.minecraft.world.blocks.BlockProperties;

public class TestExtend extends Block {
	public TestExtend(BlockProperties properties) {
		super(properties);
	}
	
	@Override
	public float friction() {
		return 1;
	}
	
	@Override
	public float speedFactor() {
		return 1.2f;
	}
	
	@Override
	public float jumpFactor() {
		return 1f;
	}
}
