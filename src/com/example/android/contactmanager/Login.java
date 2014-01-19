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
	private EditText mUsernameField;
	private EditText mPasswordField;
	private Button mLoginButton;
	private Button mRegisterButton;
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
		mUsernameField = (EditText) findViewById(R.id.usernameEditText);
		mPasswordField = (EditText) findViewById(R.id.passwordEditText);
		mLoginButton = (Button) findViewById(R.id.loginButton);
		mRegisterButton = (Button) findViewById(R.id.registerButton);
		
		
		
		//set login button
		mLoginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				username = mUsernameField.getText().toString();
				String password = mPasswordField.getText().toString();
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
		
		//set register button
		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launchRegisterActivity();
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
	
	protected void launchRegisterActivity(){
		Intent i = new Intent(this, Register.class);
		startActivity(i);
	}

}
