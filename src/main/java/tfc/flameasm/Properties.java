package tfc.flameasm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;

public class Properties {
	private final HashMap<String, String> properties = new HashMap<>();
	
	public Properties(File toParse) throws IOException {
		this(new String(Files.readAllBytes(toParse.toPath())));
	}
	
	public Properties(String toParse) {
		String[] properties = toParse.split("\n");
		for (String s : properties) {
			if (s.contains(":")) {
				String[] split = s.split(":", 2);
				this.properties.put(split[0].trim(), split[1].trim());
			}
			if (s.contains("=")) {
				String[] split = s.split("=", 2);
				this.properties.put(split[0].trim(), split[1].trim());
			}
		}
	}
	
	public static Properties parse(Object object) {
		Properties properties = new Properties("");
		Field[] fields = object.getClass().getFields();
		for (Field f : fields) {
			try {
				properties.addValue(f.getName(), f.get(object).toString());
			} catch (Throwable ignored) {
			}
		}
		return properties;
	}
	
	public static Properties parse(Object object, String file) {
		try {
			Properties properties = new Properties(new File(file));
			Field[] fields = object.getClass().getFields();
			for (Field f : fields) {
				try {
					properties.addValue(f.getName(), f.get(object).toString());
					Object val = properties.getValue(f.getName());
					if (f.getType().equals(int.class)) val = Integer.valueOf((String) val);
					else if (f.getType().equals(boolean.class)) val = Boolean.valueOf((String) val);
					else if (f.getType().equals(double.class)) val = Double.valueOf((String) val);
					else if (f.getType().equals(float.class)) val = Float.valueOf((String) val);
					else if (f.getType().equals(long.class)) val = Long.valueOf((String) val);
					else if (f.getType().equals(byte.class)) val = Byte.valueOf((String) val);
					else if (f.getType().equals(short.class)) val = Short.valueOf((String) val);
					f.set(object, val);
				} catch (Throwable ignored) {
				}
			}
			return properties;
		} catch (Throwable ignored) {
			return parse(object);
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		properties.forEach((name, val) -> builder.append(name).append(":").append(val).append("\n"));
		return builder.toString();
	}
	
	public String getValue(String property) {
		if (properties.containsKey(property))
			return properties.get(property);
		return null;
	}
	
	public <A extends java.io.Serializable> A getValue(String property, Class<A> aClass) {
		try {
			return (A) aClass.getMethod("valueOf").invoke(null, properties.get(property));
		} catch (Throwable ignored) {
			return null;
		}
	}
	
	public void setValue(String key, String value) {
		if (properties.containsKey(key)) properties.replace(key, value);
		else properties.put(key, value);
	}
	
	public void addValue(String key, String value) {
		if (!properties.containsKey(key)) properties.put(key, value);
	}
	
	public Set<String> getEntries() {
		return properties.keySet();
	}
}
