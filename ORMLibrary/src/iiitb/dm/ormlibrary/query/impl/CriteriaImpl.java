package iiitb.dm.ormlibrary.query.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.Projection;
import iiitb.dm.ormlibrary.query.criterion.LogicalExpression;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.query.criterion.PropertyProjection;
import iiitb.dm.ormlibrary.query.criterion.SimpleExpression;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Kumudini
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
	private AnnotationsScanner annotationsScanner;	


	// Criterion corresponding to each subcriteria
	private Map<Criteria, List<Criterion>> criteriaCriterionMap = new HashMap<Criteria, List<Criterion>>();
	private Context context;


	/**
	 * sqliteDatabase
	 */
	private SQLiteDatabase sqliteDatabase;
	/**
	 * entityOrClassName
	 */
	private String criteriaClassName;

	public CriteriaImpl(String criteriaClassName, SQLiteDatabase sqliteDatabase, Context context) {
		this.criteriaClassName = criteriaClassName;
		this.sqliteDatabase = sqliteDatabase;				
		this.context = context;
		this.annotationsScanner = AnnotationsScanner.getInstance(context);
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
		else className = criteriaClassName;
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
		else className = criteriaClassName;
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

	@Override
	/**
	 * Get the result of the query
	 */
	public List<?> list()
	{
		// TODO: can I create a superclass list?
		List<?> result = new LinkedList();
		try
		{
			// TODO: Ugly??? Should I be using subClassDetails field here??
			List<String> eoNames = getEntityObjectsNamesFromManifest(context);
			result = list(criteriaClassName, eoNames);
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * Get the result of the query for the specified entity class and all its subclasses
	 * @param entityOrClassName Specified entity class
	 * @param eoNames List containing all entity class names in the application
	 * @return result of the query for the specified entity class and all its subclasses
	 */
	private List<?> list(String entityOrClassName, List<String> eoNames)
	{
		ClassDetails classDetails = null;
		try
		{
			classDetails = annotationsScanner.getEntityObjectDetails(Utils
					.getClassObject(entityOrClassName).getName());
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		if (classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) == null)
			return list(entityOrClassName);

		List result = new LinkedList();

		// Collect the result of the specified class
		if (classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
				.get(Constants.STRATEGY)
				.equals(InheritanceType.TABLE_PER_CLASS)
				|| (classDetails.getAnnotationOptionValues()
						.get(Constants.INHERITANCE).get(Constants.STRATEGY)
						.equals(InheritanceType.JOINED) && !Modifier
						.isAbstract(Utils.getClassObject(entityOrClassName)
								.getModifiers())))
			result.addAll(list(entityOrClassName));

		// Collect the result of all its subclasses
		for (String eoName : eoNames)
			if (Utils.getClassObject(eoName).getSuperclass()
					.equals(Utils.getClassObject(entityOrClassName)))
				result.addAll(list(eoName, eoNames));
		return result;
	}

	private List list(String entityOrClassName) {
		List result = new ArrayList();
		Cursor cursor = null;
		try {
			
			criteriaClassName = entityOrClassName;
			StringBuilder sb = new StringBuilder();
			QueryBuilder queryBuilder = new QueryBuilder(entityOrClassName);
			sb.append(queryBuilder.getQuery());
			List<ColumnField> columnFieldList = queryBuilder.getQueryDetails().getColumnFieldList();

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

			/* Generate selection conditions to exclude the tuples in the parent
			 *  table which have discriminator values. These tuples belong to an
			 *  entity class which inherited from this parent entity 
			 *  class(entityClassName)
			 */ 
			ClassDetails entityClassDetails = annotationsScanner// TODO: AnnotationScannerImpl()
			.getEntityObjectDetails(entityOrClassName);
			if (entityClassDetails.getAnnotationOptionValues().get(
					Constants.INHERITANCE) != null
					&& entityClassDetails.getAnnotationOptionValues()
					.get(Constants.INHERITANCE).get(Constants.STRATEGY)
					.equals(InheritanceType.JOINED))
			{
				// TODO: What if no discriminator column is specified?
				// Define default discriminator column
				String tableName = (String) entityClassDetails
						.getAnnotationOptionValues().get(Constants.ENTITY)
						.get(Constants.NAME);
				String discriminatorCol = (String) entityClassDetails
						.getAnnotationOptionValues()
						.get(Constants.DISCRIMINATOR_COLUMN)
						.get(Constants.NAME);
				sb.append(" AND ").append(tableName.toLowerCase()).append(".")
				.append(discriminatorCol).append(" IS NULL ");
			}

			sb.append(";");
			String sql = sb.toString();
			Log.d("Generated SQL", sql);

			// Finally, execute query.
			cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
			
			result = new ObjectFiller(columnFieldList, entityOrClassName, cursor).getObjects();

			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		selection = null;
		selectionArgsList = new ArrayList<String>();
		return result;
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
			Class<?> eoClass = Class.forName(criteriaClassName);
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



	


	// TODO: Ugly???
	private List<String> getEntityObjectsNamesFromManifest(Context context)
			throws XmlPullParserException, IOException {
		String XML_TAG = "XML in CRITERIA";
		Resources resources = context.getResources();
		// TODO: Should get from AndroidManifest
		String uri = "xml/" + "entity_objects";
		XmlResourceParser xpp = resources.getXml(resources.getIdentifier(uri, null,
				context.getPackageName()));
		xpp.next();
		int eventType = xpp.getEventType();
		List<String> eoNames = new ArrayList<String>();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.START_TAG) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.END_TAG) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.TEXT) {
				eoNames.add(xpp.getText());
				Log.v(XML_TAG, "ClassName: " + xpp.getText());
			}
			eventType = xpp.next();
		}
		return eoNames;
	}
	
	/*
	 * Get name of table on which the criterion will be applied.
	 */
	private String getTableNameForCriterion(String className, String variableName)
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
			classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(classObj.getName());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String tableName = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
		Log.v("getTableNameForCriterion", className + " " + variableName);
		try {
			do 
			{
				classObj = Class.forName(className);
				classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(classObj.getName());
				if(classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null 
						&& classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
						.get(Constants.STRATEGY).equals(InheritanceType.JOINED))
					tableName = (String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
				for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails())
				{
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
	 * Get name of column on which the criterion will be applied.
	 */
	private String getColumnNameForCriterion(String className, String variableName)
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
			classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(classObj.getName());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String columnName = null;
		Log.v("getColumnNameForCriterion", className + " " + variableName);
		try {
			do 
			{
				classObj = Class.forName(className);
				classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(classObj.getName());
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




	public class SubCriteria implements Criteria{

		private String parent;
		private String associationPath;
		private String className;

		public SubCriteria(Criteria parent, String associationPath)
		{

			if(parent instanceof SubCriteria)
				this.parent = ((SubCriteria)parent).getClassName();
			else
				this.parent = criteriaClassName;
			this.associationPath = associationPath;
			this.className = getClassNameByAssociationPath(associationPath);
			Log.v("SubCriteria ", "New criteria with associationPath " + this.associationPath + " and className " + this.className + " and parent " + this.parent);
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
			Log.v("getClassName", " associationPath " + associationPath);
			boolean found = false;
			try {
				classDetails = annotationsScanner.getEntityObjectDetails(this.parent);
				if(classDetails.getFieldTypeDetailsByFieldName(associationPath) != null)
					found = true;
				else{
					while(Class.forName(classDetails.getClassName()).getSuperclass() != Object.class)
					{
						Log.v("getClassName" , "In loop " + classDetails.getClassName());
						if(classDetails.getFieldTypeDetailsByFieldName(associationPath) != null)
						{
							found = true;
							break;
						}
						classDetails = annotationsScanner.getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass().getName());
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
				Log.v("getClassName", "class name is " + className);
				// If the field is of collection type, get the collection parameter type.
				if((temp = Utils.getCollectionType(classDetails.getClassName(), associationPath)) != null)
					className = temp;
			}
			return className;

		}

	}



}
