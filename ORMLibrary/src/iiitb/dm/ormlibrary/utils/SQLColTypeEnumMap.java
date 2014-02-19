package iiitb.dm.ormlibrary.utils;

import iiitb.dm.ormlibrary.constants.SQLColType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kempa
 * 
 */
public class SQLColTypeEnumMap
{
	private static Map<String, SQLColType> map = new HashMap<String, SQLColType>();

	static
	{
		map.put("String", SQLColType.TEXT);
		map.put("Float", SQLColType.REAL);
		map.put("Integer", SQLColType.INTEGER);
		map.put("Long", SQLColType.INTEGER);
		map.put("float", SQLColType.REAL);
		map.put("int", SQLColType.INTEGER);
		map.put("long", SQLColType.INTEGER);

	}

	public static SQLColType get(String name)
	{
		return map.get(name);
	}

}
