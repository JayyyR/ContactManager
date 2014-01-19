package com.example.android.contactmanager;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Activity {

	private Client mKinveyClient;
	private EditText mUsernameField;
	private EditText mPasswordField;
	private Button mRegisterButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		//Connect to Kinvey Backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

		//grab UI elements
		mUsernameField = (EditText) findViewById(R.id.userEditText);
		mPasswordField = (EditText) findViewById(R.id.passEditText);
		mRegisterButton = (Button) findViewById(R.id.regButton);

		mRegisterButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String username = mUsernameField.getText().toString();
				String password = mPasswordField.getText().toString();

				mKinveyClient.user().create(username, password, new KinveyUserCallback() {
					public void onFailure(Throwable t) {
						CharSequence text = "Could not sign up.";
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
					}
					public void onSuccess(User u) {
						CharSequence text = u.getUsername() + ", your account has been created.";
						Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
						launchLogin();
					}
				});

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	
	protected void launchLogin() {
		Intent i = new Intent(this, Login.class);
		startActivity(i);
	}

}
