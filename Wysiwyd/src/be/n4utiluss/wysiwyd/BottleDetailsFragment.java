package be.n4utiluss.wysiwyd;

import be.n4utiluss.wysiwyd.database.DatabaseHelper;
import be.n4utiluss.wysiwyd.database.DatabaseContract.*;
import be.n4utiluss.wysiwyd.fonts.Fonts;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class BottleDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int MAIN_INFO_LOADER = 0;
	private static final int VARIETY_LOADER = 1;
	private BottleDetailsFragmentCallbacks linkedActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);	// So the onCreateOptionsMenu method is called, and the actions are set.
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_bottle_details, container, false);

		// (Re)start the loaders here, since this method is the first one called after we get back from the new bottle fragment, 
		// after we popped the previous state from the stack.

		if (this.getArguments().containsKey(BottleTable._ID)) {
			getLoaderManager().restartLoader(MAIN_INFO_LOADER, null, this);
			getLoaderManager().restartLoader(VARIETY_LOADER, null, this);
			Log.i("CREATE", "Test");
		}

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Fonts
		TextView name = (TextView) getView().findViewById(R.id.details_name);
		name.setTypeface(Fonts.getFonts(getActivity()).chopinScript);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof BottleDetailsFragmentCallbacks)) {
			throw new IllegalStateException("Activity must implement fragment callbacks.");
		}

		this.linkedActivity = (BottleDetailsFragmentCallbacks) activity;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.details, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch (id) {
		case R.id.action_edit:
			this.linkedActivity.onEditEvent(getArguments().getLong(BottleTable._ID));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle bundle) {
		long id = this.getArguments().getLong(BottleTable._ID);
		String idString = Long.toString(id);
		SQLiteCursorLoader cursorLoader = null;

		switch (idLoader){
		case MAIN_INFO_LOADER:
			cursorLoader = new SQLiteCursorLoader(this.getActivity(),
					new DatabaseHelper(this.getActivity()), 
					"SELECT * " +
							" FROM " + BottleTable.TABLE_NAME +
							" WHERE " + BottleTable._ID + " = ?", 
							new String[] { idString });

			break;
		case VARIETY_LOADER:
			cursorLoader = new SQLiteCursorLoader(this.getActivity(),
					new DatabaseHelper(this.getActivity()), 
					"SELECT DISTINCT v." + VarietyTable.COLUMN_NAME_NAME +
					" FROM " + BottleVarietyTable.TABLE_NAME + " bv, " + VarietyTable.TABLE_NAME + " v " +
					" WHERE bv." + BottleVarietyTable._ID + " = ?" +
					" AND bv." + BottleVarietyTable.COLUMN_NAME_VARIETY_ID + " = v." + VarietyTable._ID, 
					new String[] { idString });

			break;
		default:
		}
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (loader.getId()){
		case MAIN_INFO_LOADER:
			if (cursor.moveToFirst()) {

				TextView appellation = (TextView) getView().findViewById(R.id.details_appellation);
				TextView name = (TextView) getView().findViewById(R.id.details_name);
				TextView vintage = (TextView) getView().findViewById(R.id.details_vintage);
				TextView region = (TextView) getView().findViewById(R.id.details_region);
				TextView quantity = (TextView) getView().findViewById(R.id.details_quantity);
				TextView price = (TextView) getView().findViewById(R.id.details_price);
				RatingBar ratingBar = (RatingBar) getView().findViewById(R.id.details_mark);
				TextView colour = (TextView) getView().findViewById(R.id.details_colour);
				TextView sugar = (TextView) getView().findViewById(R.id.details_sugar);
				TextView effervescence = (TextView) getView().findViewById(R.id.details_effervescence);
				TextView addDate = (TextView) getView().findViewById(R.id.details_addDate);
				TextView apogee = (TextView) getView().findViewById(R.id.details_apogee);
				TextView location = (TextView) getView().findViewById(R.id.details_location);
				TextView note = (TextView) getView().findViewById(R.id.details_note);
				TextView code = (TextView) getView().findViewById(R.id.details_code);

				appellation.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_APPELLATION)));
				name.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_NAME)));
				vintage.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_VINTAGE))));
				region.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_REGION)));
				quantity.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_QUANTITY))) + " bottles");
				price.setText("Price: " + Float.toString(cursor.getFloat(cursor.getColumnIndex(BottleTable.COLUMN_NAME_PRICE))));
				ratingBar.setRating(cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_MARK)));

				int colourValue = cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_COLOUR));
				String[] colourArray = getResources().getStringArray(R.array.colour_array);
				colour.setText(colourArray[colourValue]);

				int sugarValue = cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_SUGAR));
				String[] sugarArray = getResources().getStringArray(R.array.sugar_array);
				sugar.setText(sugarArray[sugarValue]);

				int effervescenceValue = cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_EFFERVESCENCE));
				String[] effervescenceArray = getResources().getStringArray(R.array.effervescence_array);
				effervescence.setText(effervescenceArray[effervescenceValue]);
				
				addDate.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_ADD_DATE)));
				apogee.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_APOGEE)));
				location.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_LOCATION)));
				note.setText(cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_NOTE)));
				code.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(BottleTable.COLUMN_NAME_CODE))));
				Log.i("CUR", "Test");
			}
			break;

		case VARIETY_LOADER:
			if (this.getView() == null)
				Log.e("Details", "Null POINTER!!!!");
			LinearLayout ll = (LinearLayout) getView().findViewById(R.id.details_varieties_layout);
			cursor.moveToPosition(-1);
			while (cursor.moveToNext()) {
				TextView tv = new TextView(getActivity());
				tv.setText(cursor.getString(cursor.getColumnIndex(VarietyTable.COLUMN_NAME_NAME)));

				ll.addView(tv);
			}
			break;
		default:
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	/**
	 * The interface that must be implemented by the connected activity, 
	 * to allow this fragment to communicate with it.
	 * @author anthonydebruyn
	 *
	 */
	public interface BottleDetailsFragmentCallbacks {
		/**
		 * Called after the edit action button has been pushed.
		 */
		public void onEditEvent(long id);
	}
}
