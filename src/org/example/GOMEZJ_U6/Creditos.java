package org.example.GOMEZJ_U6;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Creditos extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creditos);

	}

}
