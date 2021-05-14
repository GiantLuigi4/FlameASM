package tfc.flameasm.remapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import tfc.flameasm.Descriptor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static tfc.flameasm.ASMApplicator.writeBytes;

public class MappingApplicator {
	public static Function<String, String> classMapper;
	public static BiFunction<String, String, String> methodMapper;
	public static BiFunction<String, String, String> fieldMapper;
	public static Function<String, String> classToMappingInfoFunc;
	
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
		if (classToMappingInfoFunc == null) return bytes;
		if (classMapper == null) return bytes;
		String mappingInfo = classToMappingInfoFunc.apply(name); // TODO: handling with this
		if (mappingInfo == null) return bytes;
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		StringBuilder classDescriptor = new StringBuilder();
		classDescriptor.append("Class: ").append(name).append("\n");
		boolean wasRemapped = false;
		for (MethodNode method : node.methods) {
			classDescriptor.append(" ").append(method.name).append(":").append("\n");
			{
				Descriptor desc = parseDescriptor(method.desc);
				for (int index = 0; index < desc.typeNames.length; index++) {
					String typeName = desc.typeNames[index];
					if (typeName.startsWith("L") && typeName.endsWith(";")) {
						typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1));
						if (typeName != null) {
							desc.typeNames[index] = "L" + typeName + ";";
							wasRemapped = true;
						}
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
							clazz = classMapper.apply(clazz.substring(1, clazz.length() - 1));
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
						clazz = classMapper.apply(insn.desc.substring(1, insn.desc.length() - 1));
						if (clazz != null) {
							insn.desc = "L" + clazz + ";";
							wasRemapped = true;
						}
					}
					
					clazz = fieldMapper.apply(insn.owner, insn.name);
					if (clazz != null) {
						insn.name = clazz;
						wasRemapped = true;
					}
					
					clazz = classMapper.apply(insn.owner);
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
					
					clazz = methodMapper.apply(insn.owner, insn.name);
					if (clazz != null) {
						insn.name = clazz;
						wasRemapped = true;
					}
					
					{
						Descriptor desc = parseDescriptor(insn.desc);
						for (int index = 0; index < desc.typeNames.length; index++) {
							String typeName = desc.typeNames[index];
							if (typeName.startsWith("L") && typeName.endsWith(";")) {
								typeName = classMapper.apply(typeName.substring(1, typeName.length() - 1));
								if (typeName != null) {
									desc.typeNames[index] = "L" + typeName + ";";
									wasRemapped = true;
								}
							}
						}
						insn.desc = desc.toString();
					}
					
					clazz = classMapper.apply(insn.owner);
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
					clazz = classMapper.apply(insn.desc);
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
				}
				else {
					System.out.println(opcodeToName.get(instruction.getOpcode()));
					System.out.println(instruction.getClass());
				}
			}
		}
		if (!wasRemapped) return bytes;
		writeBytes(new File("flameasm/remap/" + (name.replace(".", "/") + ".properties")), classDescriptor.toString().getBytes());
		ClassWriter result = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(result);
		return result.toByteArray();
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
