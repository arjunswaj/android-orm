package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.ddl.impl.DDLStatementBuilderImpl;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.scanner.impl.AnnotationsScannerImpl;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
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
    long genId = save(obj);
    // TODO: put this id in the Obj by invoking set_id(genId)
  }

  private long save(Object obj) {
    long id = -1L;
    Class<?> myClass = obj.getClass();
    ClassDetails superClassDetails = mappingCache.get(obj.getClass().getName());
    ClassDetails subClassDetails = null;

    if (null == superClassDetails) {
      // Build the Class Detail Hierarchy
      Log.e("CACHE MISS", "CACHE MISS");
      do {
        try {
          superClassDetails = annotationsScanner
              .getEntityObjectDetails(myClass);
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

      } while (Object.class != (myClass = myClass.getSuperclass()));
      mappingCache.put(obj.getClass().getName(), superClassDetails);
    }
    Map<String, Object> inheritanceOptions = superClassDetails
        .getAnnotationOptionValues().get(INHERITANCE);
    if (null == inheritanceOptions) {
      // Has no Inheritance annotation, Persist it as a plain Object
      id = saveObject(superClassDetails, obj, -1L, null);
    } else {
      // Has Inheritance annotation. Awesome. Check the Strategy and persist.
      id = saveObjectByInheritanceStrategy(superClassDetails, obj);
    }
    return id;
  }

  /**
   * Saves the Object with Inheritance
   * @param classDetails Class Details
   * @param obj Object to be saved
   * @return generated id
   */
  private long saveObjectByInheritanceStrategy(ClassDetails classDetails,
      Object obj) {
    long id = -1L;
    ClassDetails superClassDetails = classDetails;
    Map<String, String> kvp = new HashMap<String, String>();

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
            String tableName = (String) superClassDetails.getAnnotationOptionValues()
                .get(ENTITY).get(NAME);
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
   * @param tableName table name
   * @param kvp KVPs
   * @param id id of the record
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

        String columnName = fieldTypeDetail.getAnnotationOptionValues()
            .get(COLUMN).get(NAME);
        String columnValue = getterMethod.invoke(obj).toString();

        // Don't add the id obtained from getter, it has to be auto generated
        if (!columnName.equals(ID) && !columnName.equals(ID_CAPS)) {
          contentValues.put(columnName, columnValue);
          Log.d(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: " + columnValue);
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
   * @param classDetails class details
   * @param obj object to be saved
   * @param kvp KVPs
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
    classDetailsList = annotationsScanner
        .getEntityObjectCollectionDetails(this.context);
    for (ClassDetails classDetails : classDetailsList) {
      DDLStatementBuilder ddlStatementBuilder = new DDLStatementBuilderImpl();
      String stmt = ddlStatementBuilder.generateCreateTableQuery(classDetails);
      Log.d(this.getClass().getName() + ".onCreate()", stmt);
      db.execSQL(stmt);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO Auto-generated method stub

  }

}
