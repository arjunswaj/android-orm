package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.ddl.impl.DDLStatementBuilderImpl;
import iiitb.dm.ormlibrary.ddl.impl.MappingException;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.impl.CriteriaImpl;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.scanner.impl.AnnotationsScannerImpl;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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

  private static final String INHERITANCE = "Inheritance";
  private static final String STRATEGY = "strategy";
  private static final String ENTITY = "Entity";
  private static final String NAME = "name";
  private static final String COLUMN = "Column";
  private static final String ONE_TO_ONE = "OneToOne";
  private static final String ONE_TO_MANY = "OneToMany";
  private static final String JOIN_COLUMN = "JoinColumn";
  private static final String DISCRIMINATOR_COLUMN = "DiscriminatorColumn";
  private static final String DISCRIMINATOR_VALUE = "DiscriminatorValue";
  private static final String VALUE = "value";
  private static final String ID = "_id";
  private static final String ID_CAPS = "ID";
  private static final String SAVE_OBJECT_TAG = "SAVE_OBJECT";
  Context context;

  private Collection<ClassDetails> classDetailsList = null;
  private Map<String, ClassDetails> mappingCache = new HashMap<String, ClassDetails>();
  AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();
  private DDLStatementBuilder ddlStatementBuilder;

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version);
    this.context = context;
  }

  private String getEOPackage() throws NameNotFoundException {
    ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);
    String ormPackage = (String) ai.metaData.get("ormPackage");
    context.getResources();
    return ormPackage;
  }

  public void persist(Object obj) {
    long genId = save(obj, -1L, null);
    // TODO: put this id in the Obj by invoking set_id(genId)
  }

  /**
   * Create Criteria
   * @param entity entity object
   * @return Criteria Instance
   */
  public Criteria createCriteria(Class<?> entity) {
    return new CriteriaImpl(entity.getName(), getReadableDatabase());
  }
  
  
  private ClassDetails fetchClassDetailsMapping(Object obj) {
    Class<?> objClass = obj.getClass();
    ClassDetails subClassDetails = null;
    ClassDetails superClassDetails = mappingCache.get(obj.getClass().getName());
    if (null == superClassDetails) {
      Log.e("CACHE MISS", "CACHE MISS for " + objClass.getName());
      do {
        try {
          superClassDetails = annotationsScanner
              .getEntityObjectDetails(objClass);
          if (null != subClassDetails) {
            superClassDetails.getSubClassDetails().add(subClassDetails);
          }
          subClassDetails = superClassDetails;
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }

      } while (Object.class != (objClass = objClass.getSuperclass()));
      mappingCache.put(obj.getClass().getName(), superClassDetails);
    }
    return superClassDetails;
  }

  private long save(Object obj, long id, Map<String, String> passedKVPs) {
    ClassDetails classDetails = fetchClassDetailsMapping(obj);
    Map<String, Object> inheritanceOptions = classDetails
        .getAnnotationOptionValues().get(INHERITANCE);
    if (null == inheritanceOptions) {
      // Has no Inheritance annotation, Persist it as a plain Object
      id = saveObject(classDetails, obj, id, passedKVPs);
    } else {
      // Has Inheritance annotation. Awesome. Check the Strategy and persist.
      id = saveObjectByInheritanceStrategy(classDetails, obj, passedKVPs);
    }
    return id;
  }

  /**
   * Saves the Object with Inheritance
   * 
   * @param classDetails
   *          Class Details
   * @param obj
   *          Object to be saved
   * @return generated id
   */
  private long saveObjectByInheritanceStrategy(ClassDetails classDetails,
      Object obj, Map<String, String> passedKVPs) {
    long id = -1L;
    ClassDetails superClassDetails = classDetails;
    Map<String, String> kvp = new HashMap<String, String>();
    if (null != passedKVPs) {
      kvp.putAll(passedKVPs);
    }

    while (null != superClassDetails) {
      Map<String, Object> inheritanceOptions = superClassDetails
          .getAnnotationOptionValues().get(INHERITANCE);
      if (null == inheritanceOptions) {
        id = saveObject(superClassDetails, obj, id, kvp);
      } else {
        InheritanceType strategy = (InheritanceType) inheritanceOptions
            .get(STRATEGY);
        switch (strategy) {
        case JOINED:
          // Save Object with KVPs
          id = saveObject(superClassDetails, obj, id, kvp);
          break;
        case TABLE_PER_CLASS:
          // Get the KVPs
          populateKVPsOfTablePerClassStrategy(superClassDetails, obj, kvp);
          if (obj.getClass().getName().equals(superClassDetails.getClassName())) {
            String tableName = (String) superClassDetails
                .getAnnotationOptionValues().get(ENTITY).get(NAME);
            id = saveTheKVPs(tableName, kvp, id);
          }
          break;
        default:
          break;
        }
      }
      List<ClassDetails> subClassDetailsList = superClassDetails
          .getSubClassDetails();
      superClassDetails = (!subClassDetailsList.isEmpty()) ? subClassDetailsList
          .get(0) : null;
    }
    return id;
  }

  /**
   * Save the KVPs in the table. Used for Table per class design
   * 
   * @param tableName
   *          table name
   * @param kvp
   *          KVPs
   * @param id
   *          id of the record
   * @return generated id
   */
  private long saveTheKVPs(String tableName, Map<String, String> kvp, long id) {
    long genId = -1;
    ContentValues contentValues = new ContentValues();

    if (null != kvp) {
      for (String key : kvp.keySet()) {
        contentValues.put(key, kvp.get(key));
      }
      kvp.clear();
    }

    if (-1L != id) {
      contentValues.put(ID, id);
      Log.d(SAVE_OBJECT_TAG, "Col: _id " + ", Val: " + id);
    }
    genId = getWritableDatabase().insert(tableName, null, contentValues);
    return genId;
  }

  /**
   * Save the Object to DB
   * 
   * @param superClassDetails
   *          class hierarchy
   * @param obj
   *          object to save
   * @param id
   *          ID of the object to be saved. -1L if needs to be ignored and se
   *          the auto generated value
   * @param kvp
   *          Additional Key Value Pairs to be persisted
   * @return
   */
  private long saveObject(ClassDetails superClassDetails, Object obj, long id,
      Map<String, String> kvp) {
    long genId = -1;
    try {
      String tableName = (String) superClassDetails.getAnnotationOptionValues()
          .get(ENTITY).get(NAME);

      Class<?> objClass = Class.forName(superClassDetails.getClassName());
      ContentValues contentValues = new ContentValues();
      for (FieldTypeDetails fieldTypeDetail : superClassDetails
          .getFieldTypeDetails()) {
        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
            .getFieldName());
        Method getterMethod = objClass.getMethod(getterMethodName);

        if (null != fieldTypeDetail.getAnnotationOptionValues().get(COLUMN)) {
          String columnName = fieldTypeDetail.getAnnotationOptionValues()
              .get(COLUMN).get(NAME);
          String columnValue = getterMethod.invoke(obj).toString();

          // Don't add the id obtained from getter, it has to be auto generated
          if (!columnName.equals(ID) && !columnName.equals(ID_CAPS)) {
            contentValues.put(columnName, columnValue);
            Log.d(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: "
                + columnValue);
          }
        } else if (null != fieldTypeDetail.getAnnotationOptionValues().get(ONE_TO_ONE)) {
          // Handle 1-1 Composition here
          Object composedObject = getterMethod.invoke(obj);
          String joinColumnName = fieldTypeDetail.getAnnotationOptionValues()
              .get(JOIN_COLUMN).get(NAME);          
          long saveId = save(composedObject, -1L, null);
          if (null == kvp) {
            kvp = new  HashMap<String, String>();            
          }
          kvp.put(joinColumnName, String.valueOf(saveId));          
        }
      }

      // Put the Discriminator. The column is in Super class, value is in the
      // Sub class
      Map<String, Object> discriminator = superClassDetails
          .getAnnotationOptionValues().get(DISCRIMINATOR_COLUMN);
      if (null != discriminator
          && !superClassDetails.getSubClassDetails().isEmpty()) {
        String discriminatorColumn = (String) discriminator.get(NAME);
        String discriminatorValue = (String) superClassDetails
            .getSubClassDetails().get(0).getAnnotationOptionValues()
            .get(DISCRIMINATOR_VALUE).get(VALUE);
        contentValues.put(discriminatorColumn, discriminatorValue);
        Log.d(SAVE_OBJECT_TAG, "DiscriminatorCol: " + discriminatorColumn
            + ", DiscriminatorVal: " + discriminatorValue);
      }

      // Save additional KVPs
      if (null != kvp) {
        for (String key : kvp.keySet()) {
          contentValues.put(key, kvp.get(key));
        }
        kvp.clear();
      }

      if (-1L != id) {
        // Okay, add id only if explicitly passed
        contentValues.put(ID, id);
        Log.d(SAVE_OBJECT_TAG, "Col: _id " + ", Val: " + id);
      }

      genId = getWritableDatabase().insert(tableName, null, contentValues);

      // Save 1-Many and other Composed Objects
      for (FieldTypeDetails fieldTypeDetail : superClassDetails
          .getFieldTypeDetails()) {
        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
            .getFieldName());
        Method getterMethod = objClass.getMethod(getterMethodName);
        if (null != fieldTypeDetail.getAnnotationOptionValues().get(
            ONE_TO_MANY)) {
          Collection<Object> composedObjectCollection = (Collection<Object>) getterMethod
              .invoke(obj);
          String joinColumnName = fieldTypeDetail.getAnnotationOptionValues()
              .get(JOIN_COLUMN).get(NAME);
          Map<String, String> newKVPs = new HashMap<String, String>();          
          for (Object composedObject : composedObjectCollection) {
            newKVPs.put(joinColumnName, String.valueOf(genId));
            long saveId = save(composedObject, -1L, null);
          }
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return genId;
  }

  /**
   * Populating the KVPs of Table per class strategy
   * 
   * @param classDetails
   *          class details
   * @param obj
   *          object to be saved
   * @param kvp
   *          KVPs
   */
  private void populateKVPsOfTablePerClassStrategy(ClassDetails classDetails,
      Object obj, Map<String, String> kvp) {
    Class<?> objClass;
    try {
      objClass = Class.forName(classDetails.getClassName());

      for (FieldTypeDetails fieldTypeDetail : classDetails
          .getFieldTypeDetails()) {
        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
            .getFieldName());
        Method getterMethod = objClass.getMethod(getterMethodName);

        String columnName = fieldTypeDetail.getAnnotationOptionValues()
            .get(COLUMN).get(NAME);
        String columnValue = getterMethod.invoke(obj).toString();

        // Don't add the id from getter, it has to be auto generated
        if (!columnName.equals(ID) && !columnName.equals(ID_CAPS)) {
          kvp.put(columnName, columnValue);
          Log.d(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: " + columnValue);
        }
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  @Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(this.getClass().getName() + ".onCreate()", "Creating tables");
		Map<String, ClassDetails> classDetailsMap = annotationsScanner
				.getEntityObjectCollectionDetails(this.context);
		

		ddlStatementBuilder = new DDLStatementBuilderImpl(classDetailsMap);
		db.execSQL("pragma foreign_keys = on;");

		Iterator iterator = classDetailsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pairs = (Map.Entry) iterator.next();
			ClassDetails classDetails = (ClassDetails) pairs.getValue();
			try {
				Log.d("ORM Helper OnCreate",
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

	private void createTablesForHeirarchy(SQLiteDatabase db,
			ClassDetails classDetails) {
		// Create table for this class
		String stmt;
		try{
			stmt = ddlStatementBuilder.generateCreateTableQuery(classDetails);
			Log.d("CreateTablesForHeirarchy", stmt);
			db.execSQL(stmt);
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
