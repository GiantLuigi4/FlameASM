package entries.FlameASM;

import testing.DummyClass;
import tfc.flame.FlameURLLoader;
import tfc.flame.IFlameAPIMod;
import tfc.flame.IFlameMod;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.CSVReader;
import tfc.flameasm.Descriptor;
import tfc.flameasm.hookins.HookinApplicator;
import tfc.flameasm.hookins.HookinReader;
import tfc.flameasm.remapper.MappingApplicator;
import tfc.flameasm.remapper.MappingsInfo;
import tfc.flameasm.remapper.MappingsSteps;
import tfc.flameasm.remapper.NoRemap;
import tfc.flamemc.FlameLauncher;
import tfc.mappings.structure.*;
import tfc.mappings.types.Intermediary;
import tfc.mappings.types.Searge;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;

public class Main implements IFlameMod, IFlameAPIMod {
	@Override
	public void setupAPI(String[] args) {
		try {
			java.lang.Class<?> clazz = String.class;
			clazz = Descriptor.class;
			clazz = HookinReader.class;
			clazz = HookinApplicator.class;
			clazz = CSVReader.class;
			clazz = NoRemap.class;
		} catch (Throwable ignored) {
		}
		ClassLoader loader = Main.class.getClassLoader();
		if (loader instanceof FlameURLLoader) {
			try {
				java.lang.Class<?> clazz = java.lang.Class.forName("tfc.mappings.structure.FlameMapHolder");
				FlameMapHolder flame = new FlameMapHolder(readUrl("https://raw.githubusercontent.com/GiantLuigi4/FlameMappings/master/mappings/flame_mappings.mappings"));
				boolean isVersion = false;
				String versionMap = "";
				for (String s : args) {
					if (s.equals("--version")) {
						isVersion = true;
					} else if (isVersion) {
						String version = s;
						if (version.startsWith("fabric-loader")) {
							version = version.substring("fabric-loader-".length());
							version = version.substring(version.indexOf("-") + 1);
						}
						String ver = "";
						for (char c : version.toCharArray()) {
							if (c == '.' || Character.isDigit(c)) ver += c;
							else break;
						}
						version = ver;
						versionMap = version.replace("-flame", "");
						isVersion = false;
					}
				}
				MappingsHolder intermediary = Intermediary.generate(versionMap);
				MappingsHolder searge = Searge.generate(versionMap);

				MappingApplicator.registerMappings(new MappingsInfo(flame, "INTERMEDIARY", "FLAME"));
				MappingApplicator.registerMappings(new MappingsInfo(intermediary, "OBFUSCATION", "INTERMEDIARY"));
				MappingApplicator.registerMappings(new MappingsInfo(searge, "OBFUSCATION", "SEARGE"));

				{
					MappingsSteps steps = MappingApplicator.getSteps("FLAME", "SEARGE");
					boolean hitSwitch = false;
					StringBuilder stepsStr = new StringBuilder();
					{
						MappingsInfo step = steps.next();
						stepsStr.append(step.name).append("->").append(step.builtOn);
					}
					for (MappingsInfo step : steps) {
						if (step == null) {
							hitSwitch = true;
							continue;
						}
						if (hitSwitch) stepsStr.append("->").append(step.name);
						else stepsStr.append("->").append(step.builtOn);
					}
					System.out.println(stepsStr);
				}

				MappingApplicator.classMapper = (name, steps) -> {
					String lastName = name;
					steps.reset();
					boolean switched = false;
					for (MappingsInfo step : steps) {
						if (step == null) {
							switched = true;
							continue;
						}
						MappingsClass mappingsClass;
						if (step.name.equals("FLAME")) {
							if (!switched) mappingsClass = step.mappings.getFromSecondaryName(lastName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastName);
							if (mappingsClass == null) {
								return lastName;
							}
							lastName = switched ? mappingsClass.getSecondaryName() : mappingsClass.getPrimaryName();
						} else {
							if (switched) mappingsClass = step.mappings.getFromSecondaryName(lastName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastName);
							if (mappingsClass == null) {
								return lastName;
							}
							lastName = switched ? mappingsClass.getPrimaryName() : mappingsClass.getSecondaryName();
						}
					}
					return lastName;
				};
				MappingApplicator.methodMapper = (name, steps) -> {
					String lastClassName = name.substring(0, name.indexOf(";"));
					String lastName = name.substring(name.indexOf(";") + 1);
					String descriptor = lastName.substring(lastName.indexOf("("));
					lastName = lastName.substring(0, lastName.indexOf("("));
					steps.reset();
					boolean switched = false;
					for (MappingsInfo step : steps) {
						if (step == null) {
							switched = true;
							continue;
						}
						MappingsClass mappingsClass;
						if (step.name.equals("FLAME")) {
							if (!switched) mappingsClass = step.mappings.getFromSecondaryName(lastClassName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastClassName);
							if (mappingsClass == null) {
								return lastName;
							}
							String mappedDesc = MappingApplicator.mapDesc(descriptor, step.mappings);
							for (MappingsMethod method : mappingsClass.getMethods()) {
								if ((switched ? method.getSecondary() : method.getPrimary()).equals(lastName)) {
									if (method.getDesc().equals(mappedDesc) || method.getDesc().equals(descriptor)) {
										{
											lastClassName = method.getOwner();
											if (!switched)
												mappingsClass = step.mappings.getFromSecondaryName(lastClassName);
											else mappingsClass = step.mappings.getFromPrimaryName(lastClassName);
											if (mappingsClass == null) {
												return lastName;
											}
										}
										lastName = switched ? method.getPrimary() : method.getSecondary();
										descriptor = mappedDesc;
										break;
									}
								}
							}
							lastClassName = switched ? mappingsClass.getSecondaryName() : mappingsClass.getPrimaryName();
						} else {
							if (switched) mappingsClass = step.mappings.getFromSecondaryName(lastClassName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastClassName);
							if (mappingsClass == null) {
								return lastName;
							}
							lastClassName = switched ? mappingsClass.getPrimaryName() : mappingsClass.getSecondaryName();
							String mappedDesc = MappingApplicator.mapDesc(descriptor, step.mappings);
							for (MappingsMethod method : mappingsClass.getMethods()) {
								if (((switched) ? method.getSecondary() : method.getPrimary()).equals(lastName)) {
									if (method.getDesc().equals(mappedDesc) || method.getDesc().equals(descriptor)) {
										{
											lastClassName = method.getOwner();
										}
										lastName = switched ? method.getPrimary() : method.getSecondary();
										descriptor = mappedDesc;
										break;
									}
								}
							}
						}
					}
					return lastName;
				};
				MappingApplicator.fieldMapper = (name, steps) -> {
					String lastClassName = name.substring(0, name.indexOf(";"));
					String lastName = name.substring(name.indexOf(";") + 1);
					steps.reset();
					boolean switched = false;
					for (MappingsInfo step : steps) {
						if (step == null) {
							switched = true;
							continue;
						}
						MappingsClass mappingsClass;
						if (step.name.equals("FLAME")) {
							if (!switched) mappingsClass = step.mappings.getFromSecondaryName(lastClassName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastClassName);
							if (mappingsClass == null) {
								return lastName;
							}
							lastClassName = switched ? mappingsClass.getSecondaryName() : mappingsClass.getPrimaryName();
							for (MappingsField field : mappingsClass.getFields()) {
								if ((switched ? field.getSecondary() : field.getPrimary()).equals(lastName)) {
									lastName = switched ? field.getPrimary() : field.getSecondary();
									break;
								}
							}
						} else {
							if (switched) mappingsClass = step.mappings.getFromSecondaryName(lastClassName);
							else mappingsClass = step.mappings.getFromPrimaryName(lastClassName);
							if (mappingsClass == null) {
								return lastName;
							}
							lastClassName = switched ? mappingsClass.getPrimaryName() : mappingsClass.getSecondaryName();
							for (MappingsField field : mappingsClass.getFields()) {
								if ((switched ? field.getSecondary() : field.getPrimary()).equals(lastName)) {
									lastName = switched ? field.getPrimary() : field.getSecondary();
									break;
								}
							}
						}
					}
					return lastName;
				};
//					MappingApplicator.methodMapper = (name, methodName) -> {
//						MappingsClass clazzFlame = flame.getFromSecondaryName(name);
//						if (clazzFlame == null) return null;
//						String interName = clazzFlame.getPrimaryName();
//						if (methodName.contains("(")) {
//							String descriptor = methodName.substring(methodName.indexOf("("));
//							methodName = methodName.substring(0, methodName.indexOf("("));
//							String inter = "";
//							{
//								String mappedDesc;
//								{
//									Descriptor descriptor1 = ASMApplicator.parseDescriptor(descriptor);
//									for (int index = 0; index < descriptor1.typeNames.length; index++) {
//										String typeName = descriptor1.typeNames[index];
//										MappingsClass aclazz = flame.getFromSecondaryName(typeName);
//										if (aclazz != null) {
//											String className = aclazz.getPrimaryName();
//											if (className != null) descriptor1.returnType = className;
//										}
//									}
//									String typeName = descriptor1.returnType;
//									MappingsClass aclazz = flame.getFromSecondaryName(typeName);
//									if (aclazz != null) {
//										String className = aclazz.getPrimaryName();
//										if (className != null) descriptor1.returnType = className;
//									}
//									mappedDesc = descriptor1.toString();
//								}
//								for (MappingsMethod method : clazzFlame.getMethods()) {
//									if (method.getPrimary().equals(methodName)) {
//										if (method.getDesc().equals(descriptor) || method.getDesc().equals(mappedDesc)) {
//											inter = method.getSecondary();
//											break;
//										}
//									}
//								}
//							}
//							if (stopAtInter) return inter;
//							{
//								String mappedDesc;
//								{
//									Descriptor descriptor1 = ASMApplicator.parseDescriptor(descriptor);
//									for (int index = 0; index < descriptor1.typeNames.length; index++) {
//										String typeName = descriptor1.typeNames[index];
//										MappingsClass aclazz = intermediary.getFromSecondaryName(typeName);
//										if (aclazz != null) {
//											String className = aclazz.getPrimaryName();
//											if (className != null) descriptor1.returnType = className;
//										}
//									}
//									String typeName = descriptor1.returnType;
//									MappingsClass aclazz = intermediary.getFromSecondaryName(typeName);
//									if (aclazz != null) {
//										String className = aclazz.getPrimaryName();
//										if (className != null) descriptor1.returnType = className;
//									}
//									mappedDesc = descriptor1.toString();
//								}
//								MappingsClass interClass = intermediary.getFromPrimaryName(interName);
//								if (interClass == null) return null;
//								for (MappingsMethod method : interClass.getMethods()) {
//									if (method.getPrimary().equals(inter)) {
//										if (method.getDesc().equals(descriptor) || method.getDesc().equals(mappedDesc)) {
//											inter = method.getSecondary();
//											break;
//										}
//									}
//								}
//							}
//							return inter;
//						}
//						//TODO: make this work for when there are multiple methods with the same name
//						MappingsMethod m = clazzFlame.getMethodPrimary(methodName);
//						if (m == null) return null;
//						String interMethodName = m.getSecondary();
//						if (stopAtInter) return interMethodName;
//						MappingsClass interClass = intermediary.getFromPrimaryName(interName);
//						if (interClass == null) return null;
//						m = interClass.getMethodPrimary(interMethodName);
//						if (m == null) return null;
//						return m.getSecondary();
//					};
//					System.out.println(MappingApplicator.methodMapper.apply("net/minecraft/resource/ResourceName", "path()Ljava/lang/String;"));
//					MappingApplicator.fieldMapper = (name, fieldName) -> {
//						MappingsClass clazzFlame = flame.getFromSecondaryName(name);
//						if (clazzFlame == null) return null;
//						String interName = clazzFlame.getPrimaryName();
//						MappingsField selected = null;
//						for (MappingsField field : clazzFlame.getFields())
//							if (field.getPrimary().equals(fieldName)) {
//								selected = field;
//								break;
//							}
//						if (selected == null) return null;
//						String interMethodName = selected.getSecondary();
//						if (stopAtInter) return interMethodName;
//						MappingsClass interClass = intermediary.getFromPrimaryName(interName);
//						if (interClass == null) return null;
//						for (MappingsField field : interClass.getFields())
//							if (field.getPrimary().equals(interMethodName)) {
//								selected = field;
//								break;
//							}
//						if (selected == null) return null;
//						return selected.getSecondary();
//					};
				ASMApplicator.jarGetter = (className)->{
					String jarEntry = className.replace(".","/") + ".class";
					return FlameLauncher.getJarForEntry(jarEntry);
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
						for (String entry : new CSVReader(stream1.toString()).entries) {
							if (firstEntry == null) firstEntry = entry;
							else Class.forName(firstEntry + "." + entry);
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
			} catch (Throwable ex) {
				ex.printStackTrace();
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