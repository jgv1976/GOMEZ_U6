package org.example.GOMEZJ_U6;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class EnviarMensaje extends Activity {
	EditText etmensaje;
	Button enviar;
	String mensaje;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enviarmensaje);
		etmensaje = (EditText) findViewById(R.id.et_mensaje);
		enviar = (Button) findViewById(R.id.btn_enviar);
		
	}
	
	
	public void retorno (View v) {
		mensaje = etmensaje.getText().toString();
		Intent i = new Intent();
		i.putExtra("mensaje", mensaje);
		setResult(RESULT_OK, i);
		finish();
	}

}
