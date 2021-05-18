import org.lwjgl.system.Configuration;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.remapper.MappingApplicator;
import tfc.flamemc.FlameLauncher;

import java.io.File;

public class Test {
	public static void main(String[] args) {
		System.out.println(ASMApplicator.parseDescriptor(
				"(Ljava/lang/String;IIJ)I"
		));
		
		FlameLauncher.isDev = true;
		
		Configuration.LIBRARY_PATH.set(new File("libraries").getPath());
		
		File f = new File("");
		args = new String[]{
				"--username", "GiantLuigi4",
				"--version", "1.16.5",
//				"--version", "1.16.5-srg",
				"--gameDir", f.getAbsolutePath() + "\\run",
				"--assetsDir", FlameLauncher.findMCDir(false) + "\\assets",
				"--assetIndex", "1.16",
				"--accessToken", "PLEASE_FLAME_WORK_I_BEG_YOU",
				"--uuid", "ad1dbe37-ce3b-41d9-a4d0-8c2d67f99b39",
				"--userType", "mojang",
				"--versionType", "release"
		};
		FlameLauncher.main(args);
	}
}
