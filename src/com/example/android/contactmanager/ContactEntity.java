package com.example.android.contactmanager;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class ContactEntity extends GenericJson {

	@Key
	private String Name;

	public ContactEntity(){}

	private static class Phone extends GenericJson {     

		@Key
		private String Phone;

		@Key
		private String PhoneType;

		public Phone(){}
	}

	private static class Email extends GenericJson {     

		@Key
		private String Email;

		@Key
		private String EmailType;

		public Email(){}
	}
	
	public void setName(String name){
		Name = name;
	}
	
	public String getName(){
		return Name;
	}

}
