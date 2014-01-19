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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ContactImporter extends Activity {

	public static final String TAG = "ContactImporter";

	private String jsonTextFinal;
	private Activity activity = this;
	private String user;
	private Client mKinveyClient;
	private EditText mURLEditText;
	ProgressDialog originalProgDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_importer);

		mURLEditText = (EditText) findViewById(R.id.urlEditText);

		user = getIntent().getExtras().getString("user");
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

		Log.v(TAG, user);
		//on button click make the call
		Button importButton = (Button) findViewById(R.id.importButton);
		importButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String urlString = mURLEditText.getText().toString();
				Log.v(TAG, "url string is: " + urlString);
				JSONReader jsonr = new JSONReader(urlString);
				jsonr.execute();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_importer, menu);
		return true;
	}

	//Create contact entities from jsonString
	private void createContacts(){
		Gson gson = new Gson();
		Type collectionType = new TypeToken<ArrayList<ContactEntity>>(){}.getType();
		try{
			ArrayList<ContactEntity> contactsFromURL = gson.fromJson(jsonTextFinal, collectionType);
			AsyncAppData<ContactEntity> contacts = mKinveyClient.appData("contacts", ContactEntity.class);

			final int numOfContacts = contactsFromURL.size();
			int contactCount = 0;
			Log.v(TAG, "num of contacts was : " + numOfContacts);
			for (ContactEntity contact : contactsFromURL){
				contact.put("account", user);	//attach the calling account to this contact

				//save each contact to Kinvey
				contactCount++;				//keep track of contact number so when all have imported, we can get rid of dialog
				final int conCount = contactCount;
				contacts.save(contact, new KinveyClientCallback<ContactEntity>() {
					@Override
					public void onFailure(Throwable e) {
						Log.e(TAG, "failed to save contact data", e);
						Log.v(TAG, "Contact count is: " + conCount);
						if (conCount >= numOfContacts){		// when contact count equals the number of contacts, dismiss our dialog and go back
							originalProgDialog.dismiss();
							goBackToContactManager();
						}
					}
					@Override
					public void onSuccess(ContactEntity r) {
						Log.d(TAG, "saved data for entity "+ r.getName());
						Log.v(TAG, "Contact count is: " + conCount);
						if (conCount >= numOfContacts){		// when contact count equals the number of contacts, dismiss our dialog and go back
							originalProgDialog.dismiss();
							goBackToContactManager();
						}
					}
				});


			}
		}
		catch(JsonSyntaxException e){
			e.printStackTrace();
			Toast.makeText(activity, "Please enter valid URL", Toast.LENGTH_SHORT).show();
		}
		catch(NullPointerException e){
			e.printStackTrace();
			Toast.makeText(activity, "Please enter valid URL", Toast.LENGTH_SHORT).show();
		}


	}

	/*private class to grab json array from url*/
	private class JSONReader extends AsyncTask<String, Void, String>{

		String url;


		public JSONReader(String url){
			this.url = url;
		};

		@Override
		protected void onPreExecute(){
			originalProgDialog= ProgressDialog.show(activity, "Importing Contacts","Please Wait", true);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				readJsonFromUrl(url);
			} catch (JSONException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} 
			return null;
		}


		@Override
		protected void onPostExecute(String result){
			super.onPostExecute(result);

			//after it's done create contact entities
			createContacts();
		}

		//helper method to read json content from url, stores json in global string
		public void readJsonFromUrl(String url) throws IOException, JSONException {
			InputStream is = new URL(url).openStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				StringBuilder sb = new StringBuilder();
				int cp;
				while ((cp = rd.read()) != -1) {
					sb.append((char) cp);
				}
				jsonTextFinal = sb.toString();	//store the json as a string
				Log.v(TAG, jsonTextFinal);

			} finally {
				is.close();
			}
		}


	}

	//method that will refresh contact list
	protected void goBackToContactManager(){
		Intent i = new Intent(this, ContactManager.class);
		i.putExtra("user", user);
		startActivity(i);
	}

}
