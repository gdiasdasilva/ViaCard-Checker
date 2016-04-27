package com.gdiasdasilva.viacard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ShowData extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    getWindow().setWindowAnimations(R.anim.abc_fade_in);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		setContentView(R.layout.activity_show_data);
		
		Intent intent = getIntent();
		String mbReference = intent.getStringExtra("mbReference");
		String balance = intent.getStringExtra("balance");
		int totalTrips = intent.getIntExtra("totalTrips", -1);
		
		TextView mbReferenceText = (TextView)findViewById(R.id.textViewCard);
		mbReferenceText.setText(mbReference);
		
		TextView balanceText = (TextView)findViewById(R.id.textViewValue);
		balanceText.setText(balance + " €");
		
		TextView priceText = (TextView)findViewById(R.id.textViewPrice);
		TextView totalTripsText = (TextView)findViewById(R.id.textViewTotalTrips);
		
		if(totalTrips != -1)
		{
			totalTripsText.setText(String.valueOf(totalTrips));
			
			if(totalTrips > 12)
			{
				priceText.setText("0,495 €");
			}
			else
			{
				priceText.setText("1,485 €");
			}
		}
		else
		{
			totalTripsText.setText("Dados não disponíveis");
			priceText.setText("1,485 €");
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		overridePendingTransition(android.R.anim.fade_in, 0);
	}
	
	public void wipeUserData(View view)
	{
		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		
		Toast.makeText(getApplicationContext(), "Sessão terminada.", Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_data, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
