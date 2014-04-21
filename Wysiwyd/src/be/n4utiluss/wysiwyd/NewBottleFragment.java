package be.n4utiluss.wysiwyd;

import be.n4utiluss.wysiwyd.database.DatabaseContract;
import be.n4utiluss.wysiwyd.database.DatabaseHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class NewBottleFragment extends AbstractBottleInfoFragment {


	@Override
	protected void writeToDB(ContentValues values) {
		DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db.insert(DatabaseContract.BottleTable.TABLE_NAME, null, values);
		db.close();
		dismissFragment();
		getLinkedActivity().onNewBottleAdded();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();

		// We notify the activity that this fragment is being stopped with a callback method.
		getLinkedActivity().onNewBottleFragmentDismissed();
	}

	/**
	 * Interface that must be implemented by the linked activity, to be able to communicate with this fragment.
	 * @author anthonydebruyn
	 *
	 */
	public interface NewBottleFragmentCallbacks {

		/**
		 * Called when the new bottle has been added in the db.
		 */
		public void onNewBottleAdded();
		/**
		 * Called when the new bottle fragment is being destroyed.
		 * The call occurs during the onDestroy() method of this fragment.
		 */
		public void onNewBottleFragmentDismissed();
	}
}
