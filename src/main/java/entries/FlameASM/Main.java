package entries.FlameASM;

import com.tfc.mappings.structure.*;
import com.tfc.mappings.structure.Class;
import com.tfc.mappings.types.Intermediary;
import testing.DummyClass;
import tfc.flame.FlameURLLoader;
import tfc.flame.IFlameAPIMod;
import tfc.flame.IFlameMod;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.CSVReader;
import tfc.flameasm.remapper.MappingApplicator;
import tfc.flamemc.FlameLauncher;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;

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
					ASMApplicator.jarGetter = (className)->{
						String jarEntry = className.replace(".","/") + ".class";
						File jar = FlameLauncher.getJarForEntry(jarEntry);
						if (jar == null) return null;
						return jar;
					};
					Enumeration<URL> urls = Main.class.getClassLoader().getResources("hookins.csv");
					((FlameURLLoader) loader).getAsmAppliers().put("flameasm:asm", ASMApplicator::apply);
					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						InputStream stream = url.openStream();
						ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
						try {
							int b;
							while ((b = stream.read()) != -1) stream1.write(b);
							String firstEntry = null;
							for (String entry : new CSVReader(new String(stream1.toByteArray())).entries) {
								if (firstEntry == null) firstEntry = entry;
								else java.lang.Class.forName(firstEntry + "." + entry);
							}
						} catch (Throwable ignored) {
						} finally {
							stream.close();
							stream1.close();
							stream1.flush();
						}
					}
//					HashMap<File, HashMap<String, byte[]>> files = (HashMap<File, HashMap<String, byte[]>>) f.get(null);
//					for (File file : files.keySet()) {
//						byte[] bytes = FlameLauncher.getSourceFile(file, "hookins.csv");
//						if (bytes == null) continue;
//
//					}
					return;
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
