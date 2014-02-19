package iiitb.dm.ormlibrary.ddl.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.SQLColTypeEnumMap;
import android.util.Log;

public class DDLStatementBuilderImpl implements DDLStatementBuilder
{

	@Override
	public String generateCreateTableQuery(ClassDetails classDetails)
	{
		Log.d(this.getClass().getSimpleName(), "generateCreateTableQuery()");
		String tableName = classDetails.getAnnotationOptionValues()
				.get("Entity").get("Name");
		StringBuilder sb = new StringBuilder("create table " + tableName + "(");

		for (FieldTypeDetails fieldTypeDetail : classDetails
				.getFieldTypeDetails())
		{
			// TODO: What if there are no columns specified?
			String columnName = fieldTypeDetail.getAnnotationOptionValues()
					.get("Basic").get("name");
			
			Log.d(this.getClass().getSimpleName(), "Looking for " + fieldTypeDetail.getFieldType().getSimpleName());
			String columnType = SQLColTypeEnumMap.get(
					fieldTypeDetail.getFieldType().getSimpleName()).toString();
			sb.append(columnName + " " + columnType + " ");

			if (fieldTypeDetail.getAnnotationOptionValues().get("Id") == null)
				sb.append(", ");
			else
				sb.append("primary key ,");
		}
		sb.replace(sb.length() - 1, sb.length() - 1, ")");
		Log.d(this.getClass().getName(), sb.toString());

		return sb.toString();
	}
}
