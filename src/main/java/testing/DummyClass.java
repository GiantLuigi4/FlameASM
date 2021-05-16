package testing;

import modifiers.AccessTransform;
import net.minecraft.registry.MainRegistry;
import net.minecraft.resource.ResourceName;

import java.lang.reflect.Modifier;

//TODO: remove this
public class DummyClass {
	public static void main(String[] args) {
		System.out.println(name);
		String test = "2";
		Object o = new Object();
		System.out.println(o.toString());
		System.out.println(test + "1");
		
		ResourceName name = new ResourceName("test:test");
		System.out.println(name.namespace());
		System.out.println(name.path());
		
		System.out.println(MainRegistry.BLOCKS);
		
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
	@AccessTransform(Modifier.PUBLIC | Modifier.STATIC)
	private static final ResourceName name = new ResourceName("test:test");
}
