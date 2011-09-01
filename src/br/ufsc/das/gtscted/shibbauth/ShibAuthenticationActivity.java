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
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.ufsc.das.gtscted.shibbauth.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class ShibAuthenticationActivity extends Activity {

	private EditText usernameTxt;
	private EditText passwordTxt;
	private Button loginButton;
	private Button backButton;
	private Spinner idpSpinner;
	private Elements idpElements;
	private Elements formElements;
	private String wayfActionPath;
	private String selectedIdpUrl;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.idp_selection);
		loginButton = (Button) findViewById(R.id.loginButton);
		backButton = (Button) findViewById(R.id.backButton);
		usernameTxt = (EditText) findViewById(R.id.usernameTxt);
		passwordTxt = (EditText) findViewById(R.id.passwordTxt);
		idpSpinner = (Spinner) findViewById(R.id.idpSpinner);       

		//Configura o ArrayAdapter do spinner.
		ArrayAdapter<CharSequence> spinnerArrayAdapter;		
		spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		idpSpinner.setAdapter(spinnerArrayAdapter);
		
		// Obt�m os par�metros passados pela Activity anterior 
		// (no caso, a p�gina do WAYF como uma String e o
		// �nico cookie da Connection usada anteriormente)
		Bundle bundle = this.getIntent().getExtras();
		String wayfHtml = bundle.getString("html_source");
		final String wayfLocation = bundle.getString("wayf_location");
		final SerializableCookie receivedCookie =  (SerializableCookie) bundle.getSerializable("cookie");
				
		//Obt�m todos os tags de nome "option", que correspondem
		// aos IdPs, da p�gina do WAYF.
		Document wayfDocument = Jsoup.parse(wayfHtml);
		idpElements = wayfDocument.select("option");		
		
		//Popula o spinner com os nomes dos IdPs encontrados.		
		for(Element idpElement : idpElements){						
			String idpName = idpElement.text();
			spinnerArrayAdapter.add(idpName);
		}
		
		// Obt�m o caminho para o qual deve ser passado o IdP do usu�rio.
		formElements = wayfDocument.select("form");
		for(Element formElement : formElements){						
			if(formElement.attr("id").equals("IdPList")){
				wayfActionPath = formElement.attr("action");
			}			
		}			
		
		
		loginButton.setOnClickListener(new View.OnClickListener() {      	
			@Override 
			public void onClick(View v) {	
				// Obt�m a URL correspondente ao idP selecionado no spinner.
				int selectedIdpPosition = idpSpinner.getSelectedItemPosition();						
				Element selectedIdp = idpElements.get(selectedIdpPosition);
				selectedIdpUrl = selectedIdp.attr("value");
								
				try {
					// Obt�m os campos "username" e "password" fornecidos
					// pelo usu�rio e necess�rios para a autentica��o.
					String username = usernameTxt.getText().toString();
					String password = passwordTxt.getText().toString();
										
					// Cria um novo objeto Connection, e adiciona o 
					// cookie passado pela Activity anterior.
					Connection connection = new Connection();
					BasicClientCookie newCookie = new BasicClientCookie(receivedCookie.getName(),
												                       receivedCookie.getValue());
					newCookie.setDomain(receivedCookie.getDomain());
					connection.addCookie(newCookie);
					
					// Tenta realizar a autentica��o no IdP selecionado. O resultado corresponde
					// � p�gina para a qual o cliente � redirecionado em caso de autentica��o 
					// bem-sucedida.
					String authResult = connection.authenticate(wayfLocation, 
							                                       wayfActionPath,
							                                         selectedIdpUrl,
							                                               username,
							                                               password);
					
					// Apenas mostra o recurso que o usu�rio queria acessar (neste caso, mostra a p�g. de
					// "Homologa��o de atributos").
					Intent newIntent = new Intent(ShibAuthenticationActivity.this.getApplicationContext(), TestActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("arg", authResult);
					newIntent.putExtras(bundle);
					startActivity(newIntent);		
				
				} catch (IOException e) {
					String message = "IOException - problema na conex�o";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
			 	} catch (Exception e){
			 		String message = "Exception - problema na autentica��o";
					Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
					toast.show();
			 	}
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				finish();			
			}
		});


	}
}
