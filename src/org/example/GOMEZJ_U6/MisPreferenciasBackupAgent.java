package org.example.GOMEZJ_U6;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class MisPreferenciasBackupAgent extends BackupAgentHelper {
	   // El nombre del archivo SharedPreferences
	   static final String PREFS = "Preferencias";
	   // Una clave para identificar unívocamente el conjunto de copia de seguridad de los datos
	   static final String PREFS_BACKUP_KEY = "BackupPreferencias";
	   // Asignar un ayudante y agregarlo al agente de copia de seguridad.
	   @Override
	   public void onCreate() {
	      SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
	      addHelper(PREFS_BACKUP_KEY, helper);
	   }
	}
