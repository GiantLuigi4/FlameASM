package tfc.flameasm.remapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import tfc.flameasm.ASMApplicator;
import tfc.flameasm.Descriptor;
import tfc.flamemc.FlameLauncher;
import tfc.mappings.structure.MappingsClass;
import tfc.mappings.structure.MappingsHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static tfc.flameasm.ASMApplicator.parseDescriptor;
import static tfc.flameasm.ASMApplicator.writeBytes;

public class MappingApplicator {
	public static BiFunction<String, MappingsSteps, String> classMapper;
	public static BiFunction<String, MappingsSteps, String> methodMapper;
	public static BiFunction<String, MappingsSteps, String> fieldMapper;
	
	public static String targetMappings = "OBFUSCATION";
	private static final HashMap<String, MappingsInfo> mappingsSystems = new HashMap<>();
	
	public static void registerMappings(MappingsInfo info) {
		mappingsSystems.put(info.name, info);
	}
	
	public static MappingsInfo getInfo(String mappings) {
		return mappingsSystems.get(mappings);
	}
	
	public static MappingsSteps getSteps(String src, String targ) {
		MappingsSteps stepsO = new MappingsSteps();
		ArrayList<String> steps = stepsO.steps;
		MappingsInfo next = getInfo(src);
		steps.add(next.name);
		while (!next.builtOn.equals("OBFUSCATION")) {
			steps.add(next.builtOn);
			next = getInfo(next.builtOn);
		}
		steps.add(next.builtOn);
		if (targetMappings.equals("OBFUSCATION")) {
			return stepsO;
		}
		ArrayList<MappingsInfo> infos = new ArrayList<>();
		next = getInfo(targ);
		while (!next.builtOn.equals("OBFUSCATION")) {
			infos.add(next);
			next = getInfo(next.name);
		}
		for (int index = infos.size() - 1; index >= 0; index--) {
			MappingsInfo info = infos.get(index);
			steps.add(info.builtOn);
		}
		steps.add(targ);
		return stepsO;
	}
	
	static HashMap<Integer, String> opcodeToName = new HashMap<>();
	
	static {
		for (Field field : Opcodes.class.getFields()) {
			if (field.toString().startsWith("public static")) {
				try {
					int id = (int)field.get(null);
					opcodeToName.put(id, field.getName());
				} catch (Throwable ignored) {
				}
			}
		}
	}
	
	public static byte[] apply(String name, byte[] bytes) {
		if (classMapper == null) return bytes;
		String mappingInfo; // TODO: handling with this
		{
			File jar = ASMApplicator.jarGetter.apply(name);
			if (jar == null) return bytes;
			try {
				byte[] bytes1 = FlameLauncher.getSourceFile(jar, "mapping_info.properties");
				if (bytes == null) return null;
				mappingInfo = new String(bytes1);
			} catch (NullPointerException ignored) {
				return bytes;
			}
		}
		if (mappingInfo == null) return bytes; // TODO: remove this once the purpose of mappingInfo is implemented
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		StringBuilder classDescriptor = new StringBuilder();
		boolean wasRemapped = false;
		MappingsSteps steps = getSteps("FLAME", targetMappings);
		{
			ArrayList<String> interfaces = new ArrayList<>();
			for (String typeName : node.interfaces) {
				typeName = classMapper.apply(typeName, steps);
				if (typeName != null) {
					interfaces.add(typeName);
					wasRemapped = true;
				}
			}
			node.interfaces = interfaces;
		}
		{
			String typeName = node.superName;
			typeName = classMapper.apply(typeName, steps);
			if (typeName != null) {
				node.superName = typeName;
				wasRemapped = true;
			}
		}
		classDescriptor.append("Class: ").append(name).append("\n");
		for (MethodNode method : node.methods) {
			classDescriptor.append(" ").append(method.name).append(":").append("\n");
			{
				Descriptor desc = parseDescriptor(method.desc);
				for (int index = 0; index < desc.typeNames.length; index++) {
					String typeName = desc.typeNames[index];
					if (typeName.startsWith("L") && typeName.endsWith(";")) {
						typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1), steps);
						if (typeName != null) {
							desc.typeNames[index] = "L" + typeName + ";";
							wasRemapped = true;
						}
					}
				}
				String typeName = desc.returnType;
				if (typeName.startsWith("L") && typeName.endsWith(";")) {
					typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1), steps);
					if (typeName != null) {
						desc.returnType = "L" + typeName + ";";
						wasRemapped = true;
					}
				}
				method.desc = desc.toString();
			}
			classDescriptor.append("  desc: ").append(method.desc).append("\n");
			classDescriptor.append("  instructions:").append("\n");
			int num = 0;
			for (AbstractInsnNode instruction : method.instructions) {
				classDescriptor.append("   instruction").append(num++).append(": ").append(opcodeToName.get(instruction.getOpcode())).append("\n");
				if (instruction instanceof InsnNode) {
					InsnNode insn = (InsnNode) instruction;
				} else if (instruction instanceof VarInsnNode) {
					VarInsnNode insn = (VarInsnNode) instruction;
					classDescriptor.append("    var: ").append(insn.var).append("\n");
				} else if (instruction instanceof LdcInsnNode) {
					LdcInsnNode insn = (LdcInsnNode) instruction;
					if (insn.cst instanceof Type) {
						String clazz = ((Type) insn.cst).getDescriptor();
						if (clazz.startsWith("L") && clazz.endsWith(";")) {
							clazz = classMapper.apply(clazz.substring(1, clazz.length() - 1), steps);
							if (clazz != null) {
								clazz = "L" + clazz + ";";
								insn.cst = Type.getType(clazz);
								wasRemapped = true;
							}
						}
					}
					classDescriptor.append("    cst: ").append(insn.cst).append("\n");
				} else if (instruction instanceof FieldInsnNode) {
					FieldInsnNode insn = ((FieldInsnNode)instruction);
					String clazz;
					if (insn.desc.startsWith("L") && insn.desc.endsWith(";")) {
						clazz = classMapper.apply(insn.desc.substring(1, insn.desc.length() - 1), steps);
						if (clazz != null) {
							insn.desc = "L" + clazz + ";";
							wasRemapped = true;
						}
					}
					
					clazz = fieldMapper.apply(insn.owner + ";" + insn.name, steps);
					if (clazz != null) {
						insn.name = clazz;
						wasRemapped = true;
					}
					
					clazz = classMapper.apply(insn.owner, steps);
					if (clazz != null) {
						insn.owner = clazz;
						wasRemapped = true;
					}
					
					classDescriptor.append("    desc: ").append(insn.desc).append("\n");
					classDescriptor.append("    name: ").append(insn.name).append("\n");
					classDescriptor.append("    owner: ").append(insn.owner).append("\n");
				} else if (instruction instanceof MethodInsnNode) {
					MethodInsnNode insn = (MethodInsnNode) instruction;
					String clazz;
					
					clazz = methodMapper.apply(insn.owner + ";" + insn.name + insn.desc, steps);
					if (clazz != null) {
						insn.name = clazz;
						wasRemapped = true;
					}
					
					{
						Descriptor desc = parseDescriptor(insn.desc);
						for (int index = 0; index < desc.typeNames.length; index++) {
							String typeName = desc.typeNames[index];
							if (typeName.startsWith("L") && typeName.endsWith(";")) {
								typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1), steps);
								if (typeName != null) {
									desc.typeNames[index] = "L" + typeName + ";";
									wasRemapped = true;
								}
							}
						}
						String typeName = desc.returnType;
						if (typeName.startsWith("L") && typeName.endsWith(";")) {
							typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1), steps);
							if (typeName != null) {
								desc.returnType = "L" + typeName + ";";
								wasRemapped = true;
							}
						}
						insn.desc = desc.toString();
					}
					
					clazz = classMapper.apply(insn.owner, steps);
					if (clazz != null) {
						insn.owner = clazz;
						wasRemapped = true;
					}
					
					classDescriptor.append("    desc: ").append(insn.desc).append("\n");
					classDescriptor.append("    name: ").append(insn.name).append("\n");
					classDescriptor.append("    owner: ").append(insn.owner).append("\n");
				} else if (instruction instanceof TypeInsnNode) {
					TypeInsnNode insn = (TypeInsnNode)instruction;
					
					String clazz;
					clazz = classMapper.apply(insn.desc, steps);
					if (clazz != null) {
						insn.desc = clazz;
						wasRemapped = true;
					}
					
					classDescriptor.append("    desc: ").append(insn.desc).append("\n");
				} else if (instruction instanceof IincInsnNode) {
					IincInsnNode insn = (IincInsnNode)instruction;
					classDescriptor.append("    node: intIncrementNode\n");
					classDescriptor.append("    var: ").append(insn.var).append("\n");
					classDescriptor.append("    incr: ").append(insn.incr).append("\n");
				} else if (instruction instanceof LabelNode) {
					LabelNode insn = (LabelNode)instruction;
					classDescriptor.append("    node: labelNode\n");
					classDescriptor.append("    info: ").append(insn.getLabel().info).append("\n");
				} else if (instruction instanceof LineNumberNode) {
					LineNumberNode insn = (LineNumberNode)instruction;
					classDescriptor.append("    node: lineNode\n");
					classDescriptor.append("    line: ").append(insn.line).append("\n");
				} else if (instruction instanceof IntInsnNode) {
					IntInsnNode insn = (IntInsnNode)instruction;
					classDescriptor.append("    operand: ").append(insn.operand).append("\n");
				} else {
					System.out.println(opcodeToName.get(instruction.getOpcode()));
					System.out.println(instruction.getClass());
				}
			}
		}
		for (FieldNode field : node.fields) {
			String typeName = field.desc;
			if (typeName.startsWith("L") && typeName.endsWith(";")) {
				typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1), steps);
				if (typeName != null) {
					field.desc = "L" + typeName + ";";
					wasRemapped = true;
				}
			}
		}
		if (!wasRemapped) return bytes;
		writeBytes(new File("flameasm/remap/" + (name.replace(".", "/") + ".properties")), classDescriptor.toString().getBytes());
		ClassWriter result = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(result);
		return result.toByteArray();
	}
	
	public static String mapDesc(String desc, MappingsHolder holder) {
		Descriptor descr = parseDescriptor(desc);
		for (int index = 0; index < descr.typeNames.length; index++) {
			String typeName = descr.typeNames[index];
			boolean isNonPrimitive = false;
			if (typeName.startsWith("L") && typeName.endsWith(";")) {
				typeName = typeName.substring(1, typeName.length() - 1);
				isNonPrimitive = true;
			}
			MappingsClass clazz = holder.getFromPrimaryName(typeName);
			if (clazz != null) typeName = "L" + clazz.getSecondaryName() + ";";
			else {
				clazz = holder.getFromSecondaryName(typeName);
				if (clazz != null) typeName = "L" + clazz.getPrimaryName() + ";";
				else if (isNonPrimitive){
					typeName = "L" + typeName + ";";
				}
			}
			descr.typeNames[index] = typeName;
		}
		String typeName = descr.returnType;
		boolean isNonPrimitive = false;
		if (typeName.startsWith("L") && typeName.endsWith(";")){
			typeName = typeName.substring(1, typeName.length() - 1);
			isNonPrimitive = true;
		}
		MappingsClass clazz = holder.getFromPrimaryName(typeName);
		if (clazz != null) typeName = "L" + clazz.getSecondaryName() + ";";
		else {
			clazz = holder.getFromSecondaryName(typeName);
			if (clazz != null) typeName = "L" + clazz.getPrimaryName() + ";";
			else if (isNonPrimitive) {
				typeName = "L" + typeName + ";";
			}
		}
		descr.returnType = typeName;
		return descr.toString();
	}
}
