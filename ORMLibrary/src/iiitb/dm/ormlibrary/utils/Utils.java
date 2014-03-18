package iiitb.dm.ormlibrary.utils;

import android.util.Log;

public class Utils {
	
	public static String getGetterMethodName(String field)
	{
		String methodName = "get" + field.substring(0,1).toUpperCase() + field.substring(1);
		Log.d("Utils", "Method name is:" + methodName);
		return methodName;
	}

	public static String getBooleanGetterMethodName(String field)
	{
		String methodName = "is" + field.substring(0,1).toUpperCase() + field.substring(1);
		Log.d("Utils", "Method name is:" + methodName);
		return methodName;
	}

}
