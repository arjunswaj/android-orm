package iiitb.dm.ormlibrary.query.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import android.util.Log;
import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

public class QueryBuilder {
	
	private QueryDetails queryDetails = new QueryDetails();
	
	private List<ClassDetails> queue = new ArrayList<ClassDetails>();
	private String entityOrClassName;
	private Map<Criteria, List<Criterion>> criteriaCriterionList;
	
	public QueryBuilder(String entityOrClassName)
	{
		this.entityOrClassName = entityOrClassName;
		findTablesToBeJoined();
	}
	
	public QueryDetails getQueryDetails() {
		return queryDetails;
	}

	public void setQueryDetails(QueryDetails queryDetails) {
		this.queryDetails = queryDetails;
	}



	
	public String getQuery()
	{
		ClassDetails classDetails = AnnotationsScanner.getInstance().getEntityObjectDetails(entityOrClassName);
		ClassDetails superClassDetails = classDetails;
		String table = (String) classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		String comma = "";

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

		sb.append(" FROM ");
		sb.append(table);

		sb.append(queryDetails.getJoinString());
		
		return sb.toString();
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
		
		Log.v("findTablesToBeJoined ", " eoClassName is " + entityOrClassName);
		Log.v("findTablesToBeJoined ", " classDetailsName is " + classDetails.getClassName());

		// Find all the related and inherited classes.
		do
		{
			classDetails = queue.remove(0);
			Log.v("findTablesToBeJoined", "On class " + classDetails.getClassName());

			insertAssociatedClassesIntoQueue(classDetails, (String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));
			
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
				Log.v(" find ", classDetails.getClassName());

				try {
					superClassDetails = classObj != Object.class ? AnnotationsScanner.getInstance()
							.getEntityObjectDetails(Class.forName(classDetails.getClassName()).getSuperclass().getName()) : null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(superClassDetails != null && superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null)
				{
					Log.v("find", "superClass:" + superClassDetails.getClassName());
					if(superClassDetails.getAnnotationOptionValues()
							.get(Constants.INHERITANCE).get(Constants.STRATEGY)
							.equals(InheritanceType.JOINED)){

						queryDetails.addTableJoinCondition((String)superClassDetails.getAnnotationOptionValues()
								.get(Constants.ENTITY).get(Constants.NAME), 
								"_id",
								(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY)
								.get(Constants.NAME),
								"_id");
						Log.v("find", "" + (String)superClassDetails.getAnnotationOptionValues()
								.get(Constants.ENTITY).get(Constants.NAME) + 
								"_id" + 
								(String)classDetails.getAnnotationOptionValues().get(Constants.ENTITY)
								.get(Constants.NAME)+
								"_id");
						insertAssociatedClassesIntoQueue(superClassDetails, (String)superClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));
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
							Log.v(" find ", "Skipping " + classDetails.getClassName());
							
							insertAssociatedClassesIntoQueue(superClassDetails,(String) tempClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME));
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

					try {
						classObj = Class.forName(classDetails.getClassName());
					} catch (Exception e) {
						e.printStackTrace();
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
								|| fieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null)
						{

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

							if(!queryDetails.joinExists(table1, joinColumn1, table2, joinColumn2))
							{
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
									&& !queryDetails.joinExists(table1,"_id",joinTableName, inverseJoinColumnName))
							{
								queryDetails.addTableJoinCondition(joinTableName, joinColumnName, table2, "_id");
								queryDetails.addTableJoinCondition(table1,"_id",joinTableName, inverseJoinColumnName);
								queue.add(relatedClassDetails);
							}

						}

					}

	}


	
	private String getColumnAlias(String tableName, String columnName)
	{
		return tableName.toLowerCase() + "_" + columnName;
	}

}
