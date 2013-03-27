package com.example.adctrl;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.support.v4.app.NavUtils;

public class ADCtrl extends Activity implements Runnable, SensorEventListener {

	final int S_num = 51;
	QuickSort sorter = new QuickSort();

	int Tcnt = 0;
	float[] Gyro2 = new float[S_num]; 
	float[] Dir2 = new float[S_num]; 
	
	private static final String TAG = "ArduinoAccessory";
	 
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
 
	private SensorManager sensorManager, sensorManager1;
	private Sensor Magnet, Gyro;
	private float x, y, z, gx, gy, gz, gx1, gy1, gz1, tmp_dir=0.0f, r_dir, m_dir, tmp_gx1 = 0f;
	float x1, y1, x2, y2;
	long cnt=0;

	Intent nView;

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	private ToggleButton buttonON, buttonLED, buttonPWR, buttonDIR, buttonGO, buttonL, buttonR;
	private TextView _TextView = null;
	private TextView _TextView2 = null;
	private Button Rp, Lp, Rm, Lm;
	private LinearLayout LL;
	
	int press_b = 0, push_ch = 0, distA = 0, distB = 0, tmpDist, totalDist = 0, get_stat = 0;

	long currentTime, lastTime;

	char tmp;
	int value, distR, distL, stat, Mangle;

	private TextView mResponseField = null, 
			mResponseField1 = null, 
			mResponseField2 = null, 
			mResponseField3 = null, 
			mDirect = null;
	private TextView curS = null, distR_U=null, distL_U = null;

	float Rspd = 0.0f, Lspd = 0.0f;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
 
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	nView = new Intent(this, MapGen.class);
    	 
		lastTime = System.currentTimeMillis(); 
		
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
 
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
 
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Magnet = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager1 = (SensorManager) getSystemService(SENSOR_SERVICE);
		Gyro = sensorManager1.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
        setContentView(R.layout.activity_adctrl);
        
        LL = (LinearLayout)findViewById(R.id.layoutM);

//		curS = null, distR_U=null, distL_U = null;
		curS = (TextView) findViewById(R.id.textStat);
		distL_U = (TextView) findViewById(R.id.textdistL);
		distR_U = (TextView) findViewById(R.id.textdistR);
		buttonON = (ToggleButton) findViewById(R.id.toggleON);
		buttonLED = (ToggleButton) findViewById(R.id.toggleButtonLED);
		buttonPWR = (ToggleButton) findViewById(R.id.DC_PWR);
		buttonDIR = (ToggleButton) findViewById(R.id.DC_DIR);
		buttonGO =  (ToggleButton) findViewById(R.id.toggleStart);
		buttonL = (ToggleButton) findViewById(R.id.togglelGo);
		buttonR =  (ToggleButton) findViewById(R.id.togglerGo);
/*
		_TextView = (TextView) findViewById(R.id.RW);
		_TextView2 = (TextView) findViewById(R.id.LW);
		Rp = (Button) findViewById(R.id.Rplus);
		Rm = (Button) findViewById(R.id.Rminus);
		Lp = (Button) findViewById(R.id.Lplus);
		Lm = (Button) findViewById(R.id.Lminus);
		
		Rp.setOnClickListener(on_init);
		Rm.setOnClickListener(on_init);
		Lp.setOnClickListener(on_init);
		Lm.setOnClickListener(on_init);
*/		
		LL.setOnTouchListener(new OnTouchListener(){
        	public boolean onTouch(View arg0, MotionEvent event) {               
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			x1=event.getX();
        			y1=event.getY();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                	x2=event.getX();
					y2=event.getY();
					
					if(x2 - x1 > 20)
						startActivity(nView);					
                }
 				return true;
        }});
/*		
		_TextView.setTextSize(30);
		_TextView2.setTextSize(30);
		
		 _TextView.setText(Float.toString(Rspd));
		 _TextView2.setText(Float.toString(Lspd));
*/		 
		 mDirect = (TextView)findViewById(R.id.textDirect);
		 mResponseField = (TextView)findViewById(R.id.textIn1);
		 mResponseField1 = (TextView)findViewById(R.id.textIn2);
		 mResponseField2 = (TextView)findViewById(R.id.textIn3);
		 mResponseField3 = (TextView)findViewById(R.id.textIn4);
		 
		 setupAccessory();
    }

	private View.OnClickListener on_init = new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.Rplus:
				Rspd = Rspd + 1;
				_TextView.setText(Float.toString(Rspd));
				break;
			case R.id.Rminus:
				Rspd = Rspd - 1;
				_TextView.setText(Float.toString(Rspd));
				break;
			case R.id.Lplus:
				Lspd = Lspd + 1;
				_TextView2.setText(Float.toString(Lspd));
				break;
			case R.id.Lminus:
				Lspd = Lspd - 1;
				_TextView2.setText(Float.toString(Lspd));
				break;
			}
		}
	};
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
 
	@Override
	public void onStart(){
    	super.onStart();
    	
    	if (Magnet != null) 
            sensorManager.registerListener(this, Magnet, 
//					SensorManager.SENSOR_DELAY_GAME);
//            		SensorManager.SENSOR_DELAY_NORMAL);
            		SensorManager.SENSOR_DELAY_FASTEST);
    	
    	if (Gyro != null) 
            sensorManager1.registerListener(this, Gyro, 
//					SensorManager.SENSOR_DELAY_GAME);
//            		SensorManager.SENSOR_DELAY_NORMAL);
            		SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
	public void onStop(){
    	super.onStop();
    	
    	if (sensorManager != null) 
            sensorManager.unregisterListener(this);
    	if (sensorManager1 != null) 
            sensorManager1.unregisterListener(this);
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) { 
    }    
	
	@Override
	public void onResume() {
		super.onResume();
 
		if (mInputStream != null && mOutputStream != null) {
			return;
		}
 
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}
 
	@Override
	public void onPause() {
		super.onPause();
//		closeAccessory();
	}
 
	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ValueMsg t = (ValueMsg) msg.obj;
			// this is where you handle the data you sent. You get it by calling the getReading() function
			switch(t.getFlag()){
			case 'A':
				get_stat = 0;
				mResponseField.setText("L_Motor: "+t.getFlag()+"\nL_Rot_num: "+t.getReading());
				distA = t.getReading();				// 이동 거리 계산
//				mResponseField.setText("Motor: "+t.getFlag()+"\nRot_num: "+t.getReading());
/*				
				switch(stat){
				case 1:
					distA = t.getReading();				// 이동 거리 계산
					break;
				case 4:
					distA = 0 - t.getReading();				// 이동 거리 계산
					break;
				default:
					distA = 0;				// 이동 거리 계산
					break;
				}
*/				
//				totalDist = t.getReading(); 
				break;
			case 'B':
				get_stat = 0;
//				mResponseField1.setText("Motor: "+t.getFlag()+"\nRot_num: "+t.getReading());
				mResponseField.setText("R_Motor: "+t.getFlag()+"\nR_Rot_num: "+t.getReading());
				distB=t.getReading();
				break;
			case 'C':
				get_stat = 0;
				mResponseField2.setText("Motor: "+t.getFlag()+"\nRot_num: "+t.getReading());
				break;
			case 'D':
				get_stat = 0;
				mResponseField3.setText("Motor: "+t.getFlag()+"\nRot_num: "+t.getReading());
				break;
			case 'U':
				distL_U.setText("dist_L: "+distL);
				distR_U.setText("                    dist_R: "+distR);
				break;
			case 'S':
				switch(stat){
				case 0:
					curS.setText("정지");
					push_ch = 0;
					break;
				case 1:
					curS.setText("전진");
					push_ch = 1;
					break;
				case 2:
					curS.setText("좌회전");
					push_ch = 0; 
					break;
				case 3:
					curS.setText("우회전");
					push_ch =0;
					break;
				case 4:
					curS.setText("후진");
					push_ch=0;
					break;
				case 5:
					curS.setText("왼쪽 이동");
					push_ch =0;
					break;
				case 6:
					curS.setText("오른쪽 이동");
					push_ch=0;
					break;
				}
				break;
			}
		}
	};
	
	private void setupAccessory() {
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent =PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
	}
 
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "OpenAccessoryTest");
			thread.start();
			Log.d(TAG, "accessory opened");
		} else {
			Log.d(TAG, "accessory open fail");
		}
	}
 
 
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
	
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
		
		while (true) { // read data
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}
 
			i = 0;
			while (i < ret) {
				int len = ret - i;
				
				switch(buffer[i]){
				case 0x1:
					if (len >= 5) {
						Message m = Message.obtain(mHandler);
						byte hi, lo;
						value = 0;
						tmp = 'N';
						
						switch(buffer[i+1]){
							case 0x0:
								tmp = 'A';
								hi = buffer[i+3];
								lo = buffer[i+4];
								value = hi & 0xff;
								value *= 256;
								value += lo & 0xff;
								break;
							case 0x1:
								tmp = 'B';
								hi = buffer[i+3];
								lo = buffer[i+4];
								value = hi & 0xff;
								value *= 256;
								value += lo & 0xff;
								break;
							case 0x2:
								tmp = 'C';
								hi = buffer[i+3];
								lo = buffer[i+4];
								value = hi & 0xff;
								value *= 256;
								value += lo & 0xff;
								break;
							case 0x3:
								tmp = 'D';
								hi = buffer[i+3];
								lo = buffer[i+4];
								value = hi & 0xff;
								value *= 256;
								value += lo & 0xff;
								break;
							case 0x4:
								tmp = 'U';
								hi = buffer[i+3];
								lo = buffer[i+4];
								distL = hi & 0xff;
								distL *= 256;
								distL += lo & 0xff; 
								break;
							case 0x5:
								tmp = 'U';
								hi = buffer[i+3];
								lo = buffer[i+4];
								distR = hi & 0xff;
								distR *= 256;
								distR += lo & 0xff;
								break;
							case 0x6:
								tmp = 'S';
								hi = buffer[i+3];
//								lo = buffer[i+3];
								stat = hi & 0xff;
								break;
							case 0x7:
								tmp = 'T';
								hi = buffer[i+3];
								lo = buffer[i+4];
								Mangle = hi & 0xff;
								Mangle *= 256;
								Mangle += lo & 0xff;
								break;
						}
						// 'f' is the flag, use for your own logic
						// value is the value from the arduino
						m.obj = new ValueMsg(tmp, value);
						mHandler.sendMessage(m);
					}
					i += 5; // number of bytes sent from arduino
					break;
					
				default:
					Log.d(TAG, "unknown msg: " + buffer[i]);
					i = len;
					break;
				}
				
				currentTime = System.currentTimeMillis();
				
//				if(push_ch == 1 && currentTime - lastTime > 100 && distR - distL > 10){
				if(push_ch == 1 && currentTime - lastTime > 100){
//					setV(30f, 0f);
					setV(Rspd, Lspd);
					lastTime = currentTime;
				}
			}
 
		}
	}

	public void FrontGo(View v){
		byte[] buffer = new byte[5];
		
		if(buttonON.isChecked()){
//			push_ch = 1;
			
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x0; 
			buffer[2]=(byte)0; // button says on, light is off
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
//			push_ch = 0;
			
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x0;
			buffer[2]=(byte)1; // button says off, light is on
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	
	public void RearGo(View v){
		byte[] buffer = new byte[5];	//command, target, value
 		
		if(buttonPWR.isChecked()){
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x1; 
			buffer[2]=(byte)0; // button says on, PWR is off
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x1;
			buffer[2]=(byte)1; // button says off, PWR is on
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	public void LeftTurn(View v){
		
		byte[] buffer = new byte[5];	//command, target, value
 
		if(buttonLED.isChecked()){
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x2; 
			buffer[2]=(byte)0; // button says on, light is off
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x2;
			buffer[2]=(byte)1; // button says off, light is on
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	public void RightTurn(View v){
		 
		byte[] buffer = new byte[5];	//command, target, value
 		
		if(buttonDIR.isChecked()){
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x3; 
			buffer[2]=(byte)0; // button says on, DIR is front
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x3;
			buffer[2]=(byte)1; // button says off, DIR is back
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	public void LeftGo(View v){
		 
		byte[] buffer = new byte[5];	//command, target, value
 
		if(buttonL.isChecked()){
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x6; 
			buffer[2]=(byte)0; // button says on, light is off
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x6;
			buffer[2]=(byte)1; // button says off, light is on
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	public void RightGo(View v){
		 
		byte[] buffer = new byte[5];	//command, target, value
 		
		if(buttonR.isChecked()){
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x7; 
			buffer[2]=(byte)0; // button says on, DIR is front
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}else{
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x7;
			buffer[2]=(byte)1; // button says off, DIR is back
			buffer[3]=(byte)Rspd;
			buffer[4]=(byte)Lspd;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	public void rStart(View v){
		byte[] buffer = new byte[5];	//command, target, value
 
		if(buttonGO.isChecked()){
			press_b = 1;
			
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x5; 
			buffer[2]=(byte)0; // button says on, light is off
			buffer[3]=(byte)0;
			buffer[4]=(byte)0;
		}else{
			press_b = 0;
			
			buffer[0]=(byte)0x2; 
			buffer[1]=(byte)0x5;
			buffer[2]=(byte)1; // button says off, light is on
			buffer[3]=(byte)0;
			buffer[4]=(byte)0;
		}
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	public void onSensorChanged(SensorEvent event) {
		
//나침반 센서
//*		
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			gx = event.values[SensorManager.DATA_X];
			gy = event.values[SensorManager.DATA_Y];
			gz = event.values[SensorManager.DATA_Z];
		} 
		
		Tcnt = 0;

		while(Tcnt < S_num){
			Dir2[Tcnt] = 0;
			Tcnt++;
		}
		
		Tcnt = 0;
		
		while(Tcnt < S_num){
			Dir2[Tcnt] = (int)(gx*100)/100;
			Tcnt++;
		}
		
		sorter.sort(Dir2, S_num);
//*/
		
// Gyro 센서 
/*		
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			gx1 = event.values[0];
			gy1 = event.values[1];
			gz1 = event.values[2];
		} 
		
		Tcnt = 0;

		while(Tcnt < S_num){
			Gyro2[Tcnt] = 0;
			Tcnt++;
		}
		
		Tcnt = 0;
		
		while(Tcnt < S_num){
			Gyro2[Tcnt] = gz1;
			Tcnt++;
		}
		
		sorter.sort(Gyro2, S_num);
		
//		Float.parseFloat(_et.getText().toString()+"f");
//		_TextView.setText(weather);

		tmp_gx1 += (int)(Gyro2[S_num/2]*100)/100;
*/		
//*
		r_dir = Dir2[S_num/2] - tmp_dir;

// Gyro 센서값 표시		
//		mDirect.setText( String.format( "Direction\nx: %.2f\n\nGyro\nx: %.2f\nGyro(mod)\nx: %.2f\n\n", Dir2[S_num/2], gz1, tmp_gx1) );
		mDirect.setText( String.format( "Direction\nx: %.2f\n", Dir2[S_num/2]) );

		String ch_op;
		if(	press_b == 1 && Math.abs(distA) > 0 ){
			
			if(r_dir < -180)
				m_dir = r_dir+360;
			else if(r_dir > 180)
				m_dir = r_dir-360;
			else 
				m_dir = r_dir;
			
			tmp_dir = Dir2[S_num/2];
			
//			cnt++;
/*			
			if(distA < 0){
				if(180 + m_dir > 180)
					ch_op ="<dir3=" + Mangle + ", dir2=" + tmp_gx1 + ", dir=" + (m_dir - 180) + ", dist=" + (distA*(-1)) + "/>\n";
				else
					ch_op ="<dir3=" + Mangle + ", dir2=" + tmp_gx1 + ", dir=" + (180 - m_dir) + ", dist=" + (distA*(-1)) + "/>\n";
			}else
				ch_op ="<dir3=" + Mangle + ", dir2=" + tmp_gx1 + ", dir=" + m_dir + ", dist=" + distA + "/>\n";
*/
			ch_op = "A:"+distA+" B:"+distB+" L:"+ distL+" R:"+distR+" dir:" + m_dir +"\n";
			
			try{
				FileOutputStream out = openFileOutput("op1.txt",Context.MODE_APPEND);
				out.write(ch_op.getBytes());
				out.close();
			}
			catch (IOException ioe){
				System.out.print("Can't Write");
			}
		}
		//*/
	}

	public void setV(float Rspd, float Lspd){

		byte[] buffer = new byte[5];
 
		buffer[0]=(byte)0x2; 
		buffer[1]=(byte)0x4; 
		buffer[2]=(byte)0; 
		buffer[3]=(byte)Rspd;
		buffer[4]=(byte)Lspd;
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
    
}
