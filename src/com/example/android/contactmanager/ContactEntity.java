package com.example.android.contactmanager;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class ContactEntity extends GenericJson {

	@Key
	private String name;

	public ContactEntity(){}

	private static class phone extends GenericJson {     

		@Key
		private String phone;

		@Key
		private String type;

		public phone(){}
	}

	private static class email extends GenericJson {     

		@Key
		private String email;

		@Key
		private String type;

		public email(){}
	}
	
	public void setName(String name){
		name = name;
	}
	
	public String getName(){
		return name;
	}

}
