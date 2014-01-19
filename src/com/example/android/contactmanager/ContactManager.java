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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.google.api.client.util.ArrayMap;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;

public final class ContactManager extends Activity
{

	public static final String TAG = "ContactManager";

	private Button mAddAccountButton;
	private Button mImportContactsButton;
	private Button mLogoutButton;
	private ListView mContactList;
	private Client mKinveyClient;
	private String user;
	private Context context = this;


	/**
	 * Called when the activity is first created. Responsible for initializing the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);

		//grab user
		user = getIntent().getExtras().getString("user");

		//Connect to Kinvey Backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

		setContentView(R.layout.contact_manager);

		// Obtain handles to UI objects
		mAddAccountButton = (Button) findViewById(R.id.addContactButton);
		mImportContactsButton = (Button) findViewById(R.id.importURLButton);
		mContactList = (ListView) findViewById(R.id.contactList);
		mLogoutButton = (Button) findViewById(R.id.logoutButton);

		// Register handler for UI elements
		mAddAccountButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "mAddAccountButton clicked");
				launchContactAdder();
			}
		});
		mImportContactsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				launchImporter();

			}
		});
		mLogoutButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mKinveyClient.user().logout().execute();
				launchLogin();
			}
		});

		//grab and populate contacts
		getContacts();
	}

	/**
	 * Obtains the contact list for the currently selected account.
	 */
	private void getContacts()
	{

		//get contacts for user
		AsyncAppData<ContactEntity> contacts = mKinveyClient.appData("contacts", ContactEntity.class);
		Query userQuery = mKinveyClient.query();
		Log.v(TAG, "querying for: " + user);
		userQuery.equals("account", user);	//only get contacts associated with correct account
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Getting Contacts", "just a moment");
		contacts.get(userQuery, new KinveyListCallback<ContactEntity>()     {
			@Override
			public void onSuccess(ContactEntity[] result) { 
				progressDialog.dismiss();
				//on success, populate the listview
				populateContactList(result);
			}
			@Override
			public void onFailure(Throwable error)  { 
				progressDialog.dismiss();
				Log.e(TAG, "failed to fetch all", error);
			}
		});

	}


	/**
	 * Populate the contact list based on account currently signed in
	 */
	private void populateContactList(ContactEntity[] contactList) {
		// Build adapter with contact entries
		final ArrayList<ContactEntity> contactsSorted = new ArrayList<ContactEntity>(Arrays.asList(contactList));

		//sort contacts on name
		Collections.sort(contactsSorted, new CustomComparator());
		ArrayList<String> contactNames = new ArrayList<String>();
		for (ContactEntity x : contactsSorted){
			contactNames.add(x.getName());
		}
		
		//create simple adapter for displaying contacts
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, android.R.id.text1, contactNames);
		mContactList.setAdapter(adapter);
		mContactList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				//create popup dialog with contact info
				AlertDialog.Builder builder = new AlertDialog.Builder(context);

				//set title as contact's name
				builder.setTitle(contactsSorted.get(pos).getName());

				ListView contactData = new ListView(context);
				ArrayList<String> content = new ArrayList<String>();

				//add emails
				ArrayList<ArrayMap> emails =  (ArrayList<ArrayMap>) contactsSorted.get(pos).get("email");
				for (int i = 0; i <emails.size(); i++){
					content.add((String)emails.get(i).get("type") + " email: " + (String)emails.get(i).get("email"));
				}
				//add phone numbers
				ArrayList<ArrayMap> phones =  (ArrayList<ArrayMap>) contactsSorted.get(pos).get("phone");
				for (int i = 0; i <phones.size(); i++){
					content.add((String)phones.get(i).get("type") + " phone: " + (String)phones.get(i).get("phone"));
				}

				//set adapter for listview
				ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, content);
				contactData.setAdapter(modeAdapter);
				builder.setView(contactData);
				final Dialog dialog = builder.create();

				dialog.show();


			}
		});
	}



	/**
	 * Launches the ContactAdder activity to add a new contact to the selected account.
	 */
	protected void launchContactAdder() {
		Intent i = new Intent(this, ContactAdder.class);
		i.putExtra("user", user);
		startActivity(i);
	}

	/**
	 * Launches the Importer activity to import contacts to selected account.
	 */
	protected void launchImporter() {
		Intent i = new Intent(this, ContactImporter.class); 
		i.putExtra("user", user);
		startActivity(i);
	}

	/**
	 * Launches the Login activity.
	 */
	protected void launchLogin() {
		Intent i = new Intent(this, Login.class);
		startActivity(i);
	}


	//custom comparator for sorting contacts by name
	public class CustomComparator implements Comparator<ContactEntity> {
		@Override
		public int compare(ContactEntity o1, ContactEntity o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}



	//override back button so user can't back into login screen
	@Override
	public void onBackPressed() {
	}


}
