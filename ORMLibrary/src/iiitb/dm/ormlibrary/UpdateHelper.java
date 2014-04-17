package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map.Entry;

import javax.persistence.InheritanceType;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class UpdateHelper
{
	private final static String TAG = UpdateHelper.class.getSimpleName();
	private SQLiteDatabase writableDatabase;
	private SQLiteDatabase readableDatabase;

	public UpdateHelper(SQLiteDatabase readableDatabase,
			SQLiteDatabase writableDatabase)
	{
		this.writableDatabase = writableDatabase;
		this.readableDatabase = readableDatabase;
	}

	/**
	 * Update the row/s of the table/s corresponding to the specified
	 * entity object
	 * 
	 * @param classdetails The ClassDetails object of an entity class which
	 *            corresponds to the table which needs to be updated
	 * @param obj The specified object which contains data members used to
	 *            update the table
	 * 
	 */
	public void update(ClassDetails classDetails, Object obj)
	{
		if ((Long) Utils.getMemberObject(obj,
				Utils.getFieldTypeDetailsOfId(classDetails.getClassName())
						.getFieldName()) < 1)
		{
			Log.d(TAG, "Non-persistant object. Persisting...");
			new PersistenceHelper(readableDatabase, writableDatabase).save(obj,
					-1, null);
		}

		// First update row/s corresponding to all the association objects
		for (FieldTypeDetails fieldTypeDetails : classDetails
				.getFieldTypeDetails())
		{
			if (fieldTypeDetails.getAnnotationOptionValues().get(
					Constants.ONE_TO_ONE) != null)
			{
				Object associationObject = Utils.getMemberObject(obj,
						fieldTypeDetails.getFieldName());
				update(AnnotationsScanner.getInstance().getEntityObjectDetails(
						fieldTypeDetails.getFieldType().getName()),
						associationObject);
			}
			else if (fieldTypeDetails.getAnnotationOptionValues().get(
					Constants.ONE_TO_MANY) != null
					|| fieldTypeDetails.getAnnotationOptionValues().get(
							Constants.MANY_TO_MANY) != null)
			{
				Collection<Object> associationObjectCollection = (Collection<Object>) Utils
						.getMemberObject(obj, fieldTypeDetails.getFieldName());

				ParameterizedType pType = (ParameterizedType) fieldTypeDetails
						.getFieldGenericType();
				Class<?> associationClass = (Class<?>) pType
						.getActualTypeArguments()[0];
				ClassDetails associationClassDetails = AnnotationsScanner
						.getInstance().getEntityObjectDetails(
								associationClass.getName());

				if (fieldTypeDetails.getAnnotationOptionValues().get(
						Constants.ONE_TO_MANY) != null)
				{

					for (Object associationObject : associationObjectCollection)
					{
						update(associationClassDetails, associationObject);

						// now update the foreign key in the association object
						// table
						ContentValues contentValues = new ContentValues();
						String columnName = (String) fieldTypeDetails
								.getAnnotationOptionValues()
								.get(Constants.JOIN_COLUMN).get(Constants.NAME);
						String columnValue = Utils.getMemberObject(
								obj,
								Utils.getFieldTypeDetailsOfId(
										classDetails.getClassName())
										.getFieldName()).toString();
						contentValues.put(columnName, columnValue);
						updateTableOfEntityClass(associationClassDetails,
								associationObject, contentValues);
					}
				}
				else
				{
					String joinTableName = Utils.getJoinTableName(classDetails,
							associationClassDetails);

					String joinColumnName = Utils.getJoinColumnName(
							classDetails, fieldTypeDetails,
							associationClassDetails);

					String inverseJoinColumnName = Utils
							.getInverseJoinColumnName(associationClassDetails,
									fieldTypeDetails);

					String joinColumnValue[] = new String[1];
					joinColumnValue[0] = Utils
							.getMemberObject(
									obj,
									Utils.getFieldTypeDetailsOfId(
											classDetails.getClassName())
											.getFieldName()).toString();

					// First delete all mappings in the join table
					Log.d(TAG,
							"Removing mappings associated with object with id = "
									+ joinColumnValue[0]
									+ " from the join table, " + joinTableName);
					writableDatabase.delete(joinTableName, joinColumnName
							+ " = ? ", joinColumnValue);

					// update association objects and insert new mappings
					for (Object associationObject : associationObjectCollection)
					{
						update(associationClassDetails, associationObject);
						String inverseColumnValue = Utils.getMemberObject(
								associationObject,
								Utils.getFieldTypeDetailsOfId(
										associationClassDetails.getClassName())
										.getFieldName()).toString();

						// Now insert possible new mappings into the join table
						ContentValues contentValues = new ContentValues();
						contentValues.put(joinColumnName, joinColumnValue[0]);
						contentValues.put(inverseJoinColumnName,
								inverseColumnValue);
						Log.d(TAG,
								"Inserting mappings afresh into the join table, "
										+ joinTableName);
						for (Entry<String, Object> columnNameAndValue : contentValues
								.valueSet())
							Log.d(TAG, columnNameAndValue.getKey() + " : "
									+ columnNameAndValue.getValue());
						writableDatabase.insert(joinTableName, null,
								contentValues);
					}
				}
			}
		}

		// Now update row/s corresponding to this object
		ClassDetails superClassDetails = AnnotationsScanner.getInstance()
				.getEntityObjectDetails(
						Utils.getClassObject(classDetails.getClassName())
								.getSuperclass().getName());
		if (superClassDetails == null)
		{
			updateTableOfEntityClass(classDetails, obj,
					getContentValues(classDetails, obj));
		}
		else if (superClassDetails.getAnnotationOptionValues().get(
				Constants.INHERITANCE) != null
				&& superClassDetails.getAnnotationOptionValues()
						.get(Constants.INHERITANCE).get(Constants.STRATEGY) == InheritanceType.TABLE_PER_CLASS)
		{
			updateTableOfEntityClass(classDetails, obj,
					getInheritedContentValues(classDetails, obj));
		}
		else
		// Join table strategy
		{
			update(superClassDetails, obj);
			updateTableOfEntityClass(classDetails, obj,
					getContentValues(classDetails, obj));
		}
	}

	/**
	 * Update a row in the table corresponding to the specified ClassDetails
	 * object with the provided content values
	 * 
	 * @param classDetails The specified ClassDetails object
	 * @param obj The object containing the key for the row to be updated
	 * @param contentValues The provided content values
	 */
	private void updateTableOfEntityClass(ClassDetails classDetails,
			Object obj, ContentValues contentValues)
	{
		String tableName = (String) classDetails.getAnnotationOptionValues()
				.get(Constants.ENTITY).get(Constants.NAME);

		String whereArgs[] = new String[1];
		whereArgs[0] = Utils.getMemberObject(
				obj,
				Utils.getFieldTypeDetailsOfId(classDetails.getClassName())
						.getFieldName()).toString();

		Log.d(TAG, "Updating record with id " + whereArgs[0] + " in "
				+ tableName);
		for (Entry<String, Object> columnNameAndValue : contentValues
				.valueSet())
			Log.d(TAG,
					columnNameAndValue.getKey() + " : "
							+ columnNameAndValue.getValue());

		writableDatabase.update(tableName, contentValues, Constants.ID_VALUE
				+ " = ? ", whereArgs);
	}

	/**
	 * Get content values from the specified object for the entire inheritance
	 * hierarchy starting with the table corresponding to the specified
	 * ClassDetails object
	 * 
	 * @param classDetails The ClassDetails object corresponding to the start
	 *            of the inheritance hierarchy(bottom up)
	 * @param obj The object from whom the values are to be extracted
	 * @return List of required column names and respective values
	 */
	private ContentValues getInheritedContentValues(ClassDetails classDetails,
			Object obj)
	{
		ClassDetails superClassDetails = AnnotationsScanner.getInstance()
				.getEntityObjectDetails(
						Utils.getClassObject(classDetails.getClassName())
								.getSuperclass().getName());

		ContentValues contentValues = getContentValues(classDetails, obj);

		// Collect contentValue pairs of superclass only if the inheritance
		// strategy is table-per-class
		if (superClassDetails != null
				&& superClassDetails.getAnnotationOptionValues()
						.get(Constants.INHERITANCE).get(Constants.STRATEGY) == InheritanceType.TABLE_PER_CLASS)
		{
			ContentValues superClassContentValues = getInheritedContentValues(
					AnnotationsScanner.getInstance().getEntityObjectDetails(
							Utils.getClassObject(classDetails.getClassName())
									.getSuperclass().getName()), obj);
			contentValues.putAll(superClassContentValues);
		}
		return contentValues;
	}

	/**
	 * Get content values from the specified object from the table corresponding
	 * to the specified ClassDetails object
	 * 
	 * @param classDetails The ClassDetails object corresponding to the start
	 *            of the inheritance hierarchy(bottom up)
	 * @param obj The object from whom the values are to be extracted
	 * @return List of column names and respective values
	 */
	private ContentValues getContentValues(ClassDetails classDetails, Object obj)
	{
		ContentValues contentValues = new ContentValues();
		for (FieldTypeDetails fieldTypeDetails : classDetails
				.getFieldTypeDetails())
		{
			if (fieldTypeDetails.getAnnotationOptionValues().get(Constants.ID) != null)
				continue;

			if (fieldTypeDetails.getAnnotationOptionValues().get(
					Constants.COLUMN) != null)
			{
				String columnName = (String) fieldTypeDetails
						.getAnnotationOptionValues().get(Constants.COLUMN)
						.get(Constants.NAME);
				String columnValue = Utils.getMemberObject(obj,
						fieldTypeDetails.getFieldName()).toString();
				contentValues.put(columnName, columnValue);
			}
			else if (fieldTypeDetails.getAnnotationOptionValues().get(
					Constants.ONE_TO_ONE) != null)
			{
				String columnName = (String) fieldTypeDetails
						.getAnnotationOptionValues().get(Constants.JOIN_COLUMN)
						.get(Constants.NAME);
				Object associationObject = Utils.getMemberObject(obj,
						fieldTypeDetails.getFieldName());
				String columnValue = (Utils.getMemberObject(
						associationObject,
						Utils.getFieldTypeDetailsOfId(
								fieldTypeDetails.getFieldType().getName())
								.getFieldName())).toString();
				contentValues.put(columnName, columnValue);
			}
		}
		return contentValues;
	}
}
