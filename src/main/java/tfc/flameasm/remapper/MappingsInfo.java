package tfc.flameasm.remapper;

import tfc.mappings.structure.MappingsHolder;

public class MappingsInfo {
	public final MappingsHolder mappings;
	public final String builtOn;
	public final String name;
	
	public MappingsInfo(MappingsHolder mappings, String builtOn, String name) {
		this.mappings = mappings;
		this.builtOn = builtOn;
		this.name = name;
	}
}
