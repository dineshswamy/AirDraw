package com.sonymobile.smartconnect.extension.sensorsample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    
    private static PaintingWindow compassView;
    


    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compassView = new PaintingWindow(this);
        setContentView(compassView);
     //   SmartWatchListener smWatch=new SmartWatchListener(this,"com.komparify.airdraw");
    }


    public static void updateDraw(float positionx ,float positiony,float positionz)
    {
    	Log.w("AirDraw","x "+positionx+" y "+positiony+" z "+positionz);
    	compassView.updateData(positionx ,positiony,positionz);
    	
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
    }

}