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
    ClassDetails superClassDetails = null;
    ClassDetails subClassDetails = null;
    Class<?> myClass = obj.getClass();
    // Build the Class Detail Hierarchy
    do {
      try {
        superClassDetails = annotationsScanner.getEntityObjectDetails(myClass);
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

    Map<String, Object> inheritanceOptions = superClassDetails
        .getAnnotationOptionValues().get(INHERITANCE);
    if (null == inheritanceOptions) {
      // Has no Inheritance annotation, Persist it as a plain Object
      id = saveObject(superClassDetails, obj, -1L);
    } else {
      // Has Inheritance annotation. Awesome. Check the Strategy and persist.
      InheritanceType strategy = (InheritanceType) inheritanceOptions
          .get(STRATEGY);
      switch (strategy) {
      case JOINED:
        saveObjectWithInheritanceUsingJoinedStrategy(superClassDetails, obj);
        break;
      case TABLE_PER_CLASS:
        saveObjectWithInheritanceUsingTablePerClassStrategy(superClassDetails,
            obj);
        break;
      default:
        break;

      }
    }
    return id;
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
   * @return
   */
  private long saveObject(ClassDetails superClassDetails, Object obj, long id) {
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
      if (null != discriminator) {
        String discriminatorColumn = (String) discriminator.get(NAME);
        String discriminatorValue = (String) superClassDetails
            .getSubClassDetails().get(0).getAnnotationOptionValues()
            .get(DISCRIMINATOR_VALUE).get(VALUE);
        contentValues.put(discriminatorColumn, discriminatorValue);
        Log.d(SAVE_OBJECT_TAG, "DiscriminatorCol: " + discriminatorColumn
            + ", DiscriminatorVal: " + discriminatorValue);
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
   * Save the Object with all the attributes in a single table
   * 
   * @param classDetails
   *          class details Hierarchy
   * @param obj
   *          object to save
   * @return generated id
   */
  private long saveObjectWithInheritanceUsingTablePerClassStrategy(
      ClassDetails classDetails, Object obj) {
    long genId = -1L;
    String tableName = null;
    ContentValues contentValues = new ContentValues();
    ClassDetails superClassDetails = classDetails;
    while (null != superClassDetails) {
      try {
        tableName = (String) superClassDetails.getAnnotationOptionValues()
            .get(ENTITY).get(NAME);
        Class<?> objClass = Class.forName(superClassDetails.getClassName());
        for (FieldTypeDetails fieldTypeDetail : superClassDetails
            .getFieldTypeDetails()) {
          String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
              .getFieldName());
          Method getterMethod = objClass.getMethod(getterMethodName);

          String columnName = fieldTypeDetail.getAnnotationOptionValues()
              .get(COLUMN).get(NAME);
          String columnValue = getterMethod.invoke(obj).toString();

          // Don't add the id from getter, it has to be auto generated
          if (!columnName.equals(ID) && !columnName.equals(ID_CAPS)) {
            contentValues.put(columnName, columnValue);
            Log.d(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: "
                + columnValue);
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

      List<ClassDetails> subClassDetailsList = superClassDetails
          .getSubClassDetails();
      superClassDetails = (!subClassDetailsList.isEmpty()) ? subClassDetailsList
          .get(0) : null;
    }
    // tableName will be the name of the Table given in the sub class object
    genId = getWritableDatabase().insert(tableName, null, contentValues);
    return genId;
  }

  /**
   * Need to persist the parameters in different tables maintaining FK
   * references
   * 
   * @param classDetails
   *          ClassDetails Hierarchy
   * @param obj
   *          object to persist
   * @return generated id
   */
  private long saveObjectWithInheritanceUsingJoinedStrategy(
      ClassDetails classDetails, Object obj) {
    long id = -1L;
    ClassDetails superClassDetails = classDetails;
    // Persist the super class related attributes first, then go down the
    // hierarchy
    while (null != superClassDetails) {
      id = saveObject(superClassDetails, obj, id);
      List<ClassDetails> subClassDetailsList = superClassDetails
          .getSubClassDetails();
      // It'll be always 0, coz. the hierarchy was built using the obj, not the
      // XML
      superClassDetails = (!subClassDetailsList.isEmpty()) ? subClassDetailsList
          .get(0) : null;
    }
    return id;
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
