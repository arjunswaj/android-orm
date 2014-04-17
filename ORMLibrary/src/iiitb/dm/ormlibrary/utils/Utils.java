package iiitb.dm.ormlibrary.utils;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import android.content.pm.FeatureInfo;

public class Utils
{

	public static String getGetterMethodName(String field)
	{
		String methodName = "get" + field.substring(0, 1).toUpperCase()
				+ field.substring(1);
		return methodName;
	}

	public static String getSetterMethodName(String field)
	{
		String methodName = "set" + field.substring(0, 1).toUpperCase()
				+ field.substring(1);
		return methodName;
	}

	public static String getBooleanGetterMethodName(String field)
	{
		String methodName = "is" + field.substring(0, 1).toUpperCase()
				+ field.substring(1);
		return methodName;
	}

	/**
	 * Utility class : Returns a the 'Class' object of the specified class name
	 * 
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

	/*
	 * Goes up the inheritance tree of eoClassName, and finds the
	 * fieldTypeDetails of id field.
	 */
	public static FieldTypeDetails getFieldTypeDetailsOfId(String eoClassName)
	{
		Class<?> currentClass = null;
		try
		{
			currentClass = Class.forName(eoClassName);
		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FieldTypeDetails result = null;
		while (currentClass != Object.class)
		{
			ClassDetails classDetails = null;
			try
			{
				classDetails = AnnotationsScanner.getInstance()
						.getEntityObjectDetails(currentClass.getName());
			}
			catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((result = classDetails.getFieldTypeDetailsOfId()) != null)
				break;
			currentClass = currentClass.getSuperclass();
		}
		return result;
	}

	/*
	 * Goes up the inheritance tree of eoClassName, and find which class
	 * contains the id.
	 */
	public static String getClassOfId(String eoClassName)
	{
		Class<?> currentClass = null;
		try
		{
			currentClass = Class.forName(eoClassName);
		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String result = null;
		while (currentClass != Object.class)
		{
			ClassDetails classDetails = null;
			try
			{
				classDetails = AnnotationsScanner.getInstance()
						.getEntityObjectDetails(currentClass.getName());
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			if ((classDetails.getFieldTypeDetailsOfId()) != null)
			{
				result = currentClass.getName();
				break;
			}
			currentClass = currentClass.getSuperclass();
		}
		return result;
	}

	/*
	 * Get type of collection named variableName in class className
	 */
	public static String getCollectionType(String className, String variableName)
	{
		String result = null;
		ClassDetails classDetails = null;
		try
		{
			classDetails = AnnotationsScanner.getInstance()
					.getEntityObjectDetails(className);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		ParameterizedType genericType = null;
		try
		{
			genericType = (ParameterizedType) Class
					.forName(className)
					.getDeclaredField(
							classDetails.getFieldTypeDetailsByFieldName(
									variableName).getFieldName())
					.getGenericType();
			result = ((Class<?>) genericType.getActualTypeArguments()[0])
					.getName();
		}
		catch (ClassCastException ex)
		{

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public static Object getMemberObject(Object entityObj, String fieldName)
	{
		try
		{
			Method getterMethod = entityObj.getClass().getMethod(
					Utils.getGetterMethodName(fieldName));
			return getterMethod.invoke(entityObj);
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return the join column name of the join table corresponding to the
	 * specified association field in the table corresponding to the given
	 * owning sidClassdetails object
	 * 
	 * @param owningSide
	 * @param fieldTypeDetail
	 * @param inverseSide
	 * @return
	 */
	public static String getJoinColumnName(ClassDetails owningSide,
			FieldTypeDetails fieldTypeDetail, ClassDetails inverseSide)
	{
		// joinColumnName is different if there is a reverse mapping
		FieldTypeDetails joinColumnFieldTypeDetails = inverseSide
				.getFieldTypeDetailsByMappedByAnnotation(fieldTypeDetail
						.getFieldName());

		String joinColumnName = null;
		if (joinColumnFieldTypeDetails == null)
			joinColumnName = (String) owningSide.getAnnotationOptionValues()
					.get(Constants.ENTITY).get(Constants.NAME);
		else
			joinColumnName = joinColumnFieldTypeDetails.getFieldName();
		joinColumnName += "_"
				+ getFieldTypeDetailsOfId(owningSide.getClassName())
						.getAnnotationOptionValues().get(Constants.COLUMN)
						.get(Constants.NAME);
		return joinColumnName;
	}

	public static String getJoinTableName(ClassDetails owningSide,
			ClassDetails inverseSide)
	{
		return owningSide.getAnnotationOptionValues().get(Constants.ENTITY)
				.get(Constants.NAME)
				+ "_"
				+ inverseSide.getAnnotationOptionValues().get(Constants.ENTITY)
						.get(Constants.NAME);
	}
	
	public static String getInverseJoinColumnName(ClassDetails inverseSide, FieldTypeDetails fieldTypeDetails)
	{
        return fieldTypeDetails.getFieldName()
	              + "_"
	              + getFieldTypeDetailsOfId(inverseSide.getClassName())
	                  .getAnnotationOptionValues().get(Constants.COLUMN)
	                  .get(Constants.NAME);
	}
}
