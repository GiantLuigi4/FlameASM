package tfc.flameasm;

import tfc.flameasm.remapper.MappingApplicator;

import java.io.File;
import java.io.FileOutputStream;

public class ASMApplicator {
	public static byte[] apply(String s, byte[] bytes) {
		// These things should occur in the order they are listed in
		byte[] srcBytes = bytes;
		bytes = MappingApplicator.apply(s, bytes);
		// TODO: access modifiers (basically annotation based access transformers, afaik, org.ow2.asm is not powerful enough for this, as it doesn't keep default field initializers)
		// TODO: hookins (weaker mixins which are runtime mapped instead of having a refmap, and can be directly referenced)
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
}
