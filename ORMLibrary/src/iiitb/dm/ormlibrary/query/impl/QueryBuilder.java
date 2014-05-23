package iiitb.dm.ormlibrary.query.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.Projection;
import iiitb.dm.ormlibrary.query.criterion.BetweenExpression;
import iiitb.dm.ormlibrary.query.criterion.InExpression;
import iiitb.dm.ormlibrary.query.criterion.LogicalExpression;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.query.criterion.PropertyProjection;
import iiitb.dm.ormlibrary.query.criterion.SimpleExpression;
import iiitb.dm.ormlibrary.query.impl.CriteriaImpl.SubCriteria;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import android.annotation.SuppressLint;

import java.lang.reflect.Modifier;

public class QueryBuilder {

	private QueryDetails queryDetails = new QueryDetails();

	private List<ClassDetails> queue = new ArrayList<ClassDetails>();
	private String entityOrClassName;
	private Map<Criteria, List<Criterion>> criteriaCriterionList;
	private String selection;
	private String[] selectionArgs;
	private List<String> selectionArgsList = new ArrayList<String>();
	private Map<String, Integer> tableAliases = new HashMap<String, Integer>();
	private String projectionString = null;
	private Map<Criteria, ProjectionList> criteriaProjectionListMap;
	private String orderString;

	public QueryBuilder(String entityOrClassName, Map<Criteria, List<Criterion>> criteriaCriterionList, 
			Map<Criteria, ProjectionList> criteriaProjectionListMap, Map<Criteria, List<Order>> criteriaOrderMap)
	{
		this.entityOrClassName = entityOrClassName;
		this.criteriaCriterionList = criteriaCriterionList;
		this.criteriaProjectionListMap = criteriaProjectionListMap;
		this.projectionString = getProjectionString();
		this.orderString = setOrderString(criteriaOrderMap);

		findTablesToBeJoined();

		// Add all the criterion
		for (Map.Entry<Criteria, List<Criterion>> criteriaCriterion : criteriaCriterionList.entrySet())
		{
			for(Criterion criterion: criteriaCriterion.getValue())
				addCriteria(criteriaCriterion.getKey(), criterion);
		}
		if (!selectionArgsList.isEmpty()) {
			selectionArgs = new String[selectionArgsList.size()];
			int index = 0;
			for (String val : selectionArgsList) {
				selectionArgs[index] = val;
				index += 1;
			}
		}



	}

	private String setOrderString(Map<Criteria, List<Order>> criteriaOrderMap) {

		StringBuilder sb = new StringBuilder();
		if(criteriaOrderMap.size() > 0)
		{
			sb.append(" ORDER BY ");
			for (Map.Entry<Criteria, List<Order>> criteriaOrderList : criteriaOrderMap.entrySet()){

				String className = null;
				if (criteriaOrderList.getKey() instanceof SubCriteria)
					className = ((SubCriteria) criteriaOrderList.getKey()).getClassName();
				else
					className = entityOrClassName;
				for(Order order : criteriaOrderList.getValue())
				{
					sb.append(getColumnAlias(getTableNameForCriterion(className, order.getPropertyName()),
							getColumnNameForCriterion(className, order.getPropertyName())));
					if(order.isAscending())
						sb.append(" ASC, ");
					else
						sb.append(" DESC, ");
				}

			}
			sb.replace(sb.length() - 2, sb.length() - 1, " ");
		}
		return sb.toString();
	}

	public QueryDetails getQueryDetails() {
		return queryDetails;
	}

	public void setQueryDetails(QueryDetails queryDetails) {
		this.queryDetails = queryDetails;
	}


	private String getProjectionString(){
		if(criteriaProjectionListMap.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		for (Map.Entry<Criteria, ProjectionList> criteriaProjectionList : criteriaProjectionListMap.entrySet()){
			String className = null;
			if (criteriaProjectionList.getKey() instanceof SubCriteria)
				className = ((SubCriteria) criteriaProjectionList.getKey()).getClassName();
			else
				className = entityOrClassName;
			for (Projection projection : criteriaProjectionList.getValue().getElements()) {
				if (projection instanceof PropertyProjection) {
					PropertyProjection propertyProjection = (PropertyProjection) projection;
					String tableName = getTableNameForCriterion(className, propertyProjection.getPropertyName());
					String columnName = getColumnNameForCriterion(className, propertyProjection.getPropertyName());
					sb.append(tableName)
					.append(".")
					.append(columnName)
					.append(" as ")
					.append(getColumnAlias(tableName, columnName))
					.append(" , ");

				}
			}
		}
		sb.replace(sb.length() - 2, sb.length() - 1, "");
		return sb.toString();
	}

	private void addCriteria(Criteria criteria, Criterion criterion) {

		if (criterion instanceof SimpleExpression) {
			extractSimpleExpression(criteria, (SimpleExpression) criterion);
		} else if (criterion instanceof LogicalExpression) {
			extractLogicalExpression(criteria, (LogicalExpression) criterion);
		} else if (criterion instanceof BetweenExpression) {
			extractBetweenExpression(criteria, (BetweenExpression) criterion);
		} else if (criterion instanceof InExpression) {
			extractInExpression(criteria, (InExpression) criterion);
		}
	}

	private void extractInExpression(Criteria criteria, InExpression ie) {
		String className = null;
		if (criteria instanceof SubCriteria)
			className = ((SubCriteria) criteria).getClassName();
		else
			className = entityOrClassName;
		if (null == selection) {
			selection = "";
		} else {
			selection += "AND ";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("(")
		.append(getTableNameForCriterion(className, ie.getPropertyName()))
		.append(".")
		.append(getColumnNameForCriterion(className, ie.getPropertyName()))
		.append(" IN (");
		String comma = "";
		for (Object value : ie.getValues()) {
			sb.append(comma).append(" ?");
			selectionArgsList.add(String.valueOf(value));
			comma = ", ";
		}
		sb.append(")) ");
		selection += sb.toString();
	}



	private void extractBetweenExpression(Criteria criteria, BetweenExpression be) {
		String className = null;
		if (criteria instanceof SubCriteria)
			className = ((SubCriteria) criteria).getClassName();
		else
			className = entityOrClassName;
		if (null == selection) {
			selection = "("
					+ getTableNameForCriterion(className, be.getPropertyName()) + "."
					+ getColumnNameForCriterion(className, be.getPropertyName())
					+ " BETWEEN " + " ? AND ?) ";
		} else {
			selection += "AND ("
					+ getTableNameForCriterion(className, be.getPropertyName()) + "."
					+ getColumnNameForCriterion(className, be.getPropertyName())
					+ " BETWEEN " + " ? AND ?) ";
		}
		selectionArgsList.add(String.valueOf(be.getLo()));
		selectionArgsList.add(String.valueOf(be.getHi()));
	}

	private void extractSimpleExpression(Criteria criteria, SimpleExpression se) {
		String className = null;
		if (criteria instanceof SubCriteria)
			className = ((SubCriteria) criteria).getClassName();
		else
			className = entityOrClassName;
		if (null == selection) {
			selection = "("
					+ getTableNameForCriterion(className, se.getPropertyName()) + "."
					+ getColumnNameForCriterion(className, se.getPropertyName()) + " "
					+ se.getOp() + " ?) ";
		} else {
			selection += "AND ("
					+ getTableNameForCriterion(className, se.getPropertyName()) + "."
					+ getColumnNameForCriterion(className, se.getPropertyName()) + " "
					+ se.getOp() + " ?) ";
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

	private void extractSimpleExpressionFromLogicalExpression(Criteria criteria,
			SimpleExpression se) {
		String className = null;
		if (criteria instanceof SubCriteria)
			className = ((SubCriteria) criteria).getClassName();
		else
			className = entityOrClassName;
		selection += "("
				+ getTableNameForCriterion(className, se.getPropertyName()) + "."
				+ getColumnNameForCriterion(className, se.getPropertyName()) + " "
				+ se.getOp() + " ?) ";
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


	@SuppressLint("NewApi")
	public List<Map.Entry<String,String[]>> getQuery()
	{
		List<Map.Entry<String, String[]>> queriesAndArguements = new ArrayList<Map.Entry<String, String[]>>();
		ClassDetails classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(entityOrClassName);
		ClassDetails superClassDetails = classDetails;
		String table = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		String comma = "";

		if(projectionString != null)
			sb.append(projectionString);

		else{
			// Iterate over tablesToBeJoined and create aliases of columns.
			for (Map.Entry<String, List<String>> tableColumns : queryDetails.getTablesToBeJoined().entrySet())
			{
				String tableName = tableColumns.getKey();
				for(String column: tableColumns.getValue())
				{

					sb.append(comma).append(tableName.toLowerCase()).append(".").append(column)
					.append(" AS ").append(getColumnAlias(tableName, column));
					comma = ",";
				}
			}
		}

		sb.append(" FROM ");
		sb.append(table);

		sb.append(queryDetails.getJoinString());
		

		// append selection conditions
		if (null != selection) {
			sb.append(" WHERE ").append(selection);
		}

		

		queriesAndArguements.add(new AbstractMap.SimpleEntry<String, String[]>(sb.toString(), selectionArgs));
		return queriesAndArguements;
	}
	
	public String getOrderString()
	{
		return orderString;
	}
	

	/**
	 * finds Tables to be joined from entityOrClassName, 
	 * fills tablesToBeJoined and queryDetails.getTablesJoinConditions()
	 */
	private void findTablesToBeJoined()
	{
		ClassDetails classDetails = null, superClassDetails = null;
		try {
			classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(entityOrClassName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		queue.add(classDetails);

		// Find all the related and inherited classes.
		do
		{
			classDetails = queue.remove(0);

			insertAssociatedClassesIntoQueue(classDetails, 
					(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));

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
					queryDetails.getColumnFieldList().add(new ColumnField(asCol, classDetails.getClassName(), ftd.getFieldName()));
					columnNames.add(col);

				}


				queryDetails.getTablesToBeJoined().put((String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME), 
						columnNames);

				try {
					superClassDetails = classObj != Object.class ? AnnotationsScanner.getInstance()
							.getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass().getName()) : null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(superClassDetails != null && superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null)
				{
					if(superClassDetails.getAnnotationOptionValues()
							.get(Constants.INHERITANCE).get(Constants.STRATEGY)
							.equals(InheritanceType.JOINED)){

						queryDetails.addTableJoinCondition((String)superClassDetails.getAnnotationOptionValues()
								.get(Constants.ENTITY).get(Constants.NAME), 
								"_id",
								(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY)
								.get(Constants.NAME),
								"_id");
						insertAssociatedClassesIntoQueue(superClassDetails, 
								(String)superClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));
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
							queryDetails.getColumnFieldList().add(new ColumnField(asCol, superClassDetails.getClassName(), ftd.getFieldName())); 
							columnNames.add(col);
						}
						ClassDetails tempClassDetails = classDetails;
						while(superClassDetails != null && !superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
								.get(Constants.STRATEGY).equals(InheritanceType.JOINED)){

							insertAssociatedClassesIntoQueue(superClassDetails,
									(String) tempClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));
							classDetails = superClassDetails;

							try {
								classObj = Class.forName(classDetails.getClassName()).getSuperclass();
								superClassDetails = classObj != Object.class ? AnnotationsScanner.getInstance().getEntityObjectDetails(classObj.getName()) : null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(superClassDetails != null)
						{
							queryDetails.addTableJoinCondition((String)superClassDetails.getAnnotationOptionValues()
									.get(Constants.ENTITY).get(Constants.NAME), 
									"_id",
									(String)tempClassDetails.getAnnotationOptionValues().get(Constants.ENTITY)
									.get(Constants.NAME),
									"_id");
						}
						classDetails = superClassDetails;

					}

					if(classDetails != null)
					{
						try {
							classObj = Class.forName(classDetails.getClassName());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
				else classObj = Object.class;

			}while(classObj != Object.class);

		}while( queue.size() > 0);

	}


	private void insertAssociatedClassesIntoQueue(ClassDetails classDetails, String tableName)
	{
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
					relatedClassDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(fieldTypeDetails.getFieldType().getName());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String joinColumn = null;
				if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
				{
					try {
						relatedClassDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(Utils.getCollectionType(classDetails.getClassName(),
								fieldTypeDetails.getFieldName()));
					} catch (IllegalArgumentException e) {
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

				// Add to queryDetails.getTablesJoinConditions() and to queue
				String table1 = (String)relatedClassDetails.getAnnotationOptionValues()
						.get(Constants.ENTITY).get(Constants.NAME);
				String table2 = tableName;
				String joinColumn1 = joinColumn;
				String joinColumn2 = "_id";
				if(!queryDetails.joinExists(table1, joinColumn1, table2, joinColumn2)){
					queryDetails.addTableJoinCondition(table1,joinColumn1,table2,joinColumn2);
					queue.add(relatedClassDetails);
				}
			}
			// else if there is a reference, and foreign key is on this side.
			else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null
					&& fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE).get(Constants.MAPPED_BY).equals("")
					|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null){

				try {
					relatedClassDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(fieldTypeDetails.getFieldType().getName());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String table1 = (String)relatedClassDetails.getAnnotationOptionValues()
						.get(Constants.ENTITY).get(Constants.NAME);
				String table2 = tableName;
				String joinColumn1 = "_id";
				String joinColumn2 = (String)fieldTypeDetails.getAnnotationOptionValues()
						.get(Constants.JOIN_COLUMN).get(Constants.NAME);

				if(!queryDetails.joinExists(table1, joinColumn1, table2, joinColumn2)){
					queryDetails.addTableJoinCondition(table1, joinColumn1, table2, joinColumn2);
					queue.add(relatedClassDetails);
				}

			}
			// else if there is a reference, and it's many to many relationship
			else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_MANY) != null)
			{
				String className = Utils.getCollectionType(classDetails.getClassName(), fieldTypeDetails.getFieldName());
				try {
					relatedClassDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(className);
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
				table2 = (String) tableName;

				if(!queryDetails.joinExists(joinTableName, joinColumnName, table2, "_id") 
						&& !queryDetails.joinExists(table1,"_id",joinTableName, inverseJoinColumnName)){
					queryDetails.addTableJoinCondition(joinTableName, joinColumnName, table2, "_id");
					queryDetails.addTableJoinCondition(table1,"_id",joinTableName, inverseJoinColumnName);
					queue.add(relatedClassDetails);
				}

			}

		}

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
			classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(className);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String tableName = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
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





	private String getColumnAlias(String tableName, String columnName)
	{
		return tableName.toLowerCase() + "_" + columnName;
	}

	private String getTableAlias(String tableName)
	{
		if(tableAliases.get(tableName) == null)
		{
			tableAliases.put(tableName, 0);
		}
		tableAliases.put(tableName, tableAliases.get(tableName) + 1);
		return tableName + "_" + tableAliases.get(tableName);
	}

}
