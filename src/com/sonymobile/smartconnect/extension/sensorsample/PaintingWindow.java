package com.sonymobile.smartconnect.extension.sensorsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dinesh on 24/11/13.
 */
public class PaintingWindow extends View {

    private Paint paint;
    private float positionx=0,positiony=0,positionz=0;
    int start,end;
    float cxPoint,cyPoint,fx,fy,pathX,pathY;

    Path line_path;
    public PaintingWindow(Context context) {
        super(context);
        initialize();
        cxPoint = getMeasuredWidth() / 2;
        cyPoint = getMeasuredHeight() / 2;
        line_path=new Path();
        line_path.moveTo(cxPoint,cyPoint);
        line_path.lineTo(cxPoint,cyPoint);



    }

    public void initialize()
    {
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setTextSize(25);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
    }
    public  void printLog(float x,String name_x)
    {
        Log.w("Hackathon",name_x +" "+x );
    }

    @Override
    protected void onDraw(Canvas canvas) {


        canvas.drawPath(line_path,paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

    cxPoint=event.getX();
    cyPoint=event.getY();

    line_path.moveTo(event.getX(),event.getY());
    line_path.lineTo(event.getX(),event.getY());

       pathX=cxPoint;
       pathY=cyPoint;
//
//        printLog(fx,"FX ");
//        printLog(fy,"FY ");
//        printLog(pathX,"pathX");
//        printLog(pathY,"pathY");
//        printLog(positionx,"positionX");
//        printLog(positiony,"positionY");


    return true;
    }



    public  void updateData(float positionx ,float positiony,float positionz) {

//

    this.positionx=positionx;
    this.positiony=positiony;


     fx  = getMeasuredWidth()/10;
     fy = getMeasuredHeight()/10;
        pathX=pathX+2*positionx;
        pathY=pathY+2*positiony;
//
//        pathX=2*positionx+getMeasuredWidth()/2;
//       pathY=2*positionx+getMeasuredHeight()/2;

        line_path.lineTo(pathX,pathY);
        line_path.moveTo(pathX, pathY);

        invalidate();
    }

}
