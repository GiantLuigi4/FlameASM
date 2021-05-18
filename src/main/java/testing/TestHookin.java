package testing;

import tfc.flameasm.annotations.hookin.Hookin;
import tfc.flameasm.annotations.hookin.Insert;

import java.util.Random;

@Hookin(target = "testing.DummyClass")
public class TestHookin {
	@Insert(target = "print5Things()V", point = "TOP", mappings = "NONE")
	public void prePrint5Things() {
		for (int index = 0; index < 5; index++) {
			System.out.println(new Random().nextInt());
		}
	}
	
	@Insert(target = "main([Ljava/lang/String;)V", point = "BOTTOM", mappings = "NONE")
	public void postMain() {
		print5Things();
	}
	
	private static void print5Things() {
	}
}
