package org.example.GOMEZJ_U6;

import static org.example.GOMEZJ_U6.UtilidadesGCM.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class ActividadPrincipal extends Activity {
	
	private String idRegistro = "";
	private GoogleCloudMessaging gcm;
	private String mensaje;
	private String funcion;
	private String idRecibido;
	private String identificador;
	private String numPartida;
	private String remitente;
	private String nombreJ1;
	private ProgressDialog dialogo;
	private WebView navegador;
	final InterfazComunicacion miInterfazJava = new InterfazComunicacion(this);
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private BackupManager backupManager;

	//private final String apiKey = "AIzaSyDbfyYKiUs_vHF6bp-C9nUj7l6-hBhe9MM";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
// ------ Copia de seguridad en la nube de Preferencias ----------	
		backupManager = new BackupManager(this);
		
		SharedPreferences prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
		nombreJ1 = prefs.getString("jugador1", null);
		if (nombreJ1==(null) || nombreJ1.equals("")) nombreJugador("1");
		else mostrarMensajeToast(this, "Bienvenido "+nombreJ1);

		navegador = (WebView) findViewById(R.id.webkit);
		navegador.getSettings().setJavaScriptEnabled(true);
		navegador.addJavascriptInterface(miInterfazJava, "jsInterfazNativa");
		navegador.getSettings().setBuiltInZoomControls(false);
		navegador.loadUrl ("File:///android_asset/index.html");
		
		
		//------------- FUNCIONAMIENTO DEL WEBVIEW ---------------------------
		
		navegador.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return false;
			}
		});

		navegador.setWebChromeClient(new WebChromeClient() {

			@Override 
			public boolean onJsAlert(WebView view, String url, String message,JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

		});

		navegador.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				dialogo = new ProgressDialog(ActividadPrincipal.this);
				dialogo.setMessage("Abriendo aplicacion....");
				dialogo.setCancelable(true);
				dialogo.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				dialogo.dismiss();
				if (nombreJ1 != null) {
					navegador.loadUrl("javascript:cambiaNombreJugador(\"1\",\"" + nombreJ1+ "\");");
				}
			}

			@Override 
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ActividadPrincipal.this);
				builder.setMessage(description).setPositiveButton("Aceptar", 
						null).setTitle("onReceivedError");
				builder.show();
			}
		});
		
		//-------------- COMPROBAMOS QUE ESTA INSTALADO GOOGLE PLAY SERVICES ------------------

		registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
		if (comprobarGooglePlayServices()) {
			//mostrarMensajeToast(this,"Se ha encontrado un Google Play Services válido.");
		} else {
			mostrarMensajeToast(this,"No se ha encontrado un Google Play Services válido.");
		}
		registrarUsuarioGCM();
		//estadoConexion();
	}
	
	
	//--------------- OBTENER LA CLAVE DE IDENTIFICACION Y EL NOMBRE DE USUARIO -----------

	public void verId() {
		if (!dameIdRegistro(getApplicationContext()).equals(""))
			mostrarMensajeToast(this, "Nombre: "+nombreJ1+"\nIdReg= "+dameIdRegistro(getApplicationContext()));
		else mostrarMensajeToast(this, "Nombre: "+nombreJ1+"\nNo estas Conectado");
	}

	
	//------------------ CONFIGURACION DEL BOTON DE VUELTA ATRAS ------------------
	
	@Override 
	public void onBackPressed() {
		if (navegador.canGoBack()) {
			navegador.goBack();
		}
		else terminarJuego();
	}

	//----------------- COMPRUEBA SI HAY DISPONIBLE CONEXION A INTERNET ------------------
	
	private boolean comprobarConectividad() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if ((info == null || !info.isConnected() || !info.isAvailable())) {
			Toast.makeText(ActividadPrincipal.this,"Oops! No tienes conexión a internet", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	// --------------- INTERFAZ DE COMUNICACION ENTRE WEB Y ANDROID ----------
	// -------- Aqui estan los metodos que se llaman desde el WebView --------
	
	public class InterfazComunicacion {
		Context mContext;

		InterfazComunicacion(Context c) {
			mContext = c;
		}
		@JavascriptInterface
		public void mensaje(String contenido){
			Toast.makeText(mContext, contenido, Toast.LENGTH_SHORT).show();
		}
		@JavascriptInterface
		public void salir() {
			terminarJuego();
		}

		@JavascriptInterface
		public void compruebaConexion() {
			estadoConexion();
		}

		@JavascriptInterface
		public void jugarOnline() {
			juegoOnline("Enviar peticion de Partida OnLine?");
		}

		@JavascriptInterface
		public void envioMensaje() {
			enviar();
		}
	} 
	

	//------------------- OPCIONES DEL MENU PRINCIPAL ----------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.inicio, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.inicio:
			navegador.loadUrl("javascript:mostrarInicio()");
			break;
		case R.id.jugador1:
			nombreJugador("1");
			break;
		case R.id.jugador2:
			nombreJugador("2");
			break;
		case R.id.conectar:
			registrarUsuarioGCM();
			break;
		case R.id.desconectar:
			desregistrarUsuarioGCM();
			break;   
		case R.id.idevice:
			verId();
			break; 
		case R.id.creditos:
			verCreditos();
		}

		return true;
	}

	
	// ------------ CAMBIA EL NOMBRE DE LOS JUGADORES ---------------------------
	
	public void nombreJugador(final String jugador) {
		AlertDialog.Builder alert = new AlertDialog.Builder(ActividadPrincipal.this);
		alert.setTitle("Nombre jugador" + jugador);
		alert.setMessage("Nombre:");
		final EditText nombre = new EditText(getBaseContext());
		alert.setView(nombre);
		alert.setPositiveButton("Guardar",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int whichButton) {
				Editable valor = nombre.getText();
				navegador.loadUrl("javascript:cambiaNombreJugador(\"" + jugador + "\",\"" + valor.toString() + "\");");
				SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("jugador" + jugador,valor.toString());
				editor.commit();
			}
		});
		alert.setNegativeButton("Cancelar",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int whichButton) {
			}
		});
		alert.show();
		mostrarMensajeToast(this, "Se ha cambiado el nombre a "+nombreJ1);
	}
	
	
	//-------------- ENVIO DE UN MENSAJE (abre otra Actividad) ----------
	
	public void enviar() {
		Intent i = new Intent(this, EnviarMensaje.class);
		startActivityForResult(i,1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				mensaje = data.getStringExtra("mensaje");
			}
			enviarMensaje(mensaje);
		}
	}
	
	// ---------- CIERRA LA APLICACION (y desconecta el usuario del servidor)----------

	public void terminarJuego() {

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Cerrar Aplicacion")
		.setMessage("Seguro que quieres salir?")
		.setPositiveButton("Si", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();    
			}
		})
		.setNegativeButton("No", null)
		.show();

	}

	
	public void verCreditos() {
		Intent i = new Intent(this, Creditos.class);
		startActivity(i);
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		actividadAbierta = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		actividadAbierta = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		estadoConexion();
		comprobarGooglePlayServices();
		recibeMensaje(getIntent());
	}

	
	// -------------- COMPROBAR SI ESTA INSTALADO UN GOOGLE PLAY SERVICES VALIDO -----------------
	
	private boolean comprobarGooglePlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				//Dispositivo no soportado.
				finish();
			}
			return false;
		}
		return true;
	}

	
	// ----------------------- COMPRUEBA SI ESTA REGISTRADO ------------------------------
	
	private String dameIdRegistro(Context context) {
		final SharedPreferences preferencias = getSharedPreferences(
				ActividadPrincipal.class.getSimpleName(), Context.MODE_PRIVATE);
		String auxRegistroId = preferencias.getString("idRegistro", "");
		if (auxRegistroId.equals("")) {
			return "";
		}
		return auxRegistroId;
	}

	
	// ---------------------- CONECTAR EL DISPOSITIVO AL SERVIDOR --------------------------
	

	public void registrarUsuarioGCM(){
		idRegistro = dameIdRegistro(getApplicationContext());
		if (idRegistro.equals("")) {
			registrarGCM tareaRegistroGCM = new registrarGCM();
			tareaRegistroGCM.execute(null, null, null);
		} else {
			mostrarMensajeToast(this, "El dispositivo ya esta conectado.");
		}
	}


	private class registrarGCM extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... params) {
			String msg = "";
			Boolean registroWeb = false;
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
				}
				idRegistro = gcm.register(SENDER_ID);
				registroWeb = registrarDispositivoEnServidorWeb(getApplicationContext(),
						idRegistro);
				if (registroWeb == true) {
					guardarIdRegistro(getApplicationContext(), idRegistro);
					msg = "Dispositivo conectado con servidor";
				}
			} catch (IOException ex) {
				msg = "Error al conectar CGM:" + ex.getMessage();
			}
			return msg;
		}

		protected void onPostExecute(String message){
			mostrarMensajeToast(getApplicationContext(),message);
			estadoConexion();
		}
	}

	private Boolean registrarDispositivoEnServidorWeb(Context context, final String regId) {
		String serverUrl = SERVER_URL + "registrar.php";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("iddevice", regId));
		params.add(new BasicNameValuePair("idapp", SENDER_ID));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverUrl);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpclient.execute(httppost);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void guardarIdRegistro(Context context, String regId) {
		final SharedPreferences prefs = getSharedPreferences(
				ActividadPrincipal.class.getSimpleName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("idRegistro", regId);
		editor.commit();
	}


	// ---------------------- DESCONECTAR EL DISPOSITIVO DEL SERVIDOR --------------------------

	public void desregistrarUsuarioGCM() {
		desregistrarGCM tarea = new desregistrarGCM();
		tarea.execute(null, null, null);
	}

	private class desregistrarGCM extends AsyncTask<String, Integer, String> {
		protected String doInBackground(String... params) {
			Boolean desregistroWeb = false;
			String msg = "";
			String auxIdRegistro = "";
			try {
				auxIdRegistro = dameIdRegistro(getApplicationContext());
				if (auxIdRegistro.equals("")) {
					msg = "El dispositivo no conectado al servidor";
				} else {
					if (gcm == null) {
						gcm = GoogleCloudMessaging
								.getInstance(getApplicationContext());
					}
					gcm.unregister();
					desregistroWeb = desregistrarDispositivoEnServidorWeb(
							getApplicationContext(), auxIdRegistro);
					if (desregistroWeb == true) {
						guardarIdRegistro(getApplicationContext(), "");
						msg = "Dispositivo desconectado del servidor";
					} else {
						msg = "Error al desconectar el dispositivo en el servidor web.";
					}
				}
			} catch (IOException ex) {
				msg = "Error al desconectar:" + ex.getMessage();
			}
			return msg;
		}

		protected void onPostExecute(String message) {
			mostrarMensajeToast(getApplicationContext(), message);
			estadoConexion();
		}
	}

	private Boolean desregistrarDispositivoEnServidorWeb(Context context,
			final String regId) {
		String serverUrl = SERVER_URL + "desregistrar.php";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("iddevice", regId));
		params.add(new BasicNameValuePair("idapp", SENDER_ID));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverUrl);

		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpclient.execute(httppost);
			mostrarMensajeToast(context, "Desconectado del servidor web");
			return true;
		} catch (IOException e) {
			mostrarMensajeToast(context, "Error en el desregistro.");
			return false;
		}
	}


	// ---------------------- ENVIO MENSAJE DESDE MOVIL --------------------------


	public void enviarMensaje(String msg){
		mensaje = msg;
		//mensaje = et_mensaje.getText().toString();
		identificador = dameIdRegistro(getApplicationContext());
		envioMensajeGCM enviarMensajeGCM = new envioMensajeGCM();
		enviarMensajeGCM.execute(null, null, null);
	}

	/*public void enviarMensaje(View view){
			mensaje = et_mensaje.getText().toString();
			identificador = dameIdRegistro(getApplicationContext());
			   envioMensajeGCM enviarMensajeGCM = new envioMensajeGCM();
			   enviarMensajeGCM.execute(null, null, null);
		}*/


	private class envioMensajeGCM extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... params) {
			String msg = "";
			Boolean envio;
			try {
				envio = enviarMensajeaServidorWeb(getApplicationContext());//,
				//   mensaje);
				if (envio == true) {

					msg = "Envio realizado correctamente";

				}
			} catch (Exception ex) {
				msg = "No se ha podido realizar el envio" + ex.getMessage();
			}
			return msg;
		}

		protected void onPostExecute(String message){
			mostrarMensajeToast(getApplicationContext(),message);
			//et_mensaje.setText("");
		}
	}

	private Boolean enviarMensajeaServidorWeb(Context context) {

		String serverUrl = SERVER_URL + "notificar.php";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("idapp", SENDER_ID));
		params.add(new BasicNameValuePair("mensaje", mensaje));
		params.add(new BasicNameValuePair("funcion", funcion));
		params.add(new BasicNameValuePair("iddevice", identificador));
		params.add(new BasicNameValuePair("partida", numPartida));
		params.add(new BasicNameValuePair("nombre", nombreJ1));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverUrl);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpclient.execute(httppost);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	// ------------------- COMPROBAR SI ESTAMOS ONLINE --------------------

	public void estadoConexion() {
		String estadoCon = dameIdRegistro(getApplicationContext());
		if(!estadoCon.equalsIgnoreCase("")) {
			navegador.loadUrl("javascript:conectado()");
		}else {
			navegador.loadUrl("javascript:desconectado()");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			recibeMensaje(intent);
		}
	};

	
	// -------------- AQUI LLEGAN LOS INTENT CON LOS MENSAJES ----------------
	// Se reparten los mensajes tanto si llegan directamente con la aplicacion abierta o si lo hace desde la notificacion

	public void recibeMensaje(Intent intent) {

		if (intent.hasExtra("mensaje")) {
			Bundle extras = intent.getExtras();
			mensaje = extras.getString("mensaje");
			funcion = extras.getString("funcion");
			idRecibido = extras.getString("identDevice");
			numPartida = extras.getString("partida");
			remitente = extras.getString("remitente");
			
			//------- COMPOBAMOS QUE EL MENSAJE NO ES NUESTRO PARA MOSTRARLO-----------
			
			if (!idRecibido.equals(dameIdRegistro(getApplicationContext()))) mensajeRecibido(mensaje, remitente);
			
			//-------- AQUI DEFINIMOS LAS ORDENES QUE ENVIAMOS EN FUNCION DESDE EL SERVIDOR ------------------
			
			if (funcion.equals("cerrar000")) finish();
			
			getIntent().removeExtra("mensaje");
			getIntent().removeExtra("funcion");
			getIntent().removeExtra("identDevice");
			getIntent().removeExtra("partida");
			getIntent().removeExtra("remitente");
		}

	}

	//-------------------- MUESTRA MENSAJES RECIBIDOS ------------------------------------------------------

	public void mensajeRecibido(String mensaje, String remitente) {

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_email)
		.setTitle("Mensaje recibido de "+remitente)
		.setMessage(mensaje)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();    
			}
		})
		.show();
	}




	// ================================  JUEGO ONLINE ==============================
	// =============================================================================




	public void juegoOnline(String mensajeOnline) {

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_email)
		.setTitle("Peticion de Juego Online")
		.setMessage(mensajeOnline)
		.setPositiveButton("Si", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel(); 
				mensajeJugarOnline(nombreJ1+" quiere jugar una partida");
			}
		})
		.setNegativeButton("No", null)
		.show();
	}


	//---------------------- ENVIAR PETICION DE JUGAR ONLINE ---------------------

	public void mensajeJugarOnline(String msg) {
		Random r = new Random();
		numPartida =  Integer.toString(r.nextInt(10000));
		mensaje = msg;
		funcion = "INVITA";
		identificador = dameIdRegistro(getApplicationContext());
		envioMensajeGCM enviarMensajeGCM = new envioMensajeGCM();
		enviarMensajeGCM.execute(null, null, null);
	}

	public void peticionJugarOnline(String identificacion, String numPartida) {

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_email)
		.setTitle("Peticion de Partida OnLine")
		.setMessage("quieres jugar con Jugador1 a la partida num "+numPartida)
		.setPositiveButton("Si", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel(); 
				mensajeJugarOnline(nombreJ1+" quiere jugar una partida");
			}
		})
		.setNegativeButton("No", null)
		.show();
	}




}


