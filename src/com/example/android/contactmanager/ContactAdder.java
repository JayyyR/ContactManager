/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contactmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Iterator;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

public final class ContactAdder extends Activity
{
	private String user;
	private Client mKinveyClient;
	public static final String TAG = "ContactsAdder";
	public static final String ACCOUNT_NAME =
			"com.example.android.contactmanager.ContactsAdder.ACCOUNT_NAME";
	public static final String ACCOUNT_TYPE =
			"com.example.android.contactmanager.ContactsAdder.ACCOUNT_TYPE";

	private EditText mContactEmailEditText;
	private ArrayList<Integer> mContactEmailTypes;
	private Spinner mContactEmailTypeSpinner;
	private EditText mContactNameEditText;
	private EditText mContactPhoneEditText;
	private ArrayList<Integer> mContactPhoneTypes;
	private Spinner mContactPhoneTypeSpinner;
	private Button mContactSaveButton;

	/**
	 * Called when the activity is first created. Responsible for initializing the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_adder);
		
		//get active user
		user = getIntent().getExtras().getString("user");
		//connect with kinvey backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

		// Obtain handles to UI objects
		mContactNameEditText = (EditText) findViewById(R.id.contactNameEditText);
		mContactPhoneEditText = (EditText) findViewById(R.id.contactPhoneEditText);
		mContactEmailEditText = (EditText) findViewById(R.id.contactEmailEditText);
		mContactPhoneTypeSpinner = (Spinner) findViewById(R.id.contactPhoneTypeSpinner);
		mContactEmailTypeSpinner = (Spinner) findViewById(R.id.contactEmailTypeSpinner);
		mContactSaveButton = (Button) findViewById(R.id.contactSaveButton);

		// Prepare list of supported account types
		// Note: Other types are available in ContactsContract.CommonDataKinds
		//       Also, be aware that type IDs differ between Phone and Email, and MUST be computed
		//       separately.
		mContactPhoneTypes = new ArrayList<Integer>();
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
		mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
		mContactEmailTypes = new ArrayList<Integer>();
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_HOME);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_WORK);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE);
		mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_OTHER);

		// Populate list of account types for phone
		ArrayAdapter<String> adapter;
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Iterator<Integer> iter;
		iter = mContactPhoneTypes.iterator();
		while (iter.hasNext()) {
			adapter.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(
					this.getResources(),
					iter.next(),
					getString(R.string.undefinedTypeLabel)).toString());
		}
		mContactPhoneTypeSpinner.setAdapter(adapter);
		mContactPhoneTypeSpinner.setPrompt(getString(R.string.selectLabel));

		// Populate list of account types for email
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		iter = mContactEmailTypes.iterator();
		while (iter.hasNext()) {
			adapter.add(ContactsContract.CommonDataKinds.Email.getTypeLabel(
					this.getResources(),
					iter.next(),
					getString(R.string.undefinedTypeLabel)).toString());
		}
		mContactEmailTypeSpinner.setAdapter(adapter);
		mContactEmailTypeSpinner.setPrompt(getString(R.string.selectLabel));

		// Register handlers for UI elements
		mContactSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSaveButtonClicked();
			}
		});
	}

	/**
	 * Actions for when the Save button is clicked. Creates a contact entry and terminates the
	 * activity.
	 */
	private void onSaveButtonClicked() {
		Log.v(TAG, "Save button clicked");
		createContactEntry();
	}

	/**
	 * Creates a contact entry from the current UI values in the account named by mSelectedAccount.
	 */
	protected void createContactEntry() {
		// Get values from UI
		String name = mContactNameEditText.getText().toString();
		String phone = mContactPhoneEditText.getText().toString();
		String email = mContactEmailEditText.getText().toString();
		int phoneType = mContactPhoneTypes.get(
				mContactPhoneTypeSpinner.getSelectedItemPosition());
		int emailType = mContactEmailTypes.get(
				mContactEmailTypeSpinner.getSelectedItemPosition());

		//create a contact entity object to save
		ContactEntity contact = new ContactEntity();
		
		//set attributes for the contact
		contact.set("name", name);
		contact.put("account", user);
		
		//create phone entity to put in contact
		ContactEntity.phone phoneEntity = new ContactEntity.phone();
		phoneEntity.set("phone", phone);
		switch(phoneType) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			phoneEntity.set("type", "home");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			phoneEntity.set("type", "mobile");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			phoneEntity.set("type", "work");
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
			phoneEntity.set("type", "other");
			break;
		}
		
		//create email entity to put in contact
		ContactEntity.email emailEntity = new ContactEntity.email();
		emailEntity.set("email", email);
		switch(emailType) {
		case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
			emailEntity.set("type", "home");
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
			emailEntity.set("type", "mobile");
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
			emailEntity.set("type", "work");
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
			emailEntity.set("type", "other");
			break;
		}
		
		//add entities to arraylists to account for how information should be stored in the backend (in a list)
		ArrayList<ContactEntity.phone> phones = new ArrayList<ContactEntity.phone>();
		ArrayList<ContactEntity.email> emails = new ArrayList<ContactEntity.email>();
		phones.add(phoneEntity);
		emails.add(emailEntity);
		
		//set the phone and emails to the contact entity object
		contact.set("phone", phones);
		contact.set("email", emails);

		//save the contact
		AsyncAppData<ContactEntity> contacts = mKinveyClient.appData("contacts", ContactEntity.class);
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Adding Contact", "just a moment");
		contacts.save(contact, new KinveyClientCallback<ContactEntity>() {
			@Override
			public void onFailure(Throwable e) {
				Log.e(TAG, "failed to save contact data", e); 
				progressDialog.dismiss();
				goBackToContactManager();
			}
			@Override
			public void onSuccess(ContactEntity r) {
				Log.d(TAG, "saved data for entity "+ r.getName());
				progressDialog.dismiss();
				goBackToContactManager();
			}
		});
	}
	
	//method that will refresh contact list
	protected void goBackToContactManager(){
		Intent i = new Intent(this, ContactManager.class);
		i.putExtra("user", user);
		startActivity(i);
	}
}