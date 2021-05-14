package entries.FlameASM;

import net.minecraft.registry.MainRegistry;
import net.minecraft.resource.ResourceName;

public class DummyClass {
	public static void main(String[] args) {
		String test = "2";
		Object o = new Object();
		System.out.println(o.toString());
		System.out.println(test + "1");
		
		ResourceName name = new ResourceName("test:test");
		System.out.println(name.getNamespace());
		System.out.println(name.getPath());
		
		System.out.println(MainRegistry.blocks);
		
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
	
	public static void printName(ResourceName name) {
		// it is very deliberate that I'm not just using toString
		// I know toString gives the exact same result
		// I just wanna maximize references for testing purposes
		System.out.println(name.getNamespace() + ":" + name.getPath());
	}
	
	public static void test() {
		ResourceName name = new ResourceName("hello:yes");
		System.out.println(name);
		Class<?> clazz = ResourceName.class;
	}
}
