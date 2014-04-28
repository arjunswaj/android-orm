package iiitb.dm.ormlibrary.query.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;
import android.util.Log;

public class ObjectFiller {

	private AnnotationsScanner annotationsScanner = AnnotationsScanner.getInstance();
	private List<Object> objectList = new ArrayList<Object>();
	private List<Object> backEdgeInfo = new ArrayList<Object>();
	private List<ColumnField> columnFieldList;
	private String entityOrClassName;
	private Cursor cursor;

	public ObjectFiller(List<ColumnField> columnFieldList, String entityOrClassName, Cursor cursor)
	{
		this.columnFieldList = columnFieldList;
		this.entityOrClassName = entityOrClassName;
		this.cursor = cursor;
		Class<?> eoClass = null;
		try {
			eoClass = Class.forName(entityOrClassName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Parse the result and set the fields in the created objects.
		if (cursor.moveToFirst()) {
			Object eo = null;
			try{
				do {
					// Check if the object with the same id is not already there in result collection
					if((eo = findObjectInCollection(objectList, entityOrClassName, 
							cursor.getLong(cursor.getColumnIndex(findColumn(Utils.getClassOfId(entityOrClassName),
									Utils.getFieldTypeDetailsOfId(entityOrClassName).getFieldName()))),
									Utils.getFieldTypeDetailsOfId(entityOrClassName).getFieldName())) == null)
					{
						eo = Utils.getClassObject(entityOrClassName).newInstance();
						Log.v("List", " Created a new object of " + entityOrClassName);
						objectList.add(eo);
					}
					fillObject(eo, eoClass, cursor);
				} while (cursor.moveToNext());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			cursor.close();
		}
	}


	public List<Object> getObjects(){
		return objectList;
	}



	/*
	 * Fills the object with the current row in the cursor. 
	 * Calls itself to fill the associated objects.
	 * Uses columnFieldList to map between columns and variables.
	 */
	private void fillObject(Object object, Class<?> objectType, Cursor cursor)
	{
		fillId(object, objectType);
		while(objectType != Object.class)
		{
			ClassDetails classDetails = null;
			classDetails = annotationsScanner.getEntityObjectDetails(objectType.getName());

			Log.v("fillObject", "Filling Object of " + objectType.getName() + " with id " + cursor.getString(cursor.getColumnIndex(findColumn(Utils.getClassOfId(classDetails.getClassName()),
					Utils.getFieldTypeDetailsOfId(classDetails.getClassName()).getFieldName()))));
			
			for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails())
			{
				String fieldType = fieldTypeDetails.getFieldType().getSimpleName();
				String colName = findColumn(classDetails.getClassName(), fieldTypeDetails.getFieldName());
				String setterMethodName = Utils.getSetterMethodName(fieldTypeDetails.getFieldName());
				try{
					if (fieldType.equals("String")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								String.class);
						String args = cursor.getString(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					} else if (fieldType.equals("Float")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								Float.class);
						float args = cursor.getFloat(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					} else if (fieldType.equals("float")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								float.class);
						float args = cursor.getFloat(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					} else if (fieldType.equals("Integer")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								Integer.class);
						int args = cursor.getInt(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);;
					} else if (fieldType.equals("int")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								int.class);
						int args = cursor.getInt(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					} else if (fieldType.equals("Long")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								Long.class);
						long args = cursor.getLong(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					} else if (fieldType.equals("long")) {
						Method setterMethod = objectType.getMethod(setterMethodName,
								long.class);
						long args = cursor.getLong(cursor.getColumnIndex(colName));
						setterMethod.invoke(object, args);
					}
					else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_MANY) != null
							|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null){

						String getterMethodName = Utils.getGetterMethodName(fieldTypeDetails.getFieldName());
						Method getterMethod = objectType.getMethod(getterMethodName);
						Object collectionObj = getterMethod.invoke(object);
						Object obj;

						if(collectionObj == null)
						{

							// Create collection object
							collectionObj = Class.forName("java.util.ArrayList").newInstance();

							// set collection object
							setterMethodName = Utils.getSetterMethodName(fieldTypeDetails.getFieldName());
							Method setterMethod = objectType.getMethod(setterMethodName, Class.forName("java.util.Collection"));
							setterMethod.invoke(object, collectionObj);

							// Create inner type object.
							obj = Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).newInstance();


							// Add inner type object to collection object.
							Method addToCollectionMethod = fieldTypeDetails.getFieldType().getMethod("add", Object.class);							

							backEdgeInfo.add(obj);
							// Fill inner type object
							Log.v("fillObject", "Created a collection of " + obj.getClass().getName());
							Object tempObj;
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null) {
								fillObject(obj,Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
								addToCollectionMethod.invoke(collectionObj, obj);
							}
							else {
								obj = tempObj;
							}
							backEdgeInfo.remove(obj);

						}
						// Check if the object with same id is already there in collection, if yes, fill it.
						else if((obj = findObjectInCollection(collectionObj, 
								Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()),
								cursor.getLong(cursor.getColumnIndex(findColumn(Utils.getClassOfId(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())),
										Utils.getFieldTypeDetailsOfId(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).getFieldName()))),
										Utils.getFieldTypeDetailsOfId(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).getFieldName()
								)) != null){
							Log.v("fillObject", "Object already exists");

							backEdgeInfo.add(obj);
							Object tempObj;
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null)
								fillObject(obj, Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
							else 
								obj = tempObj;
							backEdgeInfo.remove(obj);
						}
						// other wise, create object and fill it.
						else{
							// Create inner type object.
							obj = Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).newInstance();

							// Add inner type object to collection object.
							Method addToCollectionMethod = fieldTypeDetails.getFieldType().getMethod("add", Object.class);
							addToCollectionMethod.invoke(collectionObj, obj);

							backEdgeInfo.add(obj);
							Object tempObj;
							// Fill inner type object
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null)
								fillObject(obj,Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
							else 
								obj = tempObj;
							backEdgeInfo.remove(obj);

						}

					}
					else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null
							|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null){
						String getterMethodName = Utils.getGetterMethodName(fieldTypeDetails.getFieldName());
						Method getterMethod = objectType.getMethod(getterMethodName);
						Object obj = getterMethod.invoke(object);
						Object tempObj;
						if(obj != null){
							backEdgeInfo.add(obj);
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, false)) == null)
								fillObject(obj, fieldTypeDetails.getFieldType(), cursor);
							else 
								obj = tempObj;
							backEdgeInfo.remove(obj);
						}
						else 
						{
							setterMethodName = Utils.getSetterMethodName(fieldTypeDetails.getFieldName());
							Method setterMethod = objectType.getMethod(setterMethodName, fieldTypeDetails.getFieldType());
							obj = fieldTypeDetails.getFieldType().newInstance();
							setterMethod.invoke(object, obj);
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, false)) == null)
								fillObject(obj, fieldTypeDetails.getFieldType(), cursor);
							else 
								obj = tempObj;
						}

					}
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
			objectType = objectType.getSuperclass();

		}
	}
	
	
	public void fillId(Object object, Class<?> objectType)
	{
		String setterMethodName = Utils.getSetterMethodName(Utils.getFieldTypeDetailsOfId(objectType.getName()).getFieldName());
		Method setterMethod = null;
		long id = cursor.getLong(cursor.getColumnIndex(findColumn( Utils.getClassOfId(objectType.getName()), Utils.getFieldTypeDetailsOfId(objectType.getName()).getFieldName())));
		try {
			try {
				setterMethod = Class.forName(Utils.getClassOfId(objectType.getName())).getMethod(setterMethodName,
						long.class);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			setterMethod.invoke(object, id);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	/*
	 * Find column name in columnFieldList, from class name and variable name and return it.
	 * Used while filling objects.
	 */
	public String findColumn(String className, String variableName)
	{
		for(ColumnField colField: columnFieldList)
		{
			if(colField.getClassName().equals(className) && colField.getFieldName().equals(variableName))
				return colField.getColumnName();
		}
		return null;
	}


	/*
	 * Find an object in the given collection object
	 * @param object collection object to search.
	 * @param collectionTypeName The inner type of collection, the type of object to search.
	 * @param id Id of object to be searched.
	 * @idField The name of id field in class collectionTypeName, used to find getter method of id.
	 */
	private Object findObjectInCollection(Object object, String collectionTypeName, Long id, String idField) {
		Collection collectionObject = (Collection)object;
		Iterator iterator = collectionObject.iterator();
		Class<?> collectionType = null;
		try {
			collectionType = Class.forName(collectionTypeName);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String getterMethodName = Utils.getGetterMethodName(idField);
		Method getterMethod = null;
		try {
			getterMethod = collectionType.getMethod(getterMethodName);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(iterator.hasNext())
		{
			Object obj = iterator.next();
			Long objId = null;
			try {
				objId = (Long)getterMethod.invoke(obj);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(objId != null && objId.equals(id))
				return obj;

		}
		return null;

	}

	/*
	 * Check if there is a back reference to an object of type TYPE and id ID.
	 * Determines this by scanning through the set backEdgeInfo, which is list of objects traversed till now.
	 */
	private Object checkBackEdge(ClassDetails classDetails, FieldTypeDetails fieldTypeDetails, Cursor cursor, boolean collection) {

		Class<?> type = null;
		long id = 0;
		
		// Determine the type and id of object to be found.
		try{
			if(collection == true){
				type = Class.forName(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()));
				id = cursor.getLong(cursor.getColumnIndex(findColumn(Utils.getClassOfId(Utils.getCollectionType(classDetails.getClassName(),
						fieldTypeDetails.getFieldName())),
						Utils.getFieldTypeDetailsOfId(Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()))
						.getFieldName())));
			}else{
				type = fieldTypeDetails.getFieldType();
				id = cursor.getLong(cursor.getColumnIndex(
						findColumn(Utils.getClassOfId(fieldTypeDetails.getFieldType().getName()), 
								Utils.getFieldTypeDetailsOfId(fieldTypeDetails.getFieldType().getName()).getFieldName())));

			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		for(Object obj : backEdgeInfo)
		{
			if(obj.getClass().getName().equals(type.getName()))
			{
				String getterMethodName = Utils.getGetterMethodName(Utils.getFieldTypeDetailsOfId(obj.getClass().getName()).getFieldName());
				Method getterMethod;
				long objId = -1L;
				try {
					getterMethod = type.getMethod(getterMethodName);
					objId = (Long) getterMethod.invoke(obj);
				} 
				catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(objId == id)
					return obj;
			}
		}
		return null;
	}



}
