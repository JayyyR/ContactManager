package com.example.android.contactmanager;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

public class Login extends Activity {
	
	private Client mKinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//Connect to Kinvey Backend
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();

		//check if user is logged in
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
