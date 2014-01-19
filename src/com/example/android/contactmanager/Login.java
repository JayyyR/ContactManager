package com.example.android.contactmanager;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	
	private Client mKinveyClient;
	private EditText usernameField;
	private EditText passwordField;
	private Button loginButton;
	private Button registerButton;
	private String username;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		

		//Connect to Kinvey Backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
		

		//check if user is logged in and if they are, go to contact manager page
		if (mKinveyClient.user().isUserLoggedIn()){
			
			//retrieve updated info for user
			mKinveyClient.user().retrieve(new KinveyUserCallback() {
		        @Override
		        public void onFailure(Throwable e) {
		        	
		        }
		        @Override
		        public void onSuccess(User user) { 
		        	username = user.getUsername();
					launchContactManager();
		        	
		        }
		    });
			
		}
		
		//grab UI elements
		usernameField = (EditText) findViewById(R.id.usernameEditText);
		passwordField = (EditText) findViewById(R.id.passwordEditText);
		loginButton = (Button) findViewById(R.id.loginButton);
		registerButton = (Button) findViewById(R.id.registerButton);
		
		
		
		//set login button
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				username = usernameField.getText().toString();
				String password = passwordField.getText().toString();
				mKinveyClient.user().login(username, password, new KinveyUserCallback() {
					public void onFailure(Throwable t) {
						CharSequence text = "Wrong username or password.";
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
						
					}
					public void onSuccess(User u) {
						CharSequence text = "Welcome back," + u.getUsername() + ".";
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
						launchContactManager();
					}
				});
				
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	protected void launchContactManager(){
		Intent i = new Intent(this, ContactManager.class);
		i.putExtra("user", username);
		startActivity(i);
	}

}
