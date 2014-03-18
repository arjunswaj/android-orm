package iiitb.dm.ormlibrary.ddl.impl;

import javax.persistence.InheritanceType;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	private final String DDL_TAG = "DDL STATEMENT BUILDER";

	@Override
	public String generateCreateTableQuery(ClassDetails classDetails, ClassDetails superClassDetails)
	{ 
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get("Entity").get("name");

		StringBuilder columnsDescription = new StringBuilder();
		String foreignKeyConstraint = "";
		for (FieldTypeDetails fieldTypeDetail : classDetails
				.getFieldTypeDetails())
		{
			String columnName = fieldTypeDetail.getAnnotationOptionValues()
					.get("Column").get("name");

			String columnType = SQLColTypeEnumMap.get(
					fieldTypeDetail.getFieldType().getSimpleName()).toString();
			columnsDescription.append(columnName + " " + columnType + " ");

			if (fieldTypeDetail.getAnnotationOptionValues().get("Id") == null)
				columnsDescription.append(", ");
			else
			{
				columnsDescription.append("primary key autoincrement, ");

			}
		}

		// Add a discriminator column if this class's inheritance strategy is JOINED.
		if(classDetails.getAnnotationOptionValues().get("Inheritance") != null){
			switch((InheritanceType)classDetails.getAnnotationOptionValues().get("Inheritance").get("strategy"))
			{
			case JOINED:
				columnsDescription.append(classDetails.getAnnotationOptionValues().get("DiscriminatorColumn").get("name") + " TEXT, ");
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
				foreignKeyConstraint = " , FOREIGN KEY(_id) REFERENCES " + superClassDetails.getAnnotationOptionValues().get("Entity").get("name") + "(_id)";
				columnsDescription.append(" _id INTEGER primary key autoincrement, ");
				break;
			case TABLE_PER_CLASS:
			default:
				columnsDescription.append(superClassDetails.getColumnsDescription() + ", ");
				break;

			}
		}
		columnsDescription.replace(columnsDescription.length() - 2,
				columnsDescription.length(), "");
		classDetails.setColumnsDescription(columnsDescription.toString());

		String createStmt = "create table " + tableName + "( "
				+ columnsDescription + foreignKeyConstraint + " )";
		Log.d(DDL_TAG, createStmt);
		return createStmt;
	}
}
