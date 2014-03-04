package iiitb.dm.ormlibrary.ddl.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	private static final String DDL_TAG = "DDL Statement Builder";

	@Override
	public String generateCreateTableQuery(ClassDetails classDetails)
	{
		// TODO: Error Checking
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get("Entity").get("name");
		Log.d(this.getClass().getName(), "Table name is " + tableName);
		StringBuilder createStmt = new StringBuilder("create table "
				+ tableName + "(");

		for (FieldTypeDetails fieldTypeDetail : classDetails
				.getFieldTypeDetails())
		{
			String columnName = fieldTypeDetail.getAnnotationOptionValues()
					.get("Column").get("name");

			String columnType = SQLColTypeEnumMap.get(
					fieldTypeDetail.getFieldType().getSimpleName()).toString();
			createStmt.append(columnName + " " + columnType + " ");

			if (fieldTypeDetail.getAnnotationOptionValues().get("Id") == null)
				createStmt.append(", ");
			else
				createStmt.append("primary key ,");
		}
		createStmt.replace(createStmt.length() - 2, createStmt.length(), ")");
		Log.d(DDL_TAG, "generateCreateTableQuery() : " + createStmt);
		return createStmt.toString();
	}
}
