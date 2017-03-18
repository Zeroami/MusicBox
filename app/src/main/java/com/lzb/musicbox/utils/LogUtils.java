package com.lzb.musicbox.utils;

import android.util.Log;

public class LogUtils {

	private static final int VERBOSE = 1;
	private static final int DEBUG = 2;
	private static final int INFO = 3;
	private static final int WARN = 4;
	private static final int ERROR = 5;
	private static final int NOTHING = 6;
	
	private static final int LEVEL = VERBOSE;
	private static final String TAG = "tag";
	
	public static void v(int msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(long msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(short msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(float msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(double msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(char msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(boolean msg){
		if(VERBOSE >= LEVEL){
			Log.v(TAG, "" + msg);
		}
	}
	public static void v(String msg){
		if(VERBOSE >= LEVEL){
			print(msg);
		}
	}
	public static void v(Object msg){
		if(VERBOSE >= LEVEL){
			print(msg);
		}
	}
	
	public static void d(int msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(long msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(short msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(float msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(double msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(char msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(boolean msg){
		if(DEBUG >= LEVEL){
			Log.d(TAG, "" + msg);
		}
	}
	public static void d(String msg){
		if(DEBUG >= LEVEL){
			print(msg);
		}
	}
	public static void d(Object msg){
		if(DEBUG >= LEVEL){
			print(msg);
		}
	}
	
	public static void i(int msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(long msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(short msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(float msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(double msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(char msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(boolean msg){
		if(INFO >= LEVEL){
			Log.i(TAG, "" + msg);
		}
	}
	public static void i(String msg){
		if(INFO >= LEVEL){
			print(msg);
		}
	}
	public static void i(Object msg){
		if(INFO >= LEVEL){
			print(msg);
		}
	}
	
	public static void w(int msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(long msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(short msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(float msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(double msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(char msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(boolean msg){
		if(WARN >= LEVEL){
			Log.w(TAG, "" + msg);
		}
	}
	public static void w(String msg){
		if(WARN >= LEVEL){
			print(msg);
		}
	}
	public static void w(Object msg){
		if(WARN >= LEVEL){
			print(msg);
		}
	}
	
	public static void e(int msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(long msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(short msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(float msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(double msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(char msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(boolean msg){
		if(ERROR >= LEVEL){
			Log.e(TAG, "" + msg);
		}
	}
	public static void e(String msg){
		if(ERROR >= LEVEL){
			print(msg);
		}
	}
	public static void e(Object msg){
		if(ERROR >= LEVEL){
			print(msg);
		}
	}

	private static void print(String msg){
		if(msg == null){
			Log.i(TAG, "null");
		}else{
			Log.i(TAG, msg);
		}
	}

	private static void print(Object msg){
		if(msg == null){
			Log.i(TAG, "null");
		}else{
			Log.i(TAG, msg.toString());
		}
	}
}
