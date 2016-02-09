package org.example.GOMEZJ_U6;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public final class UtilidadesGCM {	
	
		//static public String funcion = null;
		static public boolean actividadAbierta = false;

		static final String SERVER_URL = "http://javi-gomez.com/AppAndroid/";
	   /*Identificador del proyecto en Google Console que usa el servicio GCM. [TIENES SUSTITUIRLO POR EL TUYO]
	   */
	   static final String SENDER_ID = "698797460132";
	   static final String DISPLAY_MESSAGE_ACTION = "org.example.GOMEZJ_U6.DISPLAY_MESSAGE";
	   
	   static void mostrarMensaje(Context context,String mensaje, String funcion, String identDevice, String numPartida, String enviadoPor) {
		   
		   Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		   intent.putExtra("mensaje", mensaje);
		   intent.putExtra("funcion", funcion);
		   intent.putExtra("identDevice", identDevice);
		   intent.putExtra("partida", numPartida);
		   intent.putExtra("remitente", enviadoPor);
		   context.sendBroadcast(intent);
		}

	   
	   private static Handler manejador = new Handler();
	   static void mostrarMensajeToast(final Context context, final String mensaje) {
	      manejador.post(new Runnable() {
	         public void run() {
	            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
	         }
	      });
	   }
	   
	   
}
