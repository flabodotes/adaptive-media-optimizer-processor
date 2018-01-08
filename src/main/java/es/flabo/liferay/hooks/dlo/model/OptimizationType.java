package es.flabo.liferay.hooks.dlo.model;

public enum OptimizationType{
	LOOSY("loosy"),LOSSLESS("lossless");
	
	String value;
	OptimizationType(String val){
		this.value=val;
	}
	
	public String getValue(){
		return this.value;
	}
}