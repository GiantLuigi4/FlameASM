package entries.FlameASM;

import com.tfc.mappings.structure.*;
import com.tfc.mappings.structure.Class;
import com.tfc.mappings.types.Intermediary;
import tfc.flame.FlameURLLoader;
import tfc.flame.IFlameAPIMod;
import tfc.flame.IFlameMod;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.remapper.MappingApplicator;
import tfc.flamemc.FlameLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Main implements IFlameMod, IFlameAPIMod {
	@Override
	public void setupAPI(String[] args) {
		try {
			String s = new String();
		} catch (Throwable err) {
		}
		ClassLoader loader = Main.class.getClassLoader();
		if (loader instanceof FlameURLLoader) {
			try {
				java.lang.Class<?> clazz = java.lang.Class.forName("com.tfc.mappings.structure.FlameMapHolder");
				if (clazz != null) {
					FlameMapHolder flame = new FlameMapHolder(readUrl("https://raw.githubusercontent.com/GiantLuigi4/FlameAPI-MC-Rewrite/master/mappings/flame_mappings.mappings"));
					boolean isVersion = false;
					String versionMap = "";
					for (String s : args) {
						if (s.equals("--version")) {
							isVersion = true;
						} else if (isVersion) {
							String version = s;
							versionMap = version.replace("-flame", "");
							isVersion = false;
						}
					}
					Holder intermediary = Intermediary.generate(versionMap);
					MappingApplicator.classMapper = (name) -> {
						Class clazzFlame = flame.getFromSecondaryName(name);
						if (clazzFlame == null) return null;
						String interName = clazzFlame.getPrimaryName();
						Class interClass = intermediary.getFromPrimaryName(interName);
						if (interClass == null) return null;
						return interClass.getSecondaryName();
					};
					MappingApplicator.methodMapper = (name, methodName) -> {
						//TODO: make this work for when there are multiple methods with the same name
						Class clazzFlame = flame.getFromSecondaryName(name);
						if (clazzFlame == null) return null;
						String interName = clazzFlame.getPrimaryName();
						Method m = clazzFlame.getMethodPrimary(methodName);
						if (m == null) return null;
						String interMethodName = m.getSecondary();
						Class interClass = intermediary.getFromPrimaryName(interName);
						if (interClass == null) return null;
						m = interClass.getMethodPrimary(interMethodName);
						if (m == null) return null;
						return m.getSecondary();
					};
					MappingApplicator.fieldMapper = (name, fieldName) -> {
						Class clazzFlame = flame.getFromSecondaryName(name);
						if (clazzFlame == null) return null;
						String interName = clazzFlame.getPrimaryName();
						Field selected = null;
						for (Field field : clazzFlame.getFields())
							if (field.getPrimary().equals(fieldName)) {
								selected = field;
								break;
							}
						if (selected == null) return null;
						String interMethodName = selected.getSecondary();
						Class interClass = intermediary.getFromPrimaryName(interName);
						if (interClass == null) return null;
						for (Field field : interClass.getFields())
							if (field.getPrimary().equals(interMethodName)) {
								selected = field;
								break;
							}
						if (selected == null) return null;
						return selected.getSecondary();
					};
					MappingApplicator.classToMappingInfoFunc = (name)->{
						String jarEntry = name.replace(".","/") + ".class";
						File jar = FlameLauncher.getJarForEntry(jarEntry);
						if (jar == null) return null;
						try {
							byte[] bytes = FlameLauncher.getSourceFile(jar, "mapping_info.properties");
							if (bytes == null) return null;
							return new String(bytes);
						} catch (NullPointerException ignored) {
							System.out.println("File " + jar.toString() + " did not contain a mapping_info.properties");
							return null;
						}
					};
				}
			} catch (Throwable ignored) {
			}
			((FlameURLLoader) loader).getAsmAppliers().put("flameasm:asm", ASMApplicator::apply);
		}
	}
	
	private static String readUrl(String urlString) throws IOException {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder builder = new StringBuilder();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				builder.append(chars, 0, read);
			
			return builder.toString();
			
		} finally {
			if (reader != null)
				reader.close();
		}
	}
	
	@Override
	public void preinit(String[] strings) {
	}
	
	@Override
	public void init(String[] strings) {}
	
	@Override
	public void postinit(String[] strings) {
		DummyClass.main(strings);
	}
}
