package tfc.flameasm.hookins.utils;

import com.tfc.bytecode.utils.Access;
import org.objectweb.asm.tree.MethodNode;

public class InsertHolder {
	public String methodName = null;
	public String methodDescriptor = null;
	public String callName = null;
	public String callDesc = null;
	public String hookinClassName = null;
	public int access = -1999;
	public MethodNode method = null;
	public String point = "TOP";
	public String mappings = "SAME_AS_HOOKIN";
	
	public String getAccess() {
		return Access.parseAccess(access);
	}
}
