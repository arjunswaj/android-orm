package iiitb.dm.ormlibrary.ddl.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import iiitb.dm.ormlibrary.utils.Utils;
import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	private final String DDL_TAG = "DDL STATEMENT BUILDER";
	Map<String, ClassDetails> classDetailsMap;


	public DDLStatementBuilderImpl(Map<String, ClassDetails> classDetailsMap)
	{
		this.classDetailsMap = classDetailsMap;
	}

	@Override
	public String generateCreateTableQuery(ClassDetails classDetails) throws MappingException
	{ 
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get("Entity").get("name");

		List<ColumnDescription> columnsDescription = new ArrayList<ColumnDescription>();
		StringBuilder foreignKeyConstraint = new StringBuilder();
		String columnName = "", columnType = "";
		ArrayList<String> columnConstraints;
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
			columnConstraints = new ArrayList<String>();
			if(fieldTypeDetail.getAnnotationOptionValues().get("OneToOne") != null)
			{
				// If this is the owner class
				if(fieldTypeDetail.getAnnotationOptionValues().get("OneToOne").get("mappedBy") == "")
				{
					if(fieldTypeDetail.getAnnotationOptionValues().get("JoinColumn") != null)
					{
						columnName = fieldTypeDetail.getAnnotationOptionValues().get("JoinColumn").get("name");
						String referencedColumnName = fieldTypeDetail.getAnnotationOptionValues().get("JoinColumn").get("referencedColumnName");
						if(referencedColumnName.equals(""))
						{
							foreignKeyConstraint.append(", FOREIGN KEY (" + columnName + ") REFERENCES "
									+ classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getAnnotationOptionValues()
									.get("Entity").get("name") + "(_id ) ");
							Log.d(DDL_TAG, fieldTypeDetail.getFieldType().getName() + " " + classDetailsMap.get(fieldTypeDetail.getFieldType().getName()));
							columnType = getColumnType(classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getFieldTypeDetails(), "_id");
						}
						else{
							foreignKeyConstraint.append(", FOREIGN KEY (" + columnName + ") REFERENCES "
									+ classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getAnnotationOptionValues()
									.get("Entity").get("name") + "(" + referencedColumnName + ")");
							columnType = getColumnType(classDetailsMap.get(fieldTypeDetail.getFieldType().getName()).getFieldTypeDetails(),
									referencedColumnName);
						}
					}
					else
						throw new MappingException("@JoinColumn missing on " + fieldTypeDetail.getFieldName() + " in " + classDetails.getClassName());
				}
			}
			else{

				columnName = fieldTypeDetail.getAnnotationOptionValues()
						.get("Column").get("name");

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
				if(doesColumnNameExist(columnName, columnsDescription))
				throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
			if(!columnName.equals("") && !columnType.equals(""))
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));

		}

		// Add a discriminator column if this class's inheritance strategy is JOINED.
		if(classDetails.getAnnotationOptionValues().get("Inheritance") != null){
			switch((InheritanceType)classDetails.getAnnotationOptionValues().get("Inheritance").get("strategy"))
			{
			case JOINED:
				columnName = (String)classDetails.getAnnotationOptionValues().get("DiscriminatorColumn").get("name");
				columnType = "TEXT";
				columnConstraints = new ArrayList<String>();
				if(doesColumnNameExist(columnName, columnsDescription))
					throw new MappingException("Duplicate Columns in " + classDetails.getClassName());
				columnsDescription.add(new ColumnDescription(columnName, columnType, columnConstraints));
				break;
			case TABLE_PER_CLASS:
			default:
				break;
			}

		}


		// Depending on this class's super class, either add foreign key or columns of super class.
		if(superClassDetails != null && superClassDetails.getAnnotationOptionValues().get("Inheritance") != null){
			switch((InheritanceType)superClassDetails.getAnnotationOptionValues().get("Inheritance").get("strategy"))
			{
			case JOINED:
				foreignKeyConstraint.append(" , FOREIGN KEY(_id) REFERENCES " 
						+ superClassDetails.getAnnotationOptionValues().get("Entity").get("name")
						+ "(_id)");
				columnName = "_id";
				columnType = "INTEGER";
				columnConstraints = new ArrayList<String>();
				columnConstraints.add("PRIMARY KEY");
				columnConstraints.add("AUTOINCREMENT");
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
		return createStmt;
	}


	private String getColumnType(List<FieldTypeDetails> fieldTypeDetailsList, String columnName)
	{
		String type = null;
		for(FieldTypeDetails fieldTypeDetails: fieldTypeDetailsList)
		{
			if(fieldTypeDetails.getAnnotationOptionValues().get("Column") != null
					&& fieldTypeDetails.getAnnotationOptionValues().get("Column").get("name").equals(columnName))
				type = SQLColTypeEnumMap.get(fieldTypeDetails.getFieldType().getSimpleName()).toString();
		}

		return type;
	}

	private boolean doesColumnNameExist(String ColumnName, List<ColumnDescription> columnsDescription)
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
	


}
