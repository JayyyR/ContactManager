package com.example.android.contactmanager;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class ContactEntity extends GenericJson {

	@Key
	private String name;

	public ContactEntity(){}

	public static class phone extends GenericJson {     

		@Key
		private String phone;

		@Key
		private String type;

		public phone(){}
	}

	public static class email extends GenericJson {     

		@Key
		public String email;

		@Key
		public String type;

		public email(){}
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}

}
