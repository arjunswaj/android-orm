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
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.InheritanceType;

import android.content.ContentValues;
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
  Context context;

  private Map<String, ClassDetails> mappingCache = new HashMap<String, ClassDetails>();
  AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();
  private DDLStatementBuilder ddlStatementBuilder;

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version);
    this.context = context;
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
  
  
  /**
   * Gets the ClassDetails object corressponding to the specified object.
   * Does caching to ensure that the scanning itself is done only once.
   * 
   * 
   * @param obj
   * @return
   */
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
        .getAnnotationOptionValues().get(Constants.INHERITANCE);
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
          .getAnnotationOptionValues().get(Constants.INHERITANCE);
      if (null == inheritanceOptions) {
        id = saveObject(superClassDetails, obj, id, kvp);
      } else {
        InheritanceType strategy = (InheritanceType) inheritanceOptions
            .get(Constants.STRATEGY);
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
                .getAnnotationOptionValues().get(Constants.ENTITY).get(Constants.NAME);
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
      contentValues.put(Constants.ID, id);
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
          .get(Constants.ENTITY).get(Constants.NAME);

      Class<?> objClass = Class.forName(superClassDetails.getClassName());
      ContentValues contentValues = new ContentValues();
      for (FieldTypeDetails fieldTypeDetail : superClassDetails
          .getFieldTypeDetails()) {
        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
            .getFieldName());
        Method getterMethod = objClass.getMethod(getterMethodName);

        if (null != fieldTypeDetail.getAnnotationOptionValues().get(Constants.COLUMN)) {
          String columnName = (String) fieldTypeDetail.getAnnotationOptionValues()
              .get(Constants.COLUMN).get(Constants.NAME);
          String columnValue = getterMethod.invoke(obj).toString();

          // Don't add the id obtained from getter, it has to be auto generated
          if (!columnName.equals(Constants.ID) && !columnName.equals(Constants.ID_CAPS)) {
            contentValues.put(columnName, columnValue);
            Log.d(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: "
                + columnValue);
          }
        } else if (null != fieldTypeDetail.getAnnotationOptionValues().get(Constants.ONE_TO_ONE)) {
          // Handle 1-1 Composition here
          Object composedObject = getterMethod.invoke(obj);
          String joinColumnName = (String) fieldTypeDetail.getAnnotationOptionValues()
              .get(Constants.JOIN_COLUMN).get(Constants.NAME);          
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
          .getAnnotationOptionValues().get(Constants.DISCRIMINATOR_COLUMN);
      if (null != discriminator
          && !superClassDetails.getSubClassDetails().isEmpty()) {
        String discriminatorColumn = (String) discriminator.get(Constants.NAME);
        String discriminatorValue = (String) superClassDetails
            .getSubClassDetails().get(0).getAnnotationOptionValues()
            .get(Constants.DISCRIMINATOR_VALUE).get(Constants.VALUE);
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
        contentValues.put(Constants.ID, id);
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
        		Constants.ONE_TO_MANY)) {
          Collection<Object> composedObjectCollection = (Collection<Object>) getterMethod
              .invoke(obj);
          String joinColumnName = (String) fieldTypeDetail.getAnnotationOptionValues()
              .get(Constants.JOIN_COLUMN).get(Constants.NAME);
          Map<String, String> newKVPs = new HashMap<String, String>();          
          for (Object composedObject : composedObjectCollection) {
            newKVPs.put(joinColumnName, String.valueOf(genId));
            // TODO : Make use of these newKVP's when bidirectional is implemented
            long saveId = save(composedObject, -1L, null);
          }
        }
        else if (fieldTypeDetail.getAnnotationOptionValues()
        		.get(Constants.MANY_TO_MANY) != null)
        {
        	// for clarity of semantics for Abhijith
        	ClassDetails ownerSide = superClassDetails;
        	Collection<Object> composedObjectCollection = 
        			(Collection<Object>) getterMethod.invoke(obj);
				
			String joinColumnName = ownerSide
					.getAnnotationOptionValues().get(Constants.ENTITY)
					.get(Constants.NAME)
					+ "_"
					+ ownerSide.getFieldTypeDetailsOfId()
						.getAnnotationOptionValues().get(Constants.COLUMN)
						.get(Constants.NAME);

			ParameterizedType pType = (ParameterizedType) fieldTypeDetail
					.getFieldGenericType();
			Class<?> inverseClass = (Class<?>) pType
					.getActualTypeArguments()[0];
			// TODO: Scanning once again. 
			// Need to have another field called compositionClassDetails
			// which has information about all composed objects in the
			// owner class
			// DANGER : This ClassDetails object doesn't have its 
			// inheritance and ownedRelations members populated
			ClassDetails inverseSide = annotationsScanner
					.getEntityObjectDetails(inverseClass);
			
			String inverseJoinColumnName = fieldTypeDetail.getFieldName() + "_"
					+ inverseSide.getFieldTypeDetailsOfId()
					.getAnnotationOptionValues().get(Constants.COLUMN)
					.get(Constants.NAME);
					
			String joinTableName = ownerSide.getAnnotationOptionValues()
					.get(Constants.ENTITY).get(Constants.NAME) 
					+  "_" 
					+ inverseSide.getAnnotationOptionValues()
					.get(Constants.ENTITY).get(Constants.NAME);

			for (Object composedObject : composedObjectCollection)
			{
				ContentValues joinTableContentValues = new ContentValues();
				joinTableContentValues.put(joinColumnName, genId);
				joinTableContentValues.put(inverseJoinColumnName,
						save(composedObject, -1L, null));
				if (getWritableDatabase().insert(joinTableName, null,
				    joinTableContentValues) == -1)
					Log.e(SAVE_OBJECT_TAG, "Error inserting into database");
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

        String columnName = (String) fieldTypeDetail.getAnnotationOptionValues()
            .get(Constants.COLUMN).get(Constants.NAME);
        String columnValue = getterMethod.invoke(obj).toString();

        // Don't add the id from getter, it has to be auto generated
        if (!columnName.equals(Constants.ID) && !columnName.equals(Constants.ID_CAPS)) {
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

		Iterator<Entry<String, ClassDetails>> iterator = classDetailsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ClassDetails> pairs = (Map.Entry<String, ClassDetails>) iterator.next();
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

  /**
   * When an entity class has subclasses inheriting from it, this method
   * creates tables for the entire hierarchy of the entity class in consideration
   * and all its subclasses. 
   * 
   * TODO : Talk with kumudini and shift the check for a superclass to inside
   *        function. - Abhijith
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
				Log.d("CreateTablesForHeirarchy", stmt);
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
