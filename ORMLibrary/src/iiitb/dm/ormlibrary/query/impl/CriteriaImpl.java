package iiitb.dm.ormlibrary.query.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;

import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

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
	private String groupBy;
	private String having;
	private String orderBy;
	private String limit;


	// Criterion corresponding to each subcriteria
	private Map<Criteria, List<Criterion>> criteriaCriterionMap = new HashMap<Criteria, List<Criterion>>();
	private Map<Criteria, ProjectionList> criteriaProjectionListMap = new HashMap<Criteria, ProjectionList>();
	private Map<Criteria, List<Order>> criteriaOrderMap = new HashMap<Criteria, List<Order>>();

	/**
	 * sqliteDatabase
	 */
	private SQLiteDatabase sqliteDatabase;
	/**
	 * entityOrClassName
	 */
	private String criteriaClassName;

	public CriteriaImpl(String criteriaClassName, SQLiteDatabase sqliteDatabase) {
		this.criteriaClassName = criteriaClassName;
		this.sqliteDatabase = sqliteDatabase;
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

	@Override
	public Criteria setProjection(ProjectionList projectionList) {
		criteriaProjectionListMap.put(this, projectionList);
		return this;
	}

	private void setProjection(Criteria criteria, ProjectionList projectionList){
		criteriaProjectionListMap.put(criteria, projectionList);
	}


	@Override
	public Criteria addOrder(Order order) {
		
		if(criteriaOrderMap.get(this) == null)
		{
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(order);
			criteriaOrderMap.put(this, orderList);
		}
		else
			criteriaOrderMap.get(this).add(order);
		return this;

	}
	
	public void addOrder(Criteria criteria, Order order)
	{
		if(criteriaOrderMap.get(criteria) == null)
		{
			List<Order> orderList = new ArrayList<Order>();
			orderList.add(order);
			criteriaOrderMap.put(criteria, orderList);
		}
		else
			criteriaOrderMap.get(criteria).add(order);
	}

	@Override
	public List<?> list()
	{
		return list(criteriaClassName);
	}


	/**
	 * Get the result of the query for the specified entity class and all its subclasses
	 * @param entityOrClassName Specified entity class
	 * @param eoNames List containing all entity class names in the application
	 * @return result of the query for the specified entity class and all its subclasses
	 */
	private List<?> list(String entityOrClassName)
	{
		ClassDetails classDetails = null;
		try
		{
			classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(Utils
					.getClassObject(entityOrClassName).getName());
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		if (classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) == null)
			return getQueryResult(entityOrClassName);

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
			result.addAll(getQueryResult(entityOrClassName));

		// Collect the result of all its subclasses
		for (String eoName :  AnnotationsScanner.getInstance()
				.getAllEntityObjectDetails().keySet())
			if (Utils.getClassObject(eoName).getSuperclass()
					.equals(Utils.getClassObject(entityOrClassName)))
				result.addAll(list(eoName));
		return result;
	}

	private List getQueryResult(String entityOrClassName) {
		List result = new ArrayList();
		Cursor cursor = null;
		try {			
			criteriaClassName = entityOrClassName;
			StringBuilder sb = new StringBuilder();
			QueryBuilder queryBuilder = new QueryBuilder(entityOrClassName, criteriaCriterionMap, criteriaProjectionListMap, criteriaOrderMap);
			sb.append(queryBuilder.getQuery().get(0).getKey());
			List<ColumnField> columnFieldList = queryBuilder.getQueryDetails().getColumnFieldList();

			/* Generate selection conditions to exclude the tuples in the parent
			 *  table which have discriminator values. These tuples belong to an
			 *  entity class which inherited from this parent entity 
			 *  class(entityClassName)
			 */ 
			ClassDetails entityClassDetails = AnnotationsScanner.getInstance()// TODO: AnnotationScannerImpl()
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
			
			sb.append(queryBuilder.getOrderString());

			String sql = sb.toString();
			Log.d("Generated SQL", sql);

			// Finally, execute query.
			cursor = sqliteDatabase.rawQuery(sql, queryBuilder.getQuery().get(0).getValue());

			result = new ObjectFiller(columnFieldList, entityOrClassName, cursor).getObjects();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}


	@Override
	public Cursor cursor() {
		Cursor cursor = null;

		QueryBuilder queryBuilder = new QueryBuilder(criteriaClassName, criteriaCriterionMap, criteriaProjectionListMap, criteriaOrderMap);
		String sql = queryBuilder.getQuery().get(0).getKey();
		Log.d("Generated SQL", sql);
		cursor = sqliteDatabase.rawQuery(sql, queryBuilder.getQuery().get(0).getValue());

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
			CriteriaImpl.this.addOrder(this, order);
			return this;
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
			CriteriaImpl.this.setProjection(this, projectionList);
			return this;
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
				classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(this.parent);
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
						classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass().getName());
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
