/**
 *  Copyright (C) 2011 GT-STCFed - RNP - http://gtstcfed.das.ufsc.br
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2011 GT-STCFed - RNP - http://gtstcfed.das.ufsc.br
 *
 *  Este programa é software livre; você pode redistribuí-lo e/ou
 *  modificá-lo sob os termos da Licença Pública Geral GNU, conforme
 *  publicada pela Free Software Foundation; tanto a versão 2 da
 *  Licença como (a seu critério) qualquer versão mais nova.
 *
 *  Este programa é distribuído na expectativa de ser útil, mas SEM
 *  QUALQUER GARANTIA; sem mesmo a garantia implícita de
 *  COMERCIALIZAÇÃO ou de ADEQUAÇÃO A QUALQUER PROPÓSITO EM
 *  PARTICULAR. Consulte a Licença Pública Geral GNU para obter mais
 *  detalhes.
 *
 *  Você deve ter recebido uma cópia da Licença Pública Geral GNU
 *  junto com este programa; se não, escreva para a Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307, USA.
*/
package br.ufsc.das.gtscted.shibbauth;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.client.ClientProtocolException;
import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SPSelectionActivity extends Activity {
	Button nextButton;
	Button exitButton;
	EditText spEditText;
	AndroidHttpClient httpClient;
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}	
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.sp_selection);
		
		nextButton = (Button) findViewById(R.id.nextButton);		
		exitButton = (Button) findViewById(R.id.exitButton);
		spEditText = (EditText) findViewById(R.id.spUrlEditText);
		
		//SP para testes	
		spEditText.setText("https://sp.ufrgs.br/chimarrao/");
		
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String serviceUrl = spEditText.getText().toString();
										
				try {
					Connection connection = new Connection();
					String[] getResponseAndEndpoint = connection.httpGetWithEndpoint(serviceUrl);
					String wayfLocation = getResponseAndEndpoint[0];
					String responseBody = getResponseAndEndpoint[1];					
						
					Bundle bundle = new Bundle();
					bundle.putString("html_source", responseBody);
					bundle.putString("wayf_location", wayfLocation);
					bundle.putSerializable("cookie", connection.getSerializableCookie(0));
								
					Intent newIntent = new Intent(SPSelectionActivity.this, ShibAuthenticationActivity.class);
					newIntent.putExtras(bundle);
					startActivity(newIntent);		
															
				} catch (KeyManagementException e) {
					String message = "KeyManagementException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch (NoSuchAlgorithmException e) {
					String message = "NoSuchAlgorithmException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch (KeyStoreException e) {
					String message = "KeyStoreException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch (UnrecoverableKeyException e) {
					String message = "UnrecoverableKeyException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch (ClientProtocolException e) {
					String message = "ClientProtocolException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch (IOException e) {
					String message = "IOException";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				} catch(Exception e){
					String message = "Exception";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
				}				
			}
		});
				
		exitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			} 			
		});		
	}	
}
