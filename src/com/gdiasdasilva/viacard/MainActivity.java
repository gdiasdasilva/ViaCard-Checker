package com.gdiasdasilva.viacard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private EditText username;
	private EditText password;
	boolean userDataSaved = false;

	String balance = "";
	String mbReference = "";
	int totalTrips = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.anim.abc_slide_in_bottom);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		setContentView(R.layout.activity_main);

		username = (EditText)findViewById(R.id.editText1);
		password = (EditText)findViewById(R.id.editText2);

		findViewById(R.id.loadingPanel).setVisibility(View.GONE);

		SharedPreferences settings = getSharedPreferences("UserInfo", 0);
		String savedUsername = settings.getString("Username", "");

		if(!(savedUsername.length() == 0))
		{
			userDataSaved = true;
			username.setText(settings.getString("Username", "").toString());
			password.setText(settings.getString("Password", "").toString());

			try
			{
				login();
			}
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try 
				{
					login();
				} 
				catch (UnsupportedEncodingException e) 
				{
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		});

		((CheckBox) findViewById(R.id.cbShowPwd)).setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (!isChecked)
				{
					// show password
					password.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				else 
				{
					// hide password
					password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}
			}
		});
	}

	@Override
	public void onPause()
	{
		super.onPause();
		overridePendingTransition(android.R.anim.fade_in, 0);
	}

	public void login() throws UnsupportedEncodingException, IOException
	{				
		findViewById(R.id.editText1).setVisibility(View.GONE);
		findViewById(R.id.editText2).setVisibility(View.GONE);
		findViewById(R.id.button1).setVisibility(View.GONE);
		findViewById(R.id.cbShowPwd).setVisibility(View.GONE);
		findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

		Thread thread = new Thread(new Runnable(){
			@Override
			public void run()
			{		
				try 
				{
					getData();
					sendData();

					if(!userDataSaved)
					{
						SharedPreferences settings = getSharedPreferences("UserInfo", 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("Username", username.getText().toString());
						editor.putString("Password", password.getText().toString());
						editor.commit();
					}
				} 
				catch(UnknownHostException uhe)
				{					
					uhe.printStackTrace();

					MainActivity.this.runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getApplicationContext(), "Problema de rede.", Toast.LENGTH_SHORT).show();
							showFields();
						}
					});
				}
				catch (ArrayIndexOutOfBoundsException e)
				{					
					e.printStackTrace();

					MainActivity.this.runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getApplicationContext(), "Dados incorrectos!", Toast.LENGTH_SHORT).show();
							showFields();
						}
					});
				}
				catch(Exception e)
				{
					e.printStackTrace();

					MainActivity.this.runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(getApplicationContext(), "Erro.", Toast.LENGTH_SHORT).show();
							showFields();
						}
					});
				}
			}
		});

		thread.start();
	}

	public void showFields()
	{
		findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		findViewById(R.id.editText1).setVisibility(View.VISIBLE);
		findViewById(R.id.editText2).setVisibility(View.VISIBLE);
		findViewById(R.id.button1).setVisibility(View.VISIBLE);
		findViewById(R.id.cbShowPwd).setVisibility(View.VISIBLE);
	}

	public void sendData()
	{
		Intent intent = new Intent(this, ShowData.class);
		intent.putExtra("mbReference", mbReference);
		intent.putExtra("balance", balance);
		intent.putExtra("totalTrips", totalTrips);
		startActivity(intent);
	}

	public void getData() throws IOException
	{
		String userId = "";
		String hrefText = "";
		int dataSize = -1;

		//With this you login and a session is created
		Connection.Response res = Jsoup.connect("https://www.lusoponte.pt/viacard/viacard_gestao.asp").timeout(20*1000)
				.data("Login", username.getText().toString())
				.data("Password", password.getText().toString())
				.data("choice", "validateLogin")
				.method(Method.POST)
				.execute();

		//This will get you cookies
		Map<String, String> loginCookies = res.cookies();

		//Here you parse the page that you want. Put the url that you see when you have logged in
		Document doc = Jsoup.connect("https://www.lusoponte.pt/viacard/viacard_gestao.asp").timeout(20*1000)
				.cookies(loginCookies)
				.get();

		Elements urlsList = doc.select("a[class=texto]");

		for(Element e : urlsList)
		{
			if(e.toString().contains("altdados"))
			{
				hrefText = e.attr("href");
				break;
			}
		}	
		
		Elements tables = doc.select("table[class=textopequeno]");
		Elements rows = tables.select("tr[valign=top]");
		
		if(rows.size() == 2)
		{
			Elements fields = rows.get(1).select("td[align=center]");
			totalTrips = Integer.parseInt(fields.get(0).text());
		}	

		userId = hrefText.split("=")[2];

		Document clientDataDoc = Jsoup.connect("https://www.lusoponte.pt/viacard/viacard_gestao.asp?choice=altdadoscliente&v=" + userId)
				.timeout(10*1000)
				.cookies(loginCookies)
				.get();

		Elements data = clientDataDoc.getElementsByClass("texto").select("td");

		dataSize = data.text().split(" ").length;
		String[] words = new String[dataSize];
		words = data.text().split(" ");

		for(int i = 0; i < words.length; i++)
		{
			if(words[i].contains("Saldo:"))
				balance = words[i+1];

			if(words[i].contains("Multibanco:"))
				mbReference = words[i+1];
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
