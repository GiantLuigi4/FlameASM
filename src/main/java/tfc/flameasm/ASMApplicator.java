package tfc.flameasm;

import tfc.flameasm.hookins.HookinApplicator;
import tfc.flameasm.hookins.HookinReader;
import tfc.flameasm.remapper.MappingApplicator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.function.Function;

public class ASMApplicator {
	public static Function<String, File> jarGetter;
	
	public static byte[] apply(String s, byte[] bytes) {
		// These things should occur in the order they are listed in
		if (bytes == null) return null;
		byte[] srcBytes = bytes;
		bytes = MappingApplicator.apply(s, bytes);
		// TODO: access modifiers (basically annotation based access transformers, afaik, org.ow2.asm is not powerful enough for this, as it doesn't keep default field initializers)
		// TODO: hookins (weaker mixins which are runtime mapped instead of having a refmap, and can be directly referenced)
		// These are very poorly written, and do not work at all
		// Feel free to use these as a base, or don't
		// Hookins should add a method to the class being hooked into which runs the code of the hookin, then call that method from the method being hooked into
		// Then the reference renamer should make it so that any classes which directly call a method from a hookin, don't call it on the hookin, but instead on the class being hooked into
		// This won't particularly work out well under what I said above for methods which aren't static, so something will have to be figured out for that
		bytes = HookinReader.apply(s, bytes);
		bytes = HookinApplicator.apply(s, bytes);
		// TODO: hookin reference renamer (renames the references to hookin classes from the reference to a hookin class to the name of the class the hookin is modifying)
		if (bytes == srcBytes) return srcBytes;
		writeBytes(new File("flameasm/out/" + (s.replace(".", "/") + ".class")), bytes);
		return bytes;
	}
	
	public static void writeBytes(File f, byte[] bytes) {
		FileOutputStream stream = null;
		try {
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			stream = new FileOutputStream(f);
			stream.write(bytes);
		} catch (Throwable ignored) {
		}
		try {
			if (stream == null) return;
			stream.flush();
			stream.close();
		} catch (Throwable ignored) {
		}
	}
	
	public static Descriptor parseDescriptor(String desc) {
		String types = desc.substring(1, desc.indexOf(")"));
		boolean isInDescriptor = false;
		StringBuilder parsing = new StringBuilder();
		ArrayList<String> typesArray = new ArrayList<>();
		for (char c : types.toCharArray()) {
			if (isInDescriptor) {
				if (c == ';') {
					isInDescriptor = false;
					parsing.append(';');
					typesArray.add(parsing.toString());
					parsing.delete(0, parsing.length());
					continue;
				}
				parsing.append(c);
			} else {
				if (c == 'L') {
					isInDescriptor = true;
					parsing.append('L');
					continue;
				}
				typesArray.add("" + c);
			}
		}
		return new Descriptor(typesArray.toArray(new String[0]), desc.substring(desc.lastIndexOf(")") + 1));
	}
}
