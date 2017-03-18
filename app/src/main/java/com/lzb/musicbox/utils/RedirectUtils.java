package com.lzb.musicbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RedirectUtils {

	public static void redirect(Context context,Class<?> target,boolean isFinish){
		Intent intent = new Intent(context,target);
		context.startActivity(intent);
		if(isFinish){
			((Activity)context).finish();
		}
	}
	
	public static void redirect(Context context,Class<?> target,Bundle bundle,boolean isFinish){
		Intent intent = new Intent(context,target);
		if(bundle != null){
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
		if(isFinish){
			((Activity)context).finish();
		}
	}
}
