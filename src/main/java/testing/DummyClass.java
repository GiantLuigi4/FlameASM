package testing;

import net.minecraft.registry.BlockRegistry;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.util.resource.ResourceName;
import net.minecraft.util.vecmath.Matrix4;
import net.minecraft.util.vecmath.Vector3d;
import net.minecraft.world.blocks.BlockProperties;
//import tfc.flamemc.API.Registry;

//TODO: remove this
public class DummyClass {
	public static void main(String[] args) {
		System.out.println(TestExtend.class);
		
		for (TestEnum value : TestEnum.values()) {
			System.out.println(value);
		}
		
		try {
			BlockRegistry.register("test:test", BlockRegistry.STONE_BLOCK);
		} catch (Throwable err) {
			err.printStackTrace();
		}
		
		System.out.println((new Vector3d(1.0D, 2.0D, 3.0D)).dotProduct(new Vector3d(0.0D, 0.0D, 0.0D)));
		System.out.println(Matrix4.perspectiveMatrix(90, 1, 0.01f, 1000));

		System.out.println(name);
		String test = "2";
		Object o = new Object();
		System.out.println(o.toString());
		System.out.println(test + "1");

		ResourceName name = new ResourceName("test:test");
		System.out.println(name.namespace());
		System.out.println(name.path());

		System.out.println(BuiltinRegistries.BLOCKS);
		System.out.println(BlockRegistry.AIR);
		System.out.println(BlockRegistry.STONE_BLOCK);
		
//		Registry.register(
//				Registry.Register.BLOCKS,
//				new ResourceName("flameasm:test_extend"),
//				new TestExtend(BlockProperties.from(BlockRegistry.STONE_BLOCK))
//		);
		
		System.out.println((long)0);
		int val = 0;
		val += 2;
		val *= 3;
		int val1 = 0;
		val1 += 9;
		val1 *= -3;
		val += val1;
		System.out.println((long)val);
		test();
		printName(name);
	}

	public static void print5Things() {
		// oh no, someone forgot to fill out this method
		// better make a hookin to fill it out
	}

	public static void printName(ResourceName name) {
		// it is very deliberate that I'm not just using toString
		// I know toString gives the exact same result
		// I just wanna maximize references for testing purposes
		System.out.println(name.namespace() + ":" + name.path());
	}

	public static void test() {
		ResourceName name = new ResourceName("hello:yes");
		System.out.println(name);
		Class<?> clazz = ResourceName.class;
	}

	// TODO: use access modifiers to make this field public static instead of private static final
	private static final ResourceName name = new ResourceName("test:test");
}