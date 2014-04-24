package iiitb.dm.ormlibrary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.InheritanceType;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DeleteHelper {

	private final static String TAG = DeleteHelper.class.getSimpleName();
	private SQLiteDatabase writableDatabase;
	private SQLiteDatabase readableDatabase;
	private AnnotationsScanner annotationsScanner;

	public DeleteHelper(SQLiteDatabase readableDatabase,
			SQLiteDatabase writableDatabase) {
		this.writableDatabase = writableDatabase;
		this.readableDatabase = readableDatabase;
		this.annotationsScanner = annotationsScanner.getInstance();
		writableDatabase.execSQL(" pragma foreign_keys = on;");
	}

	public void delete(Object obj)
	{
		Long id = Utils.getId(obj);
		ClassDetails classDetails = null;

		classDetails = annotationsScanner.getEntityObjectDetailsWithInheritedFields(obj.getClass());

		for(FieldTypeDetails fieldTypeDetails: classDetails.getFieldTypeDetails()){
			// Recursively delete the associated objects.
			if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_ONE) != null 
					&& Arrays.asList((CascadeType[])fieldTypeDetails.getAnnotationOptionValues()
							.get(Constants.ONE_TO_ONE).get(Constants.CASCADE)).contains(CascadeType.DELETE)){
				delete(Utils.getObject(obj, fieldTypeDetails.getFieldName()));

			}
			else if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null 
					&& Arrays.asList((CascadeType[])fieldTypeDetails.getAnnotationOptionValues()
							.get(Constants.ONE_TO_MANY).get(Constants.CASCADE)).contains(CascadeType.DELETE)){
				for(Object relatedObject: (Collection)Utils.getObject(obj, fieldTypeDetails.getFieldName())){
					delete(relatedObject);
				}

			}
		}

		/* Go to top most superclass and delete, everything else will be taken care of by ON DELETE CASCADE */
		ClassDetails superClassDetails = annotationsScanner.getEntityObjectDetails(obj.getClass().getSuperclass().getName());
		while(superClassDetails != null ){
			classDetails = superClassDetails;
			do{
				try {
					superClassDetails = annotationsScanner.getEntityObjectDetails(Class.forName(superClassDetails.getClassName()).getSuperclass().getName());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(superClassDetails != null && superClassDetails.getAnnotationOptionValues()
					.get(Constants.INHERITANCE).get(Constants.STRATEGY).equals(InheritanceType.TABLE_PER_CLASS));

		}
		String query = "DELETE FROM " + classDetails.getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME)
				+ " WHERE _id = " + id;

		Log.d(TAG, query);
		writableDatabase.execSQL(query);

	}
}
