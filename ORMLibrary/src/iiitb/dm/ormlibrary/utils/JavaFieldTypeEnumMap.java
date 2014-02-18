package iiitb.dm.ormlibrary.utils;

import iiitb.dm.ormlibrary.constants.JavaFieldType;

import java.util.HashMap;
import java.util.Map;

public class JavaFieldTypeEnumMap {
	
	private static Map<String, JavaFieldType> map = new HashMap<String, JavaFieldType>();
	
	static {
		map.put("java.lang.String", JavaFieldType.STRING);
		map.put("java.lang.Float", JavaFieldType.FLOAT);
		map.put("java.lang.Integer", JavaFieldType.INTEGER);
		map.put("java.lang.Long", JavaFieldType.LONG);
		map.put("float", JavaFieldType.FLOAT);
		map.put("int", JavaFieldType.INTEGER);
		map.put("long", JavaFieldType.LONG);
		
	}
	
	public static JavaFieldType get(String name)
	{
		return map.get(name);
	}

}
