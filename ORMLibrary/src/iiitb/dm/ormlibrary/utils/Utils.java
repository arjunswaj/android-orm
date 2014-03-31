package iiitb.dm.ormlibrary.utils;

import android.util.Log;

public class Utils {
	
	public static String getGetterMethodName(String field)
	{
		String methodName = "get" + field.substring(0,1).toUpperCase() + field.substring(1);
		Log.v("Utils", "Method name is:" + methodName);
		return methodName;
	}
	
	public static String getSetterMethodName(String field)
  {
    String methodName = "set" + field.substring(0,1).toUpperCase() + field.substring(1);
    Log.v("Utils", "Method name is:" + methodName);
    return methodName;
  }

	public static String getBooleanGetterMethodName(String field)
	{
		String methodName = "is" + field.substring(0,1).toUpperCase() + field.substring(1);
		Log.v("Utils", "Method name is:" + methodName);
		return methodName;
	}
	
	/**
	 * Utility class : Returns a the 'Class' object of the specified class name
	 * @param className
	 * @return
	 */
	public static Class<?> getClassObject(String className)
	{
		Class<?> _class = null;
		try
		{
			_class = Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return _class;
	}

}
