package iiitb.dm.ormlibrary.dml.impl;

import java.util.List;

import android.util.Log;
import iiitb.dm.ormlibrary.ddl.FieldValue;
import iiitb.dm.ormlibrary.dml.ClassDetails;
import iiitb.dm.ormlibrary.dml.DMLQueryBuilder;

public class DMLQueryBuilderImpl implements DMLQueryBuilder {  

	@Override
	public String generateInsertQuery(String tableName,
			List<FieldValue> fieldValues) {

		StringBuffer query = new StringBuffer();
		StringBuffer values = new StringBuffer();
		StringBuffer fields = new StringBuffer();
		
		query.append("Insert into "+tableName);
		
			
		for(FieldValue fieldValue : fieldValues)
		{
			fields.append(fieldValue.getFieldName());
			switch(fieldValue.getJavaFieldType())
			{
			case INTEGER:
				values.append(Integer.parseInt(fieldValue.getFieldValue()));
				break;
			case FLOAT:
				values.append(Float.parseFloat(fieldValue.getFieldValue()));
				break;
			case LONG:
				values.append(Long.parseLong(fieldValue.getFieldValue()));
				break;
			case STRING:
				values.append("'" + fieldValue.getFieldValue() + "'");
				break;
			default:
				break;
			}
			fields.append(",");
			values.append(",");
		}

		
		fields.deleteCharAt(fields.length() - 1);
		values.deleteCharAt(values.length() - 1);
		query.append("(" + fields + ")" + " values " + "(" + values + ");");
		Log.d("ADebugTag", query.toString());
		return query.toString();
	}

}
