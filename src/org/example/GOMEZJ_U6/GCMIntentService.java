package org.example.GOMEZJ_U6;

import static org.example.GOMEZJ_U6.UtilidadesGCM.*;

import java.util.Random;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {
	
	public GCMIntentService() {
		   super("GCMIntentService");
		}
		 
		protected void onHandleIntent(Intent intent) {
		   String mensaje="";
		   String funcion = "";
		   String identDevice = "";
		   String numPartida = "";
		   String remitente = "";
		   Bundle extras = intent.getExtras();
		   GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		   String tipoMensaje = gcm.getMessageType(intent);
		   if (!extras.isEmpty()) {
		      if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(tipoMensaje)) {
		      mensaje="Error: " + extras.toString();
		      } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
		                   .equals(tipoMensaje)) {
		         mensaje="Se han borrado mensajes en el servidor: "+ extras.toString();
		      } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
		            .equals(tipoMensaje)) {
		         mensaje = extras.getString("mensaje");
		         funcion = extras.getString("funcion");
		         identDevice = extras.getString("identDevice");
		         numPartida = extras.getString("partida");
		         remitente = extras.getString("nombreJ1");
		      }
		      
		      if (actividadAbierta){
		    	  mostrarMensaje(getApplicationContext(),mensaje, funcion, identDevice, numPartida, remitente);
		    	  
		      }else {
		    	  mostrarAvisoBarraEstado(getApplicationContext(),mensaje,funcion,  identDevice, numPartida, remitente);
		      }
		   }
		   GCMBroadcastReceiver.completeWakefulIntent(intent);
		}
		
	
		public static void mostrarAvisoBarraEstado(Context context, String message, String funcion, String identDevice, String numPartida, String remitente) { 
			Intent notificationIntent = new Intent(context.getApplicationContext(), ActividadPrincipal.class);
			notificationIntent.putExtra("mensaje", message);
			notificationIntent.putExtra("funcion", funcion);
			notificationIntent.putExtra("identDevice", identDevice);
			notificationIntent.putExtra("partida", numPartida);
			notificationIntent.putExtra("remitente", remitente);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

			Random r = new Random();
			PendingIntent intent = PendingIntent.getActivity(context, r.nextInt(),notificationIntent, 0);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			long[] vibrate = {0,100,200,300};
			//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.tresraya);
			Notification notification = new NotificationCompat.Builder(context)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(message+"........Enviado por "+remitente)
				.setVibrate(vibrate)
				.setSmallIcon(R.drawable.tresrayaico)
				.setContentIntent(intent)
				.setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) //Sonido y vibración
				.setAutoCancel(true)
				.build();
			notificationManager.notify(0, notification);
		}
		

}
