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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

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
		user = getIntent().getExtras().getString("user");
		mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_adder);

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

		//        // Prepare model for account spinner
		//        mAccounts = new ArrayList<AccountData>();
		//        mAccountAdapter = new AccountAdapter(this, mAccounts);

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

		// Prepare the system account manager. On registering the listener below, we also ask for
		// an initial callback to pre-populate the account list.
		//        AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);

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
		contact.set("name", name);
		contact.set("account", user);
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
		//add entities to arraylists to account for how information is read on the contact list
		ArrayList<ContactEntity.phone> phones = new ArrayList<ContactEntity.phone>();
		ArrayList<ContactEntity.email> emails = new ArrayList<ContactEntity.email>();
		phones.add(phoneEntity);
		emails.add(emailEntity);
		
		//set the phone and emails to the contact entity object
		contact.set("phone", phones);
		contact.set("email", emails);
		Log.v(TAG, "saving: "+contact);

		//save the contact
		AsyncAppData<ContactEntity> contacts = mKinveyClient.appData("contacts", ContactEntity.class);
		contacts.save(contact, new KinveyClientCallback<ContactEntity>() {
			@Override
			public void onFailure(Throwable e) {
				Log.e(TAG, "failed to save contact data", e); 
				goBackToContactManager();
			}
			@Override
			public void onSuccess(ContactEntity r) {
				Log.d(TAG, "saved data for entity "+ r.getName());
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
//
//        // Prepare contact creation request
//        //
//        // Note: We use RawContacts because this data must be associated with a particular account.
//        //       The system will aggregate this with any other data for this contact and create a
//        //       coresponding entry in the ContactsContract.Contacts provider for us.
//        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, mSelectedAccount.getType())
//                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, mSelectedAccount.getName())
//                .build());
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
//                .build());
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
//                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
//                .build());
//        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
//                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType)
//                .build());
//
//        // Ask the Contact provider to create a new contact
//        Log.i(TAG,"Selected account: " + mSelectedAccount.getName() + " (" +
//                mSelectedAccount.getType() + ")");
//        Log.i(TAG,"Creating contact: " + name);
//        try {
//            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//        } catch (Exception e) {
//            // Display warning
//            Context ctx = getApplicationContext();
//            CharSequence txt = getString(R.string.contactCreationFailure);
//            int duration = Toast.LENGTH_SHORT;
//            Toast toast = Toast.makeText(ctx, txt, duration);
//            toast.show();
//
//            // Log exception
//            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
//        }
//    }
//
//    /**
//     * Called when this activity is about to be destroyed by the system.
//     */
//    @Override
//    public void onDestroy() {
//        // Remove AccountManager callback
//        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
//        super.onDestroy();
//    }
//
//    /**
//     * Updates account list spinner when the list of Accounts on the system changes. Satisfies
//     * OnAccountsUpdateListener implementation.
//     */
//    public void onAccountsUpdated(Account[] a) {
//        Log.i(TAG, "Account list update detected");
//        // Clear out any old data to prevent duplicates
//        mAccounts.clear();
//
//        // Get account data from system
//        AuthenticatorDescription[] accountTypes = AccountManager.get(this).getAuthenticatorTypes();
//
//        // Populate tables
//        for (int i = 0; i < a.length; i++) {
//            // The user may have multiple accounts with the same name, so we need to construct a
//            // meaningful display name for each.
//            String systemAccountType = a[i].type;
//            AuthenticatorDescription ad = getAuthenticatorDescription(systemAccountType,
//                    accountTypes);
//            AccountData data = new AccountData(a[i].name, ad);
//            mAccounts.add(data);
//        }
//
//        // Update the account spinner
//        mAccountAdapter.notifyDataSetChanged();
//    }
//
//    /**
//     * Obtain the AuthenticatorDescription for a given account type.
//     * @param type The account type to locate.
//     * @param dictionary An array of AuthenticatorDescriptions, as returned by AccountManager.
//     * @return The description for the specified account type.
//     */
//    private static AuthenticatorDescription getAuthenticatorDescription(String type,
//            AuthenticatorDescription[] dictionary) {
//        for (int i = 0; i < dictionary.length; i++) {
//            if (dictionary[i].type.equals(type)) {
//                return dictionary[i];
//            }
//        }
//        // No match found
//        throw new RuntimeException("Unable to find matching authenticator");
//    }
//
//    /**
//     * A container class used to repreresent all known information about an account.
//     */
//    private class AccountData {
//        private String mName;
//        private String mType;
//        private CharSequence mTypeLabel;
//        private Drawable mIcon;
//
//        /**
//         * @param name The name of the account. This is usually the user's email address or
//         *        username.
//         * @param description The description for this account. This will be dictated by the
//         *        type of account returned, and can be obtained from the system AccountManager.
//         */
//        public AccountData(String name, AuthenticatorDescription description) {
//            mName = name;
//            if (description != null) {
//                mType = description.type;
//
//                // The type string is stored in a resource, so we need to convert it into something
//                // human readable.
//                String packageName = description.packageName;
//                PackageManager pm = getPackageManager();
//
//                if (description.labelId != 0) {
//                    mTypeLabel = pm.getText(packageName, description.labelId, null);
//                    if (mTypeLabel == null) {
//                        throw new IllegalArgumentException("LabelID provided, but label not found");
//                    }
//                } else {
//                    mTypeLabel = "";
//                }
//
//                if (description.iconId != 0) {
//                    mIcon = pm.getDrawable(packageName, description.iconId, null);
//                    if (mIcon == null) {
//                        throw new IllegalArgumentException("IconID provided, but drawable not " +
//                                "found");
//                    }
//                } else {
//                    mIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
//                }
//            }
//        }
//
//        public String getName() {
//            return mName;
//        }
//
//        public String getType() {
//            return mType;
//        }
//
//        public CharSequence getTypeLabel() {
//            return mTypeLabel;
//        }
//
//        public Drawable getIcon() {
//            return mIcon;
//        }
//
//        public String toString() {
//            return mName;
//        }
//    }
//
//    /**
//     * Custom adapter used to display account icons and descriptions in the account spinner.
//     */
//    private class AccountAdapter extends ArrayAdapter<AccountData> {
//        public AccountAdapter(Context context, ArrayList<AccountData> accountData) {
//            super(context, android.R.layout.simple_spinner_item, accountData);
//            setDropDownViewResource(R.layout.account_entry);
//        }
//
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//            // Inflate a view template
//            if (convertView == null) {
//                LayoutInflater layoutInflater = getLayoutInflater();
//                convertView = layoutInflater.inflate(R.layout.account_entry, parent, false);
//            }
//            TextView firstAccountLine = (TextView) convertView.findViewById(R.id.firstAccountLine);
//            TextView secondAccountLine = (TextView) convertView.findViewById(R.id.secondAccountLine);
//            ImageView accountIcon = (ImageView) convertView.findViewById(R.id.accountIcon);
//
//            // Populate template
//            AccountData data = getItem(position);
//            firstAccountLine.setText(data.getName());
//            secondAccountLine.setText(data.getTypeLabel());
//            Drawable icon = data.getIcon();
//            if (icon == null) {
//                icon = getResources().getDrawable(android.R.drawable.ic_menu_search);
//            }
//            accountIcon.setImageDrawable(icon);
//            return convertView;
//        }
//    }
//}
