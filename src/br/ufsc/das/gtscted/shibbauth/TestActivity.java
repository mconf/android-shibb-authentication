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

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


/*Activity que apenas mostra uma String recebida atrav�s do Bundle.
 */

public class TestActivity extends Activity {

	private TextView scrollableTextView;
	@Override
	protected void onPause() {
		finish();
	}	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.client_tester);
		scrollableTextView = (TextView) findViewById(R.id.textToDisplay);
		Bundle bundle = this.getIntent().getExtras();
		String htmlReceived = bundle.getString("arg");
		scrollableTextView.setText(htmlReceived);	
			
	}
}
