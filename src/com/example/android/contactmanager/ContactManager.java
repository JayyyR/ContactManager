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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

public final class ContactManager extends Activity
{

	public static final String TAG = "ContactManager";

	private Button mAddAccountButton;
	private Button mImportContactsButton;
	private ListView mContactList;
	private Client mKinveyClient;
	private String user = "test";


	/**
	 * Called when the activity is first created. Responsible for initializing the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);


		//Connect to Kinvey Backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();


		setContentView(R.layout.contact_manager);

		// Obtain handles to UI objects
		mAddAccountButton = (Button) findViewById(R.id.addContactButton);
		mImportContactsButton = (Button) findViewById(R.id.importURLButton);
		mContactList = (ListView) findViewById(R.id.contactList);


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



		mKinveyClient.user().logout().execute();
		//		//testing
		//		mKinveyClient.user().create("tested", "pass", new KinveyUserCallback() {
		//			public void onFailure(Throwable t) {
		//				CharSequence text = "Could not sign up.";
		//				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		//			}
		//			public void onSuccess(User u) {
		//				CharSequence text = u.getUsername() + ", your account has been created.";
		//				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		//			}
		//		});

		//Login with test account for now
		if (!mKinveyClient.user().isUserLoggedIn()){

			mKinveyClient.user().login(user, "pass", new KinveyUserCallback() {
				public void onFailure(Throwable t) {
					CharSequence text = "Wrong username or password.";
					Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
				public void onSuccess(User u) {
					CharSequence text = "Welcome back," + u.getUsername() + ".";
					Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				}
			});

		}




		// Populate the contact list
		// populateContactList();
		getContacts();



	}



	/**
	 * Populate the contact list based on account currently selected in the account spinner.
	 */
	private void populateContactList(ContactEntity[] contactList) {
		// Build adapter with contact entries
		ArrayList<String> contactNames = new ArrayList<String>();
		for (ContactEntity x : contactList){
			contactNames.add(x.getName());
		}
		Collections.sort(contactNames);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, android.R.id.text1, contactNames);
		mContactList.setAdapter(adapter);
	}

	/**
	 * Obtains the contact list for the currently selected account.
	 *
	 * @return A cursor for for accessing the contact list.
	 */
	private void getContacts()
	{
		
		//get contacts for user
		AsyncAppData<ContactEntity> contacts = mKinveyClient.appData("contacts", ContactEntity.class);
		Query myQuery = mKinveyClient.query();
		myQuery.equals("account", user);	//only get contacts associated with correct account
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Getting Contacts", "just a moment");
		contacts.get(myQuery, new KinveyListCallback<ContactEntity>()     {
			@Override
			public void onSuccess(ContactEntity[] result) { 
				progressDialog.dismiss();
				Log.v(TAG, "received "+ result.length + " events");
				for (ContactEntity i : result)
					Log.v(TAG, "received: " + i.getName());
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
	 * Launches the ContactAdder activity to add a new contact to the selected account.
	 */
	protected void launchContactAdder() {
		Intent i = new Intent(this, ContactAdder.class);
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


}
