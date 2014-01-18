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
import com.google.gson.reflect.TypeToken;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ContactImporter extends Activity {
	
	private String jsonTextFinal;
	Activity activity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_importer);
		
		
		//on button click make the call
		Button importButton = (Button) findViewById(R.id.importButton);
		importButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				JSONObject json;
				JSONReader jsonr = new JSONReader("https://raw2.github.com/Fetchnotes/ContactManager/super-secret-stuff/contacts.json");
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
		ArrayList<ContactEntity> ints2 = gson.fromJson(jsonTextFinal, collectionType);
		Log.v("entities", "" + ints2.size());
		for (ContactEntity x : ints2){
			Log.v("entities", ""+ x.getName());
		}
	}
	
	/*private class to grab json array from url*/
	private class JSONReader extends AsyncTask<String, Void, String>{

		String url;
		ProgressDialog progressDialog;
		
		public JSONReader(String url){
			this.url = url;
		};
		
		@Override
		protected void onPreExecute(){
			progressDialog= ProgressDialog.show(activity, "Importing Contacts","Please Wait", true);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				readJsonFromUrl(url);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return null;
		}
	 

		@Override
		protected void onPostExecute(String result){
			super.onPostExecute(result);
			progressDialog.dismiss();
			//after it's done create contact entities
			createContacts();
		}
		

		public void readJsonFromUrl(String url) throws IOException, JSONException {
			InputStream is = new URL(url).openStream();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				StringBuilder sb = new StringBuilder();
				int cp;
				while ((cp = rd.read()) != -1) {
					sb.append((char) cp);
				}
				jsonTextFinal = sb.toString();

			} finally {
				is.close();
			}
		}


	}

}
