package com.clara.beesightings;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clara.beesightings.firebase.BeeSighting;
import com.clara.beesightings.firebase.Firebase;

import java.util.ArrayList;


/** A list of user's sightings, and ability to modify or delete */


//TODO notify user that app is working - looks like it isn't doing anything.
//TODO stop user modifying a sighting while the update is being processed?
	//TODO the update depends on the list position. The list may change as the user is updating. Should use sighting key to update.


public class ManageSightingsActivity extends AppCompatActivity implements Firebase.SightingsUpdatedListener, EditSightingDialog.SightingUpdatedListener {

	ListView mSightingList;
	UserSightingsAdapter mAdapter;

	Firebase firebase;

	private static String TAG = "MANAGE SIGHTINGS";

	private TextView title;
	private boolean loaded = false;

	private boolean listActive = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_sightings);

		mSightingList = (ListView) findViewById(R.id.user_list);
		mAdapter = new UserSightingsAdapter(this, R.layout.user_sightings_list_element);
		mSightingList.setAdapter(mAdapter);

		firebase = new Firebase();

		//get userID
		String userId = getSharedPreferences(SignInActivity.USERS_PREFS, MODE_PRIVATE).getString(SignInActivity.FIREBASE_USER_ID_PREF_KEY, "something has gone wrong here");

		//Request this user's reported sightings
		firebase.getUserSightings(this, userId);

	}


	@Override
	public void sightingsUpdated(ArrayList<BeeSighting> s) {

		if (!listActive) {
			activateList();
			listActive = true;
		}

		Log.d(TAG, s.toString());

		mAdapter.clear();
		mAdapter.addAll(s);
		mAdapter.notifyDataSetChanged();

	}


	private void activateList() {

		//Tap to edit....

		mSightingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				//Display dialog with ability to edit number of bees and description
				BeeSighting toEdit = mAdapter.getItem(i);
				EditSightingDialog dialog = EditSightingDialog.newInstance(toEdit);
				dialog.show(getSupportFragmentManager(), "Edit Sighting Dialog");
				// The callback - sightingUpdated() sends the update to Firebase.
			}
		});


		// Long-press to delete

		mSightingList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

				final BeeSighting toDelete = mAdapter.getItem(i);

				AlertDialog.Builder builder = new AlertDialog.Builder(ManageSightingsActivity.this);
				builder.setMessage("Delete this sighting?")
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								//delete item
								//So this will cause a value event, which causes Firebase to call sightings updated, which will update the list.

								Log.d(TAG, "Deleting " + toDelete);
								firebase.deleteSighting(toDelete);
								Toast.makeText(ManageSightingsActivity.this, "Sighting being deleted", Toast.LENGTH_LONG).show();

							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.create()
						.show();

				return false;
			}
		});

	}


	//Update Dialog callback

	@Override
	public void sightingUpdated(BeeSighting updated) {

		Log.d(TAG, "Sighting updated callback, updated sighting is " + updated);
		Toast.makeText(this, "Sighting updated", Toast.LENGTH_LONG).show();

		firebase.updateSighting(updated);

	}
}
