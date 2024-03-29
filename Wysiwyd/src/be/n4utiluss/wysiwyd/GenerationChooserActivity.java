package be.n4utiluss.wysiwyd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import be.n4utiluss.wysiwyd.database.DatabaseContract;
import be.n4utiluss.wysiwyd.database.DatabaseContract.BottleTable;
import be.n4utiluss.wysiwyd.database.DatabaseHelper;
import be.n4utiluss.wysiwyd.fonts.Fonts;
import be.n4utiluss.wysiwyd.nfc.NFCWriterActivity;
import be.n4utiluss.wysiwyd.qr_code.Contents;
import be.n4utiluss.wysiwyd.qr_code.QRCodeEncoder;
import be.n4utiluss.wysiwyd.zxing.integration.android.IntentIntegrator;
import be.n4utiluss.wysiwyd.zxing.integration.android.IntentResult;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class GenerationChooserActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String ALBUM_NAME = "Wysiwyd Bottles";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generation_chooser);

		// The loader for the background image:
		getLoaderManager().initLoader(0, null, this);

		// Fonts
		TextView title = (TextView) findViewById(R.id.generation_chooser_title);
		TextView barcode = (TextView) findViewById(R.id.generation_chooser_barcode_text);
		TextView nfc = (TextView) findViewById(R.id.generation_chooser_nfc_text);

		title.setTypeface(Fonts.getFonts(this).robotoThin);
		barcode.setTypeface(Fonts.getFonts(this).robotoThin);
		nfc.setTypeface(Fonts.getFonts(this).robotoThin);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_choice, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when the user pushes the qr code generation button, to generate a qr code and send it.
	 */
	@SuppressLint("SimpleDateFormat")
	public void QRorBarCode(View view) {
		long code = getIntent().getExtras().getLong(ResultsActivity.BOTTLE_CODE);
		String codeString = Long.toString(code);

		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(codeString, 
				null,
				Contents.Type.TEXT, 
				BarcodeFormat.QR_CODE.toString(), 
				300);

		FileOutputStream out = null;

		try {
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();

			// Create an image file name
		    String imageFileName = "QRCode_" + codeString + ".png";
		    File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM_NAME);
		    if (!storageDir.exists() || !storageDir.isDirectory()) {
		    	if (!storageDir.mkdirs()) {
		            Log.e("Picture folder creation", "Directory not created");
		            throw new IOException("Album Directory not created.");
		        }
		    }
		    File image = new File(storageDir, imageFileName);
		    if (image.exists())
		    	image.delete();
			
			out = new FileOutputStream(image);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			
			Uri pngUri = Uri.fromFile(image);
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("image/*");
			emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, pngUri);
			
			startActivity(emailIntent);

		} catch (WriterException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try{
				out.close();
			} catch(Throwable ignore) {}
		}
	}

	/**
	 * Called when the user pushes the 
	 * @param view
	 */
	public void NFCCode(View view) {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
			Intent nfcIntent = new Intent(this, NFCWriterActivity.class);
			long code = getIntent().getExtras().getLong(ResultsActivity.BOTTLE_CODE);
			nfcIntent.putExtra(ResultsActivity.BOTTLE_CODE, code);
			startActivity(nfcIntent);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

			if (resultCode == RESULT_OK && scanResult != null) {

			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle bundle) {

		SQLiteCursorLoader cursorLoader = new SQLiteCursorLoader(this,
				DatabaseHelper.getInstance(this), 
				DatabaseContract.SELECT + BottleTable._ID + DatabaseContract.COMMA_SEP + BottleTable.COLUMN_NAME_IMAGE +
				DatabaseContract.FROM + BottleTable.TABLE_NAME +
				DatabaseContract.WHERE + BottleTable.COLUMN_NAME_IMAGE + DatabaseContract.NOT + DatabaseContract.NULL +
				DatabaseContract.ORDER_BY + DatabaseContract.RANDOM +
				DatabaseContract.LIMIT + "1", 
				null);

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		String photoPath = null;

		if (cursor.moveToFirst()) {
			photoPath = cursor.getString(cursor.getColumnIndex(BottleTable.COLUMN_NAME_IMAGE));
		} else {
			return;
		}

		// Check if file exists and is readable:
		File picture = new File(photoPath);
		if (!picture.exists() || !picture.isFile() || !picture.canRead()) {
			return;
		}

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

		ImageView imageView = (ImageView) findViewById(R.id.generation_chooser_image_background);
		Bitmap finalBitmap = BlurBuilder.blur(this, bitmap);
		imageView.setImageBitmap(finalBitmap);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {}
}