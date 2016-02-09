package org.example.GOMEZJ_U6;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class Bienvenida extends Activity {

	@Override
	public void onCreate(Bundle icicle) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(icicle);

		// muestra  la activity de bienvenida
		
		setContentView(R.layout.bienvenida);

		// disponemos el tiempo en milisegundos que estara la pantalla
		
		Handler x = new Handler();
		x.postDelayed(new splashhandler(), 3000);
	}

	class splashhandler implements Runnable {

		public void run() {

			// llamamos a la activity principal
			
			startActivity(new Intent(getApplication(), ActividadPrincipal.class));
			
			// cerramos esta 
			
			finish();

		}
	}
}