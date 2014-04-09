package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.impl.DDLStatementBuilderImpl;
import iiitb.dm.ormlibrary.ddl.impl.MappingException;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.impl.CriteriaImpl;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

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
  private Context context;
  private PersistenceHelper persistenceHelper;
  
  private DDLStatementBuilder ddlStatementBuilder;
  private AnnotationsScanner annotationsScanner;

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version);
    this.context = context;
    annotationsScanner = AnnotationsScanner.getInstance(context);
    persistenceHelper = new PersistenceHelper(annotationsScanner, getReadableDatabase(), getWritableDatabase());
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
    return new CriteriaImpl(entity.getName(), getReadableDatabase(), context);
  }  
  @Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(this.getClass().getName() + ".onCreate()", "Creating tables");
		Map<String, ClassDetails> classDetailsMap = AnnotationsScanner.getInstance(context)
				.getEntityObjectCollectionDetails();

		ddlStatementBuilder = new DDLStatementBuilderImpl(classDetailsMap);
		db.execSQL("pragma foreign_keys = on;");

		Iterator<Entry<String, ClassDetails>> iterator = classDetailsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ClassDetails> pairs = (Map.Entry<String, ClassDetails>) iterator.next();
			ClassDetails classDetails = (ClassDetails) pairs.getValue();
			try {
				Log.v("ORM Helper OnCreate",
						Class.forName(classDetails.getClassName())
								.getSuperclass() + " " + Object.class);
				if (Object.class == Class.forName(classDetails.getClassName())
						.getSuperclass()) {
					createTablesForHeirarchy(db, classDetails);
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
	private void createTablesForHeirarchy(SQLiteDatabase db,
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
			createTablesForHeirarchy(db, subClassDetails);
		}

	}
	
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO Auto-generated method stub

  }

}
