package tfc.flameasm;

public class Descriptor {
	public String[] typeNames;
	public String returnType;
	
	public Descriptor(String[] names, String returnType) {
		this.typeNames = names;
		this.returnType = returnType;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder("(");
		for (String name : typeNames) builder.append(name);
		builder.append(")").append(returnType);
		return builder.toString();
	}
}
