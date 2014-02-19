package iiitb.dm.ormlibrary.scanner.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

import iiitb.dm.ormlibrary.constants.JavaFieldType;
import iiitb.dm.ormlibrary.ddl.FieldValue;
import iiitb.dm.ormlibrary.scanner.ClassScanner;
import iiitb.dm.ormlibrary.scanner.ScanResult;
import iiitb.dm.ormlibrary.utils.JavaFieldTypeEnumMap;

import javax.persistence.*;

import android.util.Log;

public class ClassScannerImpl implements ClassScanner{
	
	public ScanResult scan(Class classObject)
	{
		ScanResult result = new ScanResult();
		
		
		Entity entityAnn = (Entity)classObject.getAnnotation(Entity.class);
		result.setTableName(entityAnn.name());
		
		
		List<FieldValue> fieldValues = new ArrayList<FieldValue>();
		for(Field field : classObject.getDeclaredFields())
		{
			Column annotation = field.getAnnotation(Column.class);	
			if(annotation != null)
			{
				FieldValue fieldValue = new FieldValue();
				fieldValue.setField(field);
				fieldValue.setFieldName(annotation.name());
				fieldValue.setJavaFieldType(JavaFieldTypeEnumMap.get(field.getType().getName()));
				if(field.getAnnotation(Id.class) != null)
					fieldValue.setId(true);
				fieldValues.add(fieldValue);
			}
		}
		result.setFieldValues(fieldValues);
		
		return result;
	}

}
