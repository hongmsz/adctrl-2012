package com.example.adctrl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class MapGen extends Activity {

	private LinearLayout contentsContatiner;
	testView test;
	
	FileInputStream in;
	FileOutputStream out;
	String op_out="";
	
	float x1, y1, x2, y2;
	
	float posX = 360, posY = 640;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
				
		LinearLayout layout = new LinearLayout(this);
		
		createContentsContainer();       
		layout.addView(contentsContatiner);
        
        setContentView(layout);

	}
	
	private void createContentsContainer(){
		contentsContatiner = new LinearLayout(this);    	
    	test = new testView(this);    	
    	contentsContatiner.addView(test);
    	
    	test.setOnTouchListener(new OnTouchListener(){
        	public boolean onTouch(View arg0, MotionEvent event) {               
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			x1=event.getX();
        			y1=event.getY();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                	x2=event.getX();
					y2=event.getY();
					
					if(x1 - x2 > 20)
						finish();
                }
 				return true;
        }});
    	
	}

	public class testView extends View {
		
		Paint pnt = new Paint();
		
		public testView(Context context)
    	{
    		super(context);
    	}
    	public testView(Context context, AttributeSet attrs) {
    		super(context, attrs);
    	}
    	public void onDraw(Canvas canvas){
    		pnt.setColor(0xffA90000);//red
    		canvas.drawCircle(posX,posY,10,pnt);     		
    		
		}
	}
	
	public void run() {
	     while(true) 
	     {

	    	 try{
	    			in = openFileInput("op1.txt");
//	    			rd = new InputStreamReader(in);
	    			byte[] data = new byte[in.available()];
	    			while(in.read(data) != -1){
	    		    	in.close();
	    				op_out = new String(data);
	    			}
	    		}catch(FileNotFoundException e){
	    			try{
	    				out = openFileOutput("op1.txt",Context.MODE_WORLD_READABLE);
	    				out.write(op_out.getBytes());
	    				out.close();
	    			}
	    			catch (IOException ioe){
	    				System.out.print("Can't Write");
	    			}
	    		}catch (IOException e) {
	    			e.printStackTrace();
	    		}

			 test.postInvalidate();
	     }
	  }
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			if(keyCode == KeyEvent.KEYCODE_BACK){
				finish();
			}
		}
		return false;
	}
	
}
