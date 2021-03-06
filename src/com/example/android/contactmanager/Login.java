package com.example.android.contactmanager;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
			final ProgressDialog progressDialog = ProgressDialog.show(this, "Logging in with Saved Credentials", "just a moment");
			
			//retrieve updated info for user, had to do this in order to get current user's username
			mKinveyClient.user().retrieve(new KinveyUserCallback() {
				@Override
				public void onFailure(Throwable e) {
					progressDialog.dismiss();
				}
				@Override
				public void onSuccess(User user) { 
					progressDialog.dismiss();
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
				
				//login the user
				mKinveyClient.user().login(username, password, new KinveyUserCallback() {
					public void onFailure(Throwable t) {
						CharSequence text = "Wrong username or password.";
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

					}
					public void onSuccess(User u) {
						CharSequence text = "Welcome back, " + u.getUsername() + ".";
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

	//launch into main contact view
	protected void launchContactManager(){
		Intent i = new Intent(this, ContactManager.class);
		i.putExtra("user", username);
		startActivity(i);
	}

	//launch register activity
	protected void launchRegisterActivity(){
		Intent i = new Intent(this, Register.class);
		startActivity(i);
	}

	//override back button so user can't back
	@Override
	public void onBackPressed() {
	}


}
