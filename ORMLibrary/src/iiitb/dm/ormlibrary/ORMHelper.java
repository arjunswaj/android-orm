package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.ddl.impl.DDLStatementBuilderImpl;
import iiitb.dm.ormlibrary.ddl.impl.MappingException;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.impl.CriteriaImpl;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * ORM Helper
 * 
 * @author arjun
 * 
 */
public class ORMHelper extends SQLiteOpenHelper {

  private static final String SAVE_OBJECT_TAG = "SAVE_OBJECT";
  private PersistenceHelper persistenceHelper;
  
  private DDLStatementBuilder ddlStatementBuilder;
  private AnnotationsScanner annotationsScanner;

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version); 
    annotationsScanner = AnnotationsScanner.getInstance(context); // Crucial step to ensure use of AnnotationsScanner.getInstance() throughout the library
    persistenceHelper = new PersistenceHelper(getReadableDatabase(), getWritableDatabase());
  }
  
  public void persist(Object obj) {
    long genId = persistenceHelper.save(obj, persistenceHelper.getId(obj), null);
    Log.d(SAVE_OBJECT_TAG, "Saved: " + obj.getClass().getSimpleName()
        + " Generated Id: " + genId);
  }

  /**
   * Create Criteria
   * @param entity entity object
   * @return Criteria Instance
   */
  public Criteria createCriteria(Class<?> entity) {
    return new CriteriaImpl(entity.getName(), getReadableDatabase());
  }  
  
  @Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(this.getClass().getName() + ".onCreate()", "Creating tables");
		Map<String, ClassDetails> classDetailsMap = annotationsScanner
				.getAllEntityObjectDetails();

		ddlStatementBuilder = new DDLStatementBuilderImpl(classDetailsMap);
		db.execSQL("pragma foreign_keys = on;");

		for (Map.Entry<String, ClassDetails> pairs : classDetailsMap.entrySet())
		{
			ClassDetails classDetails = (ClassDetails) pairs.getValue();
			try {
				Log.v("ORM Helper OnCreate",
						Class.forName(classDetails.getClassName())
								.getSuperclass() + " " + Object.class);
				if (Object.class == Class.forName(classDetails.getClassName())
						.getSuperclass()) {
				  createTablesForHierarchy(db, classDetails);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

  /**
   * When an entity class has subclasses inheriting from it, this method
   * creates tables for the entire hierarchy of the entity class in consideration
   * and all its subclasses. 
   * 
   * @param db
   * @param classDetails
   */
	private void createTablesForHierarchy(SQLiteDatabase db,
			ClassDetails classDetails) {
		// Create table/s for this class
		Collection<String> stmts = new LinkedList<String>();
		try{
			stmts = ddlStatementBuilder
					.generateCreateTableStmts(classDetails);
			for (String stmt : stmts)
			{
				Log.v("CreateTablesForHeirarchy", stmt);
				db.execSQL(stmt);
			}
		}
		catch(MappingException ex)
		{
			Log.e("Mapping Exception", ex.getMessage());
		}

		// Create Tables for all the sub classes recursively
		for (ClassDetails subClassDetails : classDetails.getSubClassDetails()) {
			createTablesForHierarchy(db, subClassDetails);
		}
	}
	
	/**
   * When an entity class has subclasses inheriting from it, this method
   * drops tables for the entire hierarchy of the entity class in consideration
   * and all its subclasses. 
   * 
   * @param db
   * @param classDetails
   * @param classDetailsMap
   */
  private void dropTablesForHierarchy(SQLiteDatabase db,
      ClassDetails classDetails, Map<String, ClassDetails> classDetailsMap) {
    // Create table/s for this class
    String table = null;

    for (FieldTypeDetails fieldTypeDetail : classDetails.getFieldTypeDetails()) {
      if (fieldTypeDetail.getAnnotationOptionValues().get(
          Constants.MANY_TO_MANY) != null
          && fieldTypeDetail.getAnnotationOptionValues()
              .get(Constants.MANY_TO_MANY).get(Constants.MAPPED_BY).equals("")) {
        ParameterizedType pType = (ParameterizedType) fieldTypeDetail
            .getFieldGenericType();
        Class<?> inverseSideEntityClass = (Class<?>) pType
            .getActualTypeArguments()[0];

        ClassDetails inverseSide = classDetailsMap
            .get(inverseSideEntityClass.getName());
        
        // owningSide table
        String owningSideTableName = (String) classDetails
            .getAnnotationOptionValues().get(Constants.ENTITY)
            .get(Constants.NAME);

        // inverseSide table
        String inverseSideTableName = (String) inverseSide
            .getAnnotationOptionValues().get(Constants.ENTITY)
            .get(Constants.NAME);

        String joinTableName = owningSideTableName + "_" + inverseSideTableName;
        db.execSQL("DROP TABLE IF EXISTS " + joinTableName);
        Log.d("DROP TABLE", "DROP TABLE IF EXISTS " + joinTableName);
      }
    }
    table = (String) classDetails.getAnnotationOptionValues()
        .get(Constants.ENTITY).get(Constants.NAME);

    db.execSQL("DROP TABLE IF EXISTS " + table);
    Log.d("DROP TABLE", "DROP TABLE IF EXISTS " + table);

    // Create Tables for all the sub classes recursively
    for (ClassDetails subClassDetails : classDetails.getSubClassDetails()) {
      dropTablesForHierarchy(db, subClassDetails, classDetailsMap);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(this.getClass().getName() + ".onUpgrade()", "Upgrading Schema");
    Map<String, ClassDetails> classDetailsMap = annotationsScanner
        .getAllEntityObjectDetails();
    for (Map.Entry<String, ClassDetails> pairs : classDetailsMap.entrySet()) {
      ClassDetails classDetails = (ClassDetails) pairs.getValue();
      try {
        if (Object.class == Class.forName(classDetails.getClassName())
            .getSuperclass()) {
          dropTablesForHierarchy(db, classDetails, classDetailsMap);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    onCreate(db);
  }
}
