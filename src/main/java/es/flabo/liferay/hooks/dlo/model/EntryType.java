package es.flabo.liferay.hooks.dlo.model;

public enum EntryType{	
	JPG("image/jpeg"),PNG("image/png"),GIF("image/gif"),UNKNOW("unknow");
	
	private String value;
	
	EntryType(String val){
		this.value=val;
	}
	
	public String getValue(){
		return this.value;
	}
		
}
