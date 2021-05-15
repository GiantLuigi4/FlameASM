package tfc.flameasm.hookins;

import com.tfc.bytecode.asm.ASM.ASM;
import com.tfc.bytecode.utils.Access;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tfc.flameasm.hookins.utils.InsertHolder;

import java.util.ArrayList;
import java.util.HashMap;

public class HookinApplicator {
	protected static HashMap<String, ArrayList<InsertHolder>> insertMap = new HashMap<>();
	
	// TODO: fix this
	public static byte[] apply(String name, byte[] bytes) {
		if (!insertMap.containsKey(name)) return bytes;
		ArrayList<InsertHolder> holders = insertMap.get(name);
		ASM asm = new ASM(bytes);
		for (InsertHolder holder : holders) {
			InsnList list = new InsnList();
			list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, name.replace(".","/"), holder.callName, holder.callDesc));
			asm.transformMethod(holder.methodName, holder.methodDescriptor, list, holder.point.equals("TOP"));
//			for (AbstractInsnNode instruction : holder.method.instructions) {
//				if (instruction instanceof MethodInsnNode) {
//					MethodInsnNode insn = ((MethodInsnNode) instruction);
//					if (insn.owner.equals(holder.hookinClassName.replace(".","/"))) insn.owner = name.replace(".","/");
//				}
//			}
//			asm.transformMethod(holder.methodName, holder.methodDescriptor, holder.method.instructions, holder.point.equals("TOP"));
		}
		bytes = asm.toBytes();
		
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		HashMap<String, MethodNode> methods = new HashMap<>();
		for (MethodNode method : node.methods) {
			methods.put(method.name + method.desc, method);
//			MethodNode node1 = methods.get(holder.methodName + holder.methodDescriptor);
		}
		for (InsertHolder holder : holders) {
			String access = Access.parseAccess(methods.get(holder.methodName + holder.methodDescriptor).access);
			MethodNode methodNode = new MethodNode();
			methodNode.name = holder.callName;
			methodNode.desc = holder.callDesc;
			if (access.contains("static")) {
				access = Access.parseAccess(holder.access);
				if (!access.contains("static")) access = access + " static";
				methodNode.access = Access.parseAccess(access);
			} else {
				methodNode.access = holder.access;
			}
			methodNode.instructions = holder.method.instructions;
			for (AbstractInsnNode instruction : methodNode.instructions) {
				if (instruction instanceof MethodInsnNode) {
					MethodInsnNode insn = ((MethodInsnNode) instruction);
					if (insn.owner.equals(holder.hookinClassName.replace(".","/")))
						insn.owner = name.replace(".","/");
				}
			}
			node.methods.add(methodNode);
		}
		
		ClassWriter result = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(result);
		return result.toByteArray();
	}
}
