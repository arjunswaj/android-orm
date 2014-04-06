package iiitb.dm.ormlibrary.query.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.LogicalExpression;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.query.criterion.PropertyProjection;
import iiitb.dm.ormlibrary.query.criterion.SimpleExpression;
import iiitb.dm.ormlibrary.query.Projection;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.scanner.impl.AnnotationsScannerImpl;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import iiitb.dm.ormlibrary.utils.Utils;

/**
 * @author root
 *
 */
public class CriteriaImpl implements Criteria {

	private boolean distinct;
	private String table;
	private String[] columns;
	private String selection;
	private String[] selectionArgs;
	private List<String> selectionArgsList = new ArrayList<String>();
	private String groupBy;
	private String having;
	private String orderBy;
	private String limit;
	private ProjectionList projectionList;
	private AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();
	private Map<String, ClassDetails> mappingCache;

	// List of subcriteria 
	private List<SubCriteria> subCriteriaList = new ArrayList<SubCriteria>();
	// Criterion corresponding to each subcriteria
	private Map<Criteria, List<Criterion>> criteriaCriterionMap = new HashMap<Criteria, List<Criterion>>();
	// List of tables' join conditions
	private List<TableJoinCondition> tablesJoinConditions = new ArrayList<TableJoinCondition>();
	// List of tables to be joined
	private Map<String, List<String>> tablesToBeJoined = new HashMap<String, List<String>>();
	// List of ColumnField, which will be used to map columns to fields of classes.
	private List<ColumnField> columnFieldList = new ArrayList<ColumnField>();
	private List<Object> backEdgeInfo = new ArrayList<Object>();


	/**
	 * sqliteDatabase
	 */
	private SQLiteDatabase sqliteDatabase;
	/**
	 * entityOrClassName
	 */
	private String entityOrClassName;

	public CriteriaImpl(String entityOrClassName, SQLiteDatabase sqliteDatabase, Map<String, ClassDetails> mappingCache) {
		this.entityOrClassName = entityOrClassName;
		this.sqliteDatabase = sqliteDatabase;
		this.mappingCache = mappingCache;
	}

	// Add a criterion on this criteria
	@Override
	public Criteria add(Criterion criterion) {
		if(criteriaCriterionMap.get(this) == null)
		{
			List<Criterion> criterionList = new ArrayList<Criterion>();
			criterionList.add(criterion);
			criteriaCriterionMap.put(this, criterionList);
		}
		else
			criteriaCriterionMap.get(this).add(criterion);
		return this;
	}

	// Add a criterion on a subcriteria
	public void add(Criteria criteria, Criterion criterion){
		if(criteriaCriterionMap.get(criteria) == null)
		{
			List<Criterion> criterionList = new ArrayList<Criterion>();
			criterionList.add(criterion);
			criteriaCriterionMap.put(criteria, criterionList);
		}
		else
			criteriaCriterionMap.get(criteria).add(criterion);

	}

	private void addCriteria(Criteria criteria, Criterion criterion) {

		if (criterion instanceof SimpleExpression) {
			extractSimpleExpression(criteria, (SimpleExpression) criterion);
		} else if (criterion instanceof LogicalExpression) {
			extractLogicalExpression(criteria, (LogicalExpression) criterion);
		}
	}

	private void extractSimpleExpression(Criteria criteria, SimpleExpression se) {
		String className = null;
		if(criteria instanceof SubCriteria)
			className = ((SubCriteria)criteria).getClassName();
		else className = entityOrClassName;
		if (null == selection) {
			selection = "(" + getTableNameForCriterion(className, se.getPropertyName()) + "." + getColumnNameForCriterion(className, se.getPropertyName()) + " " + se.getOp() + " ?) ";
		} else {
			selection += "AND (" + getTableNameForCriterion(className, se.getPropertyName()) + "." +  getColumnNameForCriterion(className, se.getPropertyName()) + " " + se.getOp() + " ?) ";
		}
		selectionArgsList.add(se.getValue().toString());
	}

	private void addCriteriaFromLogicalExpression(Criteria criteria, Criterion criterion) {
		if (criterion instanceof SimpleExpression) {
			extractSimpleExpressionFromLogicalExpression(criteria, (SimpleExpression) criterion);
		} else if (criterion instanceof LogicalExpression) {
			extractLogicalExpression(criteria, (LogicalExpression) criterion);
		}
	}

	private void extractSimpleExpressionFromLogicalExpression(Criteria criteria, SimpleExpression se) {
		String className = null;
		if(criteria instanceof SubCriteria)
			className = ((SubCriteria)criteria).getClassName();
		else className = entityOrClassName;
		selection += "(" + getTableNameForCriterion(className, se.getPropertyName()) + "." + getColumnNameForCriterion(className, se.getPropertyName()) + " " + se.getOp() + " ?) ";
		selectionArgsList.add(se.getValue().toString());
	}

	private void extractLogicalExpression(Criteria criteria, LogicalExpression le) {
		if (null == selection) {
			selection = "(";
		} else {
			selection += "AND (";
		}
		addCriteriaFromLogicalExpression(criteria, le.getLhs());
		selection += le.getOp() + " ";
		addCriteriaFromLogicalExpression(criteria, le.getRhs());
		selection += ") ";
	}

	@Override
	public Criteria addOrder(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	private ClassDetails fetchClassDetailsMapping(Class<?> objClass) {
		ClassDetails subClassDetails = null;
		String objClassName = objClass.getName();
		ClassDetails superClassDetails = mappingCache.get(objClassName);
		Log.d("fetchClassDetailsMapping", " objClassName " + objClassName);
		for(Map.Entry<String, ClassDetails> entry: mappingCache.entrySet())
			Log.d("fetchClassDetailsMapping" , " " + entry.getKey() + "--" + entry.getValue().getClassName());
		if (null == superClassDetails) {
			Log.e("CACHE MISS", "CACHE MISS for " + objClass.getName());
			do {
				try {
					superClassDetails = annotationsScanner
							.getEntityObjectDetails(objClass);
					if (null != subClassDetails) {
						superClassDetails.getSubClassDetails().add(subClassDetails);
					}
					subClassDetails = superClassDetails;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

				Log.d("fetchClassDetailsMapping", " superClassDetails " + superClassDetails.getClassName());

			} while (Object.class != (objClass = objClass.getSuperclass()));
			mappingCache.put(objClassName, superClassDetails);
		}
		return superClassDetails;
	}


	@Override
	public List list() {
		List result = new ArrayList();
		Cursor cursor = null;
		try {
			Class<?> eoClass = Class.forName(entityOrClassName);
			ClassDetails classDetails = annotationsScanner.getEntityObjectDetails(eoClass);
			ClassDetails superClassDetails = classDetails;
			Entity entity = eoClass.getAnnotation(Entity.class);
			table = entity.name();
			findTablesToBeJoined();


			Map<FieldTypeDetails, String> colFieldMap = new HashMap<FieldTypeDetails, String>();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			String comma = "";

			// Iterate over tablesToBeJoined and create aliases of columns.
			for (Map.Entry<String, List<String>> tableColumns : tablesToBeJoined.entrySet())
			{
				String tableName = tableColumns.getKey();
				for(String column: tableColumns.getValue())
				{

					sb.append(comma).append(tableName.toLowerCase()).append(".").append(column)
					.append(" AS ").append(getColumnAlias(tableName, column));
					comma = ",";
				}
			}

			sb.append(" FROM ");
			sb.append(table);

			// Iterate over tablesJoinConditions and create joins.
			for(TableJoinCondition tableJoinCondition: tablesJoinConditions)
			{
				sb.append(tableJoinCondition.getJoinString());
			}

			// Add all the criterion
			for (Map.Entry<Criteria, List<Criterion>> criteriaCriterionList : criteriaCriterionMap.entrySet())
			{
				for(Criterion criterion: criteriaCriterionList.getValue())
					addCriteria(criteriaCriterionList.getKey(), criterion);

			}

			if (!selectionArgsList.isEmpty()) {
				selectionArgs = new String[selectionArgsList.size()];
				int index = 0;
				for (String val : selectionArgsList) {
					selectionArgs[index] = val;
					index += 1;
				}
			}


			// append selection conditions
			if (null != selection) {
				sb.append(" WHERE ").append(selection);
			}
			sb.append(";");
			String sql = sb.toString();
			Log.d("Generated SQL", sql);

			// Finally, execute query.
			cursor = sqliteDatabase.rawQuery(sql, selectionArgs);

			// Parse the result and set the fields in the created objects.
			if (cursor.moveToFirst()) {
				Object eo = null;
				do {
					// Check if the object with the same id is not already there in result collection
					if((eo = findObjectInCollection(result, entityOrClassName, 
							cursor.getLong(cursor.getColumnIndex(findColumn(getClassOfId(entityOrClassName),
									getFieldTypeDetailsOfId(entityOrClassName).getFieldName()))),
									getFieldTypeDetailsOfId(entityOrClassName).getFieldName())) == null)
					{
						eo = eoClass.newInstance();
						Log.d("List", " Created a new object of " + eoClass.getName() + ":-)");
						result.add(eo);
					}
					fillObject(eo, eoClass, cursor);
				} while (cursor.moveToNext());
				cursor.close();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		return result;
	}

	/*
	 * Goes up the inheritance tree of eoClassName, and finds the fieldTypeDetails of id field.
	 */
	private FieldTypeDetails getFieldTypeDetailsOfId(String eoClassName) {
		Class<?> currentClass = null;
		try {
			currentClass = Class.forName(eoClassName);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FieldTypeDetails result = null;
		while(currentClass != Object.class)
		{
			ClassDetails classDetails = null;
			try {
				classDetails = annotationsScanner.getEntityObjectDetails(currentClass);
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
			if((result = classDetails.getFieldTypeDetailsOfId()) != null)
				break;
			currentClass = currentClass.getSuperclass();
		}
		Log.d("FieldTypeDetailsOfId", "returning " + result);
		return result;
	}

	/*
	 * Goes up the inheritance tree of eoClassName, and find which class contains the id.
	 */
	private String getClassOfId(String eoClassName) {
		Class<?> currentClass = null;
		try {
			currentClass = Class.forName(eoClassName);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String result = null;
		while(currentClass != Object.class)
		{
			ClassDetails classDetails = null;
			try {
				classDetails = annotationsScanner.getEntityObjectDetails(currentClass);
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
			if((classDetails.getFieldTypeDetailsOfId()) != null)
			{
				result = currentClass.getName();
				break;
			}
			currentClass = currentClass.getSuperclass();
		}
		Log.d("FieldTypeDetailsOfId", "returning " + result);
		return result;
	}

	/*
	 * Fills the object with the current row in the cursor. 
	 * Calls itself to fill the associated objects.
	 * Uses columnFieldList to map between columns and variables.
	 */
	private void fillObject(Object object, Class<?> objectType, Cursor cursor)
	{

		while(objectType != Object.class)
		{
			ClassDetails classDetails = null;
			try {
				classDetails = annotationsScanner.getEntityObjectDetails(objectType);
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
			Log.d("fillObject", "Filling Object of " + objectType.getName() + " with id " + cursor.getString(cursor.getColumnIndex(findColumn(getClassOfId(classDetails.getClassName()),
					getFieldTypeDetailsOfId(classDetails.getClassName()).getFieldName()))));
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
							obj = Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).newInstance();


							// Add inner type object to collection object.
							Method addToCollectionMethod = fieldTypeDetails.getFieldType().getMethod("add", Object.class);
							addToCollectionMethod.invoke(collectionObj, obj);

							backEdgeInfo.add(obj);
							// Fill inner type object
							Log.d("fillObject", "Created a collection of " + obj.getClass().getName());
							Object tempObj;
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null)
								fillObject(obj,Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
							else
								obj = tempObj;
							backEdgeInfo.remove(obj);
						
						}
						// Check if the object with same id is already there in collection, if yes, fill it.
						else if((obj = findObjectInCollection(collectionObj, 
								getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()),
								cursor.getLong(cursor.getColumnIndex(findColumn(getClassOfId(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())),
										getFieldTypeDetailsOfId(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).getFieldName()))),
										getFieldTypeDetailsOfId(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).getFieldName()
								)) != null){
							Log.d("fillObject", "Object already exists");
							
							backEdgeInfo.add(obj);
							Object tempObj;
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null)
								fillObject(obj, Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
							else 
								obj = tempObj;
							backEdgeInfo.remove(obj);
						}
						// other wise, create object and fill it.
						else{
							// Create inner type object.
							obj = Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())).newInstance();

							// Add inner type object to collection object.
							Method addToCollectionMethod = fieldTypeDetails.getFieldType().getMethod("add", Object.class);
							addToCollectionMethod.invoke(collectionObj, obj);
							
							backEdgeInfo.add(obj);
							Object tempObj;
							// Fill inner type object
							if((tempObj = checkBackEdge(classDetails, fieldTypeDetails, cursor, true)) == null)
								fillObject(obj,Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName())), cursor);
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


	

	/*
	 * Check if there is a back reference to an object of type TYPE and id ID.
	 * Determines this by scanning through the set backEdgeInfo, which is list of objects traversed till now.
	 */
	private Object checkBackEdge(ClassDetails classDetails, FieldTypeDetails fieldTypeDetails, Cursor cursor, boolean collection) {

		Class<?> type = null;
		long id = 0;
		try{
			if(collection == true){
				type = Class.forName(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()));
				id = cursor.getLong(cursor.getColumnIndex(findColumn(getClassOfId(getCollectionType(classDetails.getClassName(),
						fieldTypeDetails.getFieldName())),
						getFieldTypeDetailsOfId(getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName()))
						.getFieldName())));
			}else{
				type = fieldTypeDetails.getFieldType();
				id = cursor.getLong(cursor.getColumnIndex(
						findColumn(getClassOfId(fieldTypeDetails.getFieldType().getName()), 
								getFieldTypeDetailsOfId(fieldTypeDetails.getFieldType().getName()).getFieldName())));

			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		for(Object obj : backEdgeInfo)
		{
			if(obj.getClass() == type)
			{
				String getterMethodName = Utils.getGetterMethodName(getFieldTypeDetailsOfId(obj.getClass().getName()).getFieldName());
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
		Log.d("doesObjectExistInCollection", " id is " + id + " idField is " + idField + " collectionTypeName is " + collectionTypeName);
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
		Log.d("doesObjectExistInColleciton", "NO!");
		return null;

	}

	@Override
	public Criteria setProjection(ProjectionList projectionList) {
		this.projectionList = projectionList;
		return this;
	}

	@Override
	public Cursor cursor() {
		Cursor cursor = null;
		try {
			Class<?> eoClass = Class.forName(entityOrClassName);
			Entity entity = eoClass.getAnnotation(Entity.class);
			table = entity.name();
			if (!selectionArgsList.isEmpty()) {
				selectionArgs = new String[selectionArgsList.size()];
				int index = 0;
				for (String val : selectionArgsList) {
					selectionArgs[index] = val;
					index += 1;
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (null == this.projectionList) {
				sb.append("* ");
			} else {
				String comma = "";
				for (Projection projection : projectionList.getElements()) {
					if (projection instanceof PropertyProjection) {
						PropertyProjection propertyProjection = (PropertyProjection) projection;
						sb.append(comma).append(propertyProjection.getPropertyName());
					}
					comma = ", ";
				}
				sb.append(" ");
			}
			sb.append("FROM ");
			sb.append(table).append(" ");
			sb.append("WHERE ").append(selection);
			sb.append(";");
			// cursor = sqliteDatabase.query(distinct, table, columns, selection,
			// selectionArgs, groupBy, having, orderBy, limit);x
			String sql = sb.toString();
			Log.d("Generated SQL", sql);
			cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return cursor;
	}


	/* (non-Javadoc)
	 * @see iiitb.dm.ormlibrary.query.Criteria#createCriteria(java.lang.String)
	 */
	@Override
	public Criteria createCriteria(String relatedEntityFieldName)
	{
		return new SubCriteria(this, relatedEntityFieldName);
	}



	/**
	 * finds Tables to be joined from entityOrClassName, 
	 * fills tablesToBeJoined and tablesJoinConditions
	 */
	private void findTablesToBeJoined()
	{
		ClassDetails classDetails = null, superClassDetails = null;
		try {
			classDetails = annotationsScanner.getEntityObjectDetails(Class.forName(entityOrClassName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<ClassDetails> queue = new ArrayList<ClassDetails>();
		queue.add(classDetails);
		Log.d("findTablesToBeJoined ", " eoClassName is " + entityOrClassName);
		Log.d("findTablesToBeJoined ", " classDetailsName is " + classDetails.getClassName());

		// Find all the related and inherited classes.
		do
		{
			classDetails = queue.remove(0);
			Log.d("findTablesToBeJoined", "On class " + classDetails.getClassName());

			// Add all related classes' details to the queue.
			for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails())
			{
				ClassDetails relatedClassDetails = null;

				// If there is a reference to another class, but foreign key is on other side.
				if((fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null
						&& fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE).get(Constants.MAPPED_BY).equals(""))
						|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
				{
					try {
						relatedClassDetails = annotationsScanner.getEntityObjectDetails(fieldTypeDetails.getFieldType());
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

					String joinColumn = null;
					if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
					{
						try {
							relatedClassDetails = annotationsScanner.getEntityObjectDetails(Class.forName(getCollectionType(classDetails.getClassName(),
									fieldTypeDetails.getFieldName())));
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// Find the column name.
						String referenceName = (String)fieldTypeDetails.getAnnotationOptionValues()
								.get(Constants.ONE_TO_MANY).get(Constants.MAPPED_BY);
						joinColumn = null;
						if(!referenceName.equals(""))
						{
							for(FieldTypeDetails relatedFieldTypeDetails : relatedClassDetails.getFieldTypeDetails())
							{
								if(relatedFieldTypeDetails.getFieldName().equals(referenceName))
									joinColumn = (String)relatedFieldTypeDetails.getAnnotationOptionValues()
									.get(Constants.JOIN_COLUMN).get(Constants.NAME);
							}
						}
						else
							joinColumn = (String) fieldTypeDetails.getAnnotationOptionValues()
							.get(Constants.JOIN_COLUMN).get(Constants.NAME);

					}
					else
						joinColumn = "_id";

					// Add to tablesJoinConditions and to queue
					String table1 = (String)relatedClassDetails.getAnnotationOptionValues()
							.get(Constants.ENTITY).get(Constants.NAME);
					String table2 = (String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
					String joinColumn1 = joinColumn;
					String joinColumn2 = "_id";
					if(!joinExists(table1, joinColumn1, table2, joinColumn2)){
						tablesJoinConditions.add(new TableJoinCondition(table1,joinColumn1,table2,joinColumn2));
						queue.add(relatedClassDetails);
					}
				}
				// else if there is a reference, and foreign key is on this side.
				else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null
						&& fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE).get(Constants.MAPPED_BY).equals("")
						|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null)
				{

					try {
						relatedClassDetails = annotationsScanner.getEntityObjectDetails(fieldTypeDetails.getFieldType());
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

					String table1 = (String)relatedClassDetails.getAnnotationOptionValues()
							.get(Constants.ENTITY).get(Constants.NAME);
					String table2 = (String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
					String joinColumn1 = "_id";
					String joinColumn2 = (String)fieldTypeDetails.getAnnotationOptionValues()
							.get(Constants.JOIN_COLUMN).get(Constants.NAME);

					if(!joinExists(table1, joinColumn1, table2, joinColumn2))
					{
						tablesJoinConditions.add(new TableJoinCondition(table1, joinColumn1, table2, joinColumn2));
						queue.add(relatedClassDetails);
					}

				}
				// else if there is a reference, and it's many to many relationship
				else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_MANY) != null)
				{
					String className = getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName());
					try {
						relatedClassDetails = annotationsScanner.getEntityObjectDetails(Class.forName(className));
					} catch (Exception e) {
						e.printStackTrace();
					}


					ClassDetails owningSide, inverseSide;
					if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_MANY).get(Constants.MAPPED_BY).equals(""))
					{
						owningSide = classDetails;
						inverseSide = relatedClassDetails;
					}
					else{
						owningSide = relatedClassDetails;
						inverseSide = classDetails;
					}

					// owningSide table
					String owningSideTableName = (String) owningSide
							.getAnnotationOptionValues().get(Constants.ENTITY)
							.get(Constants.NAME);

					// inverseSide table
					String inverseSideTableName = (String) inverseSide
							.getAnnotationOptionValues().get(Constants.ENTITY)
							.get(Constants.NAME);

					// Join table name
					String joinTableName = owningSideTableName + "_" + inverseSideTableName;

					// joinColumnName must be different if there is a reverse mapping
					FieldTypeDetails joinColumnFieldTypeDetails = inverseSide
							.getFieldTypeDetailsByMappedByAnnotation(fieldTypeDetails
									.getFieldName());
					String joinColumnName;
					if (joinColumnFieldTypeDetails == null)
						joinColumnName = owningSideTableName;
					else
						joinColumnName = joinColumnFieldTypeDetails.getFieldName();
					joinColumnName += "_"
							+ owningSide.getFieldTypeDetailsOfId()
							.getAnnotationOptionValues().get(Constants.COLUMN)
							.get(Constants.NAME);


					String inverseJoinColumnName = fieldTypeDetails.getFieldName() + "_"
							+ inverseSide.getFieldTypeDetailsOfId()
							.getAnnotationOptionValues().get(Constants.COLUMN)
							.get(Constants.NAME);

					String table1, table2, joinColumn1, joinColumn2;

					// There will be 2 joins in this case, join of joinTable with both related tables.
					table1 = (String) relatedClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
					table2 = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);

					if(!joinExists(joinTableName, joinColumnName, table2, "_id") 
							&& !joinExists(table1,"_id",joinTableName, inverseJoinColumnName))
					{
						tablesJoinConditions.add(new TableJoinCondition(joinTableName, joinColumnName, table2, "_id"));
						tablesJoinConditions.add(new TableJoinCondition(table1,"_id",joinTableName, inverseJoinColumnName));
						queue.add(relatedClassDetails);
					}

				}

			}


			/* Go up the inheritance tree of this class.
			 */
			Class<?> classObj = null;
			try {
				classObj = Class.forName(classDetails.getClassName());
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			do
			{
				List<String> columnNames = new ArrayList<String>();

				/* Map column names aliases to class and field, so that later values of columns can be set to right fields.
				 */
				for (FieldTypeDetails ftd : classDetails.getFieldTypeDetails()) {
					if(ftd.getAnnotationOptionValues().get(Constants.COLUMN) == null)
						continue;
					String col = (String) ftd.getAnnotationOptionValues()
							.get(Constants.COLUMN).get(Constants.NAME);
					String asCol = getColumnAlias(((String)classDetails.getAnnotationOptionValues()
							.get(Constants.ENTITY).get(Constants.NAME))
							.toLowerCase(), col);
					columnFieldList.add(new ColumnField(asCol, classDetails.getClassName(), ftd.getFieldName()));
					columnNames.add(col);

				}


				tablesToBeJoined.put((String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME), 
						columnNames);
				Log.d(" find ", classDetails.getClassName());

				try {
					superClassDetails = classObj != Object.class ? annotationsScanner
							.getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass()) : null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(superClassDetails != null && superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null)
				{
					Log.d("find", "superClass:" + superClassDetails.getClassName());
					if(superClassDetails.getAnnotationOptionValues()
							.get(Constants.INHERITANCE).get(Constants.STRATEGY)
							.equals(InheritanceType.JOINED)){

						tablesJoinConditions.add(new TableJoinCondition((String)superClassDetails.getAnnotationOptionValues()
								.get(Constants.ENTITY).get(Constants.NAME), 
								"_id",
								(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY)
								.get(Constants.NAME),
								"_id")
								);
						Log.d("find", "" + (String)superClassDetails.getAnnotationOptionValues()
								.get(Constants.ENTITY).get(Constants.NAME) + 
								"_id" + 
								(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY)
								.get(Constants.NAME)+
								"_id");

						classDetails = superClassDetails;

					}
					/* If inheritance strategy of super class is table per class, no need to join 
					 * with another table.
					 */
					else if(superClassDetails.getAnnotationOptionValues()
							.get(Constants.INHERITANCE).get(Constants.STRATEGY)
							.equals(InheritanceType.TABLE_PER_CLASS)){

						// The map between the column names and class and field names must be created though.
						for (FieldTypeDetails ftd : superClassDetails.getFieldTypeDetails()) {
							if(ftd.getAnnotationOptionValues().get(Constants.COLUMN) == null)
								continue;
							String col = (String) ftd.getAnnotationOptionValues()
									.get(Constants.COLUMN).get(Constants.NAME);
							String asCol = getColumnAlias(((String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME))
									.toLowerCase() , col);
							columnFieldList.add(new ColumnField(asCol, superClassDetails.getClassName(), ftd.getFieldName())); 
							columnNames.add(col);
						}
						ClassDetails tempClassDetails = classDetails;
						while(superClassDetails != null && !superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
								.get(Constants.STRATEGY).equals(InheritanceType.JOINED)){
							Log.d(" find ", "Skipping " + classDetails.getClassName());
							classDetails = superClassDetails;

							try {
								classObj = Class.forName(classDetails.getClassName()).getSuperclass();
								superClassDetails = classObj != Object.class ? annotationsScanner.getEntityObjectDetails(classObj) : null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(superClassDetails != null)
						{
							tablesJoinConditions.add(new TableJoinCondition((String)superClassDetails.getAnnotationOptionValues()
									.get(Constants.ENTITY).get(Constants.NAME), 
									"_id",
									(String)tempClassDetails.getAnnotationOptionValues().get(Constants.ENTITY)
									.get(Constants.NAME),
									"_id")
									);
						}
						classDetails = superClassDetails;

					}

					try {
						classObj = Class.forName(classDetails.getClassName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else classObj = Object.class;

			}while(classObj != Object.class);

		}while( queue.size() > 0);

	}


	public String getColumnAlias(String tableName, String columnName)
	{
		return tableName.toLowerCase() + "_" + columnName;
	}


	public boolean joinExists(String tableName, String joinColumn, String otherTableName, String otherJoinColumn)
	{
		return tablesJoinConditions.contains(new TableJoinCondition(tableName, joinColumn, otherTableName, otherJoinColumn))
				|| tablesJoinConditions.contains(new TableJoinCondition(otherTableName, otherJoinColumn, tableName, joinColumn));
	}

	/*
	 * Get name of table on which the criterion will be applied.
	 */
	public String getTableNameForCriterion(String className, String variableName)
	{
		Class<?> classObj = null;
		try {
			classObj = Class.forName(className);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean found = false;
		ClassDetails classDetails = null;
		try {
			classDetails = annotationsScanner.getEntityObjectDetails(classObj);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String tableName = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
		Log.d("getTableNameForCriterion", className + " " + variableName);
		try {
			do 
			{
				classObj = Class.forName(className);
				classDetails = annotationsScanner.getEntityObjectDetails(classObj);
				if(classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null 
						&& classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
						.get(Constants.STRATEGY).equals(InheritanceType.JOINED))
					tableName = (String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
				for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails())
				{
					/*if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.COLUMN) != null 
							&& ((String)fieldTypeDetails.getAnnotationOptionValues().get(Constants.COLUMN)
									.get(Constants.NAME))
									.equalsIgnoreCase(variableName))*/
					if(fieldTypeDetails.getFieldName().equals(variableName))
					{
						found = true;
					}
				}
				className = classObj.getSuperclass().getName();
			}while(found == false && classObj.getSuperclass() != Object.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tableName;

	}

	/*
	 * 
	 */
	public String getColumnNameForCriterion(String className, String variableName)
	{
		Class<?> classObj = null;
		try {
			classObj = Class.forName(className);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean found = false;
		ClassDetails classDetails = null;
		try {
			classDetails = annotationsScanner.getEntityObjectDetails(classObj);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String columnName = null;
		Log.d("getColumnNameForCriterion", className + " " + variableName);
		try {
			do 
			{
				classObj = Class.forName(className);
				classDetails = annotationsScanner.getEntityObjectDetails(classObj);
				for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails())
				{
					if(fieldTypeDetails.getFieldName().equals(variableName))
					{
						found = true;
						columnName = (String) fieldTypeDetails.getAnnotationOptionValues().get(Constants.COLUMN).get(Constants.NAME);
					}
				}
				className = classObj.getSuperclass().getName();
			}while(found == false && classObj.getSuperclass() != Object.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return columnName;

	}


	/*
	 * Get type of collection named variableName in class className
	 */
	public String getCollectionType(String className, String variableName)
	{
		String result = null;
		ClassDetails classDetails = null;
		try {
			classDetails = annotationsScanner.getEntityObjectDetails(Class.forName(className));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ParameterizedType genericType = null;
		try {
			genericType = (ParameterizedType)Class.forName(className)
					.getDeclaredField(classDetails.getFieldTypeDetailsByFieldName(variableName).getFieldName())
					.getGenericType();
			result = ((Class<?>)genericType.getActualTypeArguments()[0]).getName();
		} 
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		Log.d("CollectionType", "returning " + result);
		return result;
	}

	/*
	 * Find column name in columnFieldList, from class name and variable name and return it.
	 * Used while filling objects.
	 */
	public String findColumn(String className, String variableName)
	{
		for(ColumnField colField: columnFieldList)
		{
			Log.d("findColumn", "classname: " + colField.getClassName() + " variableName: " + colField.getFieldName());
			if(colField.getClassName().equals(className) && colField.getFieldName().equals(variableName))
				return colField.getColumnName();
		}
		Log.d("findColumn", "Returning null");
		return null;
	}



	public class SubCriteria implements Criteria{

		private String parent;
		private String associationPath;
		private String className;

		public SubCriteria(Criteria parent, String associationPath)
		{

			if(parent instanceof SubCriteria)
				this.parent = ((SubCriteria)parent).getClassName();
			else
				this.parent = entityOrClassName;
			this.associationPath = associationPath;
			this.className = getClassNameByAssociationPath(associationPath);
			CriteriaImpl.this.subCriteriaList.add(this);
			Log.d("SubCriteria ", "New criteria with associationPath " + this.associationPath + " and className " + this.className + " and parent " + this.parent);
		}

		public String getClassName() {
			return className;
		}


		public void setClassName(String className) {
			this.className = className;
		}


		public String getAssociationPath() {
			return associationPath;
		}

		public void setAssociationPath(String associationPath) {
			this.associationPath = associationPath;
		}



		@Override
		public Criteria add(Criterion criterion) {
			CriteriaImpl.this.add(this, criterion);
			return this;

		}

		@Override
		public Criteria addOrder(Order order) {
			return null;
		}

		@Override
		public List list() {
			return CriteriaImpl.this.list();
		}

		@Override
		public Cursor cursor() {
			return CriteriaImpl.this.cursor();
		}

		@Override
		public Criteria setProjection(ProjectionList projectionList) {
			return CriteriaImpl.this.setProjection(projectionList);
		}

		@Override
		public Criteria createCriteria(String associationPath) {
			return new SubCriteria(this, associationPath);
		}


		/*
		 * Association path is a variable name. Gets the name of the class to which the variable belongs.
		 * Goes up the inheritance tree from parent class.
		 */
		public String getClassNameByAssociationPath(String associationPath)
		{
			ClassDetails classDetails = null;
			Log.d("getClassName", " associationPath " + associationPath);
			boolean found = false;
			try {
				classDetails = annotationsScanner.getEntityObjectDetails(Class.forName(this.parent));
				if(classDetails.getFieldTypeDetailsByFieldName(associationPath) != null)
					found = true;
				else{
					while(Class.forName(classDetails.getClassName()).getSuperclass() != Object.class)
					{
						Log.d("getClassName" , "In loop " + classDetails.getClassName());
						if(classDetails.getFieldTypeDetailsByFieldName(associationPath) != null)
						{
							found = true;
							break;
						}
						classDetails = annotationsScanner.getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass());
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			String className = null;
			String temp;
			if(found == true)
			{
				className = classDetails.getFieldTypeDetailsByFieldName(associationPath).getFieldType().getName();
				Log.d("getClassName", "class name is " + className);
				// If the field is of collection type, get the collection parameter type.
				if((temp = CriteriaImpl.this.getCollectionType(classDetails.getClassName(), associationPath)) != null)
					className = temp;
			}
			return className;

		}

	}

	public class TableJoinCondition{
		private String tableName;
		private String joinColumn;
		private String otherTableName;
		private String otherJoinColumn;



		public TableJoinCondition(String tableName, String joinColumn,
				String otherTableName, String otherJoinColumn) {
			super();
			this.tableName = tableName;
			this.joinColumn = joinColumn;
			this.otherTableName = otherTableName;
			this.otherJoinColumn = otherJoinColumn;
		}

		/*
		 * Overridden to compare individual elements of the object.
		 * Used by method joinExists.
		 */
		@Override
		public boolean equals(Object obj)
		{
			TableJoinCondition tableJoinConditionObj = (TableJoinCondition)obj;
			return tableName.equals(tableJoinConditionObj.getTableName())
					&& joinColumn.equals(tableJoinConditionObj.getJoinColumn())
					&& otherTableName.equals(tableJoinConditionObj.getOtherTableName())
					&& otherJoinColumn.equals(tableJoinConditionObj.getOtherJoinColumn());
		}

		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getJoinColumn() {
			return joinColumn;
		}
		public void setJoinColumn(String joinColumn) {
			this.joinColumn = joinColumn;
		}
		public String getOtherTableName() {
			return otherTableName;
		}
		public void setOtherTableName(String otherTableName) {
			this.otherTableName = otherTableName;
		}
		public String getOtherJoinColumn() {
			return otherJoinColumn;
		}
		public void setOtherJoinColumn(String otherJoinColumn) {
			this.otherJoinColumn = otherJoinColumn;
		}
		public String getJoinString(){
			return " JOIN " + tableName + " ON " + tableName + "." + joinColumn + "=" + otherTableName + "." + otherJoinColumn;
		}



	}

	public class ColumnField{
		String columnName;
		String className;
		String fieldName;


		public ColumnField(String columnName, String className,
				String fieldName) {
			this.columnName = columnName;
			this.className = className;
			this.fieldName = fieldName;
		}


		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

	}
}
