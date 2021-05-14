package tfc.flameasm.hookins;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.CSVReader;
import tfc.flameasm.Descriptor;
import tfc.flameasm.annotations.hookin.Hookin;
import tfc.flameasm.hookins.utils.InsertHolder;
import tfc.flamemc.FlameLauncher;

import java.io.File;
import java.util.ArrayList;

import static tfc.flameasm.ASMApplicator.parseDescriptor;
import static tfc.flameasm.remapper.MappingApplicator.classMapper;

public class HookinReader {
	public static byte[] apply(String name, byte[] bytes) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		ArrayList<String> csv;
		{
			File jar = ASMApplicator.jarGetter.apply(name);
			if (jar == null) return bytes;
			try {
				byte[] bytes1 = FlameLauncher.getSourceFile(jar, "hookins.csv");
				if (bytes1 == null) return bytes;
				csv = new CSVReader(new String(bytes1)).entries;
			} catch (NullPointerException ignored) {
				return bytes;
			}
		}
		if (csv == null || csv.isEmpty()) return bytes;
		if (name.startsWith(csv.get(0)) && !csv.contains(name.replace(csv.get(0) + ".", "")))
			return bytes;
		
		ArrayList<InsertHolder> insertHolders;
		{
			if (node.visibleAnnotations == null) return bytes;
			String targ = "";
			String hookinAnnotationName = "L" + (Hookin.class.getTypeName().replace(".","/")) + ";";
			for (AnnotationNode visibleAnnotation : node.visibleAnnotations) {
				if (visibleAnnotation.desc.equals(hookinAnnotationName)) {
					String mappings = "SAME_AS_JAR";
					String target = "";
					for (Object value : visibleAnnotation.values) {
						if (value.equals("mappings")) mappings = null;
						else if (mappings == null) mappings = (String) value;
						else if (value.equals("target")) targ = null;
						else if (targ == null) targ = (String) value;
					}
				}
			}
			if (!HookinApplicator.insertMap.containsKey(targ)) {
				insertHolders = new ArrayList<>();
				HookinApplicator.insertMap.put(targ, insertHolders);
			} else insertHolders = HookinApplicator.insertMap.get(targ);
		}
		
		for (MethodNode method : node.methods) {
			if (method.name.equals("<init>")) continue; // TODO: initializer insert
			if (method.name.equals("<clinit>")) method.name = ("__FLAMEASM_CLINIT__" + name.replace(".", "_"));
			else method.name = ("__FLAMEASM_HOOKIN_METHOD__"
					+ name.replace(".", "_"))
					+ "$" + method.name
					+ "$" + method.desc.replace("(", "LP$").replace(")", "RP$").replace("[","_ARRAY$").replace(";","$ENDTYPENAME_")
					;
			if (method.visibleAnnotations == null) continue;
			InsertHolder holder = new InsertHolder();
			String field = null;
			String value = null;
			for (AnnotationNode visibleAnnotation : method.visibleAnnotations) {
				for (Object val : visibleAnnotation.values) {
					if (field == null) {
						field = (String) val;
					} else {
						value = (String) val;
						if (field.equals("target")) {
							holder.methodName = value.substring(0, value.indexOf("("));
							Descriptor desc = parseDescriptor(value.substring(value.indexOf("(")));
							for (int index = 0; index < desc.typeNames.length; index++) {
								String typeName = desc.typeNames[index];
								if (typeName.startsWith("L") && typeName.endsWith(";")) {
									typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1));
									if (typeName != null) {
										desc.typeNames[index] = "L" + typeName + ";";
									}
								}
							}
							String typeName = desc.returnType;
							if (typeName.startsWith("L") && typeName.endsWith(";")) {
								typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1));
								if (typeName != null) {
									desc.returnType = "L" + typeName + ";";
								}
							}
							holder.methodDescriptor = desc.toString();
							holder.callName = method.name;
							holder.callDesc = method.desc;
							holder.hookinClassName = name;
							holder.access = method.access;
						} else if (field.equals("point")) holder.point = value;
						else if (field.equals("mappings")) holder.mappings = value;
						holder.method = method;
						field = null;
						value = null;
					}
				}
			}
			insertHolders.add(holder);
		}
		ClassWriter result = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(result);
		return result.toByteArray();
	}
}
