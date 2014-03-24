package iiitb.dm.ormlibrary.ddl.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.RelationshipType;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;

import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	private final String DDL_TAG = "DDL STATEMENT BUILDER";
	private Map<String, ClassDetails> classDetailsMap;

	public DDLStatementBuilderImpl(Map<String, ClassDetails> classDetailsMap)
	{
		this.classDetailsMap = classDetailsMap;
	}

	@Override
	public Collection<String> generateCreateTableStmts(ClassDetails classDetails) throws MappingException
	{ 
		Collection<String> createStmts = new LinkedList<String>();
		
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get(Constants.ENTITY).get(Constants.NAME);

		List<ColumnDescription> columnsDescription = new ArrayList<ColumnDescription>();
		StringBuilder foreignKeyConstraint = new StringBuilder();
		String columnName = "", columnType = "";
		ArrayList<String> columnConstraints = null;
		String superClassColumnDescription = "";

		ClassDetails superClassDetails = null;
		try {
			superClassDetails = classDetailsMap.get(Class.forName(classDetails.getClassName()).getSuperclass().getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		for (FieldTypeDetails fieldTypeDetail : classDetails
				.getFieldTypeDetails())
		{
			Log.d(DDL_TAG, " " + classDetails.getClassName() + " " +fieldTypeDetail.getFieldName() );
			columnName = "";
			columnType= "";
			columnConstraints = new ArrayList<String>();
			if(fieldTypeDetail.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null)
			{
				// If this is the owner class
				if(fieldTypeDetail.getAnnotationOptionValues().get(Constants.ONE_TO_ONE).get(Constants.MAPPED_BY) == "")
				{
					if(fieldTypeDetail.getAnnotationOptionValues().get(Constants.JOIN_COLUMN) != null)
					{
						columnName = (String) fieldTypeDetail.getAnnotationOptionValues().get(Constants.JOIN_COLUMN).get(Constants.NAME);
						String referencedColumnName = (String) fieldTypeDetail.getAnnotationOptionValues().get(Constants.JOIN_COLUMN).get(Constants.REFERENCED_COLUMN_NAME);
						if(referencedColumnName.equals(""))
						{
							foreignKeyConstraint.append(", FOREIGN KEY (" + columnName + ") REFERENCES "
									+ classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getAnnotationOptionValues()
									.get(Constants.ENTITY).get(Constants.NAME) + "(_id ) ");
							Log.d(DDL_TAG, fieldTypeDetail.getFieldType().getName() + " " + classDetailsMap.get(fieldTypeDetail.getFieldType().getName()));
							columnType = getColumnType(classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getFieldTypeDetails(), Constants.ID);
						}
						else{
							foreignKeyConstraint.append(", FOREIGN KEY (" + columnName + ") REFERENCES "
									+ classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getAnnotationOptionValues()
									.get(Constants.ENTITY).get(Constants.NAME) + "(" + referencedColumnName + ")");
							columnType = getColumnType(classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getFieldTypeDetails(),
									referencedColumnName);
						}
					}
					else
						throw new MappingException("@" + Constants.JOIN_COLUMN + " missing on " + fieldTypeDetail.getFieldName() + " in " + classDetails.getClassName());
				}
			}
			else if(fieldTypeDetail.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
			{

			}
			else if(fieldTypeDetail.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null)
			{
				columnName = (String) fieldTypeDetail.getAnnotationOptionValues().get(Constants.JOIN_COLUMN).get(Constants.NAME);
				foreignKeyConstraint.append(", FOREIGN KEY (" + columnName + ") REFERENCES "
						+ classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getAnnotationOptionValues()
						.get(Constants.ENTITY).get(Constants.NAME) + "(_id ) ");
				Log.d(DDL_TAG, fieldTypeDetail.getFieldType().getName() + " " + classDetailsMap.get(fieldTypeDetail.getFieldType().getName()));
				columnType = getColumnType(classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getFieldTypeDetails(), Constants.ID);
			}
			else if (fieldTypeDetail.getAnnotationOptionValues().get(
					Constants.MANY_TO_MANY) != null)
			{
				// TODO: Validation
				ParameterizedType pType = (ParameterizedType) fieldTypeDetail
						.getFieldGenericType();
				Class<?> nonOwnedSideEntityClass = (Class<?>) pType
						.getActualTypeArguments()[0];

				ClassDetails ownedSideClassDetails = classDetailsMap
						.get(nonOwnedSideEntityClass.getName());
				createStmts.add(generateJoinTableCreateStmt(classDetails,
						ownedSideClassDetails, fieldTypeDetail));
			}
			else{

				columnName = (String) fieldTypeDetail.getAnnotationOptionValues()
						.get(Constants.COLUMN).get(Constants.NAME);

				columnType = SQLColTypeEnumMap.get(
						fieldTypeDetail.getFieldType().getSimpleName()).toString();

				if (fieldTypeDetail.getAnnotationOptionValues().get("Id") != null)
				{
					if(!columnName.equals("_id") || !columnType.equals("INTEGER"))
						throw new MappingException("Column name must be _id and type must be INTEGER/INT " + fieldTypeDetail.getFieldName());
					columnConstraints.add("PRIMARY KEY");
					columnConstraints.add("AUTOINCREMENT");
				}

			}

			if(columnNameExists(columnName, columnsDescription))
				throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
			if(!columnName.equals("") && !columnType.equals(""))
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));

		}

		// Scan through owned Relations and create columns and foreign key constraints
		for(ClassDetails relatedClassDetails : classDetails.getOwnedRelations().get(RelationshipType.MANY_TO_ONE))
		{
			columnName = "";
			columnType = "";
			for(FieldTypeDetails fieldTypeDetails : relatedClassDetails.getFieldTypeDetails())
			{

				if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
				{
					ParameterizedType genericType = null;
					try {
						genericType = (ParameterizedType)Class.forName(relatedClassDetails.getClassName())
								.getDeclaredField(fieldTypeDetails.getFieldName())
								.getGenericType();
					} catch (NoSuchFieldException e) {
						throw new MappingException("OneToMany should be a Collection Type");
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					try{
						if((Class<?>)genericType.getActualTypeArguments()[0] == Class.forName(classDetails.getClassName()))
						{
							columnName = (String) fieldTypeDetails.getAnnotationOptionValues().get(Constants.JOIN_COLUMN).get(Constants.NAME);
							columnType = getColumnType(relatedClassDetails.getFieldTypeDetails(), Constants.ID);
							columnConstraints = new ArrayList<String>();
							foreignKeyConstraint.append(" , FOREIGN KEY(" + columnName + ") REFERENCES " 
									+ relatedClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME)
									+ "(_id)");
						}
					}
					catch(ClassNotFoundException ex)
					{
						ex.printStackTrace();
					}
				}
			}

			if(columnNameExists(columnName, columnsDescription))
				throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
			if(!columnName.equals("") && !columnType.equals(""))
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));
		}



		// Add a discriminator column if this class's inheritance strategy is JOINED.
		if(classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null){
			switch((InheritanceType)classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE).get(Constants.STRATEGY))
			{
			case JOINED:
				columnName = (String)classDetails.getAnnotationOptionValues().get("DiscriminatorColumn").get(Constants.NAME);
				columnType = "TEXT";
				columnConstraints = new ArrayList<String>();
				if(columnNameExists(columnName, columnsDescription))
					throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));
				break;
			case TABLE_PER_CLASS:
			default:
				break;
			}

		}


		// Depending on this class's super class, either add foreign key or columns of super class.
		if(superClassDetails != null && superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) != null){
			switch((InheritanceType)superClassDetails.getAnnotationOptionValues().get(Constants.INHERITANCE).get(Constants.STRATEGY))
			{
			case JOINED:
				foreignKeyConstraint.append(" , FOREIGN KEY(_id) REFERENCES " 
						+ superClassDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME)
						+ "(_id)");
				columnName = "_id";
				columnType = "INTEGER";
				columnConstraints = new ArrayList<String>();
				columnConstraints.add("PRIMARY KEY");
				columnConstraints.add("AUTOINCREMENT");
				if(columnNameExists(columnName, columnsDescription))
					throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));
				break;
			case TABLE_PER_CLASS:
			default:
				superClassColumnDescription = ", " + superClassDetails.getColumnsDescription();
				break;

			}
		}

		String columnsDescriptionString = generateColumnDescriptionString(columnsDescription);
		classDetails.setColumnsDescription(columnsDescriptionString);
		String createStmt = "CREATE TABLE " + tableName + "( "
				+ columnsDescriptionString + superClassColumnDescription + foreignKeyConstraint.toString() +  " )";
		Log.d(DDL_TAG, createStmt);
		createStmts.add(createStmt);
		return createStmts;
	}


	// TODO: Can be replaced with ClassDetails.getFieldTypeDetailsByColumnName(String columnName)
	private String getColumnType(List<FieldTypeDetails> fieldTypeDetailsList, String columnName)
	{
		String type = null;
		for(FieldTypeDetails fieldTypeDetails: fieldTypeDetailsList)
		{
			if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.COLUMN) != null
					&& fieldTypeDetails.getAnnotationOptionValues().get(Constants.COLUMN).get(Constants.NAME).equals(columnName))
				type = SQLColTypeEnumMap.get(fieldTypeDetails.getFieldType().getSimpleName()).toString();
		}

		return type;
	}

	private boolean columnNameExists(String ColumnName, List<ColumnDescription> columnsDescription)
	{
		boolean result = false;
		for(ColumnDescription columnDescription : columnsDescription)
		{
			if(columnDescription.getColumnName().equals(ColumnName))
				result = true;
		}
		return result;
	}

	private String generateColumnDescriptionString(List<ColumnDescription> columnsDescription)
	{
		StringBuilder query = new StringBuilder();
		for(ColumnDescription columnDescription : columnsDescription)
		{
			query.append(columnDescription.getColumnName() + " " + columnDescription.getColumnType());
			for(String constraint : columnDescription.getColumnConstraints())
				query.append(" " + constraint + " ");
			query.append(", ");
		}
		query.delete(query.length() - 2, query.length() - 1);
		return query.toString();

	}

	/**
	 * Generates the join table for the m:n relation 
	 * 
	 * @param owningSide The ClassDetails object of the entity class where the 
	 * join table, if not defaulted, has been specified.
	 * 
	 * @param inverseSide The ClassDetails object of the entity corresponding to
	 * the other side of the many-to-many relationship.
	 * 
	 * @param mappedField The FieldTypeDetails object containing the mapping 
	 * details of the many-to-many relation(corresponds to a field of the owning
	 *  side)
	 * 
	 * @return SQL statement for creation of the join table
	 */
	// TODO : Error checking to be done. Can error checking be separated from 
	// the table creation logic?
	private String generateJoinTableCreateStmt(ClassDetails owningSide,
			ClassDetails inverseSide, FieldTypeDetails mappedField)
	{
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
		
		String joinColumnName = owningSideTableName
				+ "_"
				+ owningSide.getFieldTypeDetailsOfId()
						.getAnnotationOptionValues().get(Constants.COLUMN)
						.get(Constants.NAME);
		
		String joinColumnType = SQLColTypeEnumMap.get(
				owningSide.getFieldTypeDetailsOfId().getFieldType()
						.getSimpleName()).toString();

		String inverseJoinColumnName = mappedField.getFieldName() + "_"
				+ inverseSide.getFieldTypeDetailsOfId()
				.getAnnotationOptionValues().get(Constants.COLUMN)
				.get(Constants.NAME);
		
		String inverseJoinColumnType = SQLColTypeEnumMap.get(
				inverseSide.getFieldTypeDetailsOfId().getFieldType()
				.getSimpleName()).toString();

		String createStmt = "create table " + joinTableName + "("
				+ joinColumnName + " "
				+ joinColumnType + " references " + owningSideTableName + "("
				+ owningSide.getFieldTypeDetailsOfId()
					.getAnnotationOptionValues().get(Constants.COLUMN)
					.get(Constants.NAME) + "), "
				+ inverseJoinColumnName + " "
				+ inverseJoinColumnType + " references " + inverseSideTableName + "("
				+ inverseSide.getFieldTypeDetailsOfId()
					.getAnnotationOptionValues().get(Constants.COLUMN)
					.get(Constants.NAME) + "))";
		Log.d(DDL_TAG, createStmt);

		return createStmt;
	}
}