package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.DDLStatementBuilder;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.ddl.impl.DDLStatementBuilderImpl;
import iiitb.dm.ormlibrary.dml.DMLQueryBuilder;
import iiitb.dm.ormlibrary.dml.impl.DMLQueryBuilderImpl;
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

  Context context;
  private DMLQueryBuilder dmlQueryBuilder;

  private Collection<ClassDetails> classDetailsList = null;

  AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version);
    this.context = context;
    dmlQueryBuilder = new DMLQueryBuilderImpl();
  }

  private String getEOPackage() throws NameNotFoundException {
    ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);
    String ormPackage = (String) ai.metaData.get("ormPackage");
    context.getResources();
    return ormPackage;
  }

  public void persist(Object obj) {

    Class objClass = obj.getClass();
    save(obj);
    // String insertQuery = dmlQueryBuilder.generateInsertQuery(
    // scannedClassesTableMap.get(objClass), fieldValues);
    //
    // getWritableDatabase().execSQL(insertQuery);
  }

  private int save(Object obj) {
    int id = -1;
    ClassDetails superClassDetails = null;
    ClassDetails subClassDetails = null;
    Class<?> myClass = obj.getClass();
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
        .getAnnotationOptionValues().get("Inheritance");
    if (null == inheritanceOptions) {
      // Persist plain Object
      long genId = saveObject(superClassDetails, obj, -1L);
    } else {
      InheritanceType strategy = (InheritanceType) inheritanceOptions
          .get("strategy");
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

  private long saveObject(ClassDetails superClassDetails, Object obj, long id) {
    long genId = -1;
    try {
      String tableName = (String) superClassDetails.getAnnotationOptionValues()
          .get("Entity").get("name");

      Class<?> objClass = Class.forName(superClassDetails.getClassName());
      ContentValues contentValues = new ContentValues();
      for (FieldTypeDetails fieldTypeDetail : superClassDetails
          .getFieldTypeDetails()) {
        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
            .getFieldName());
        Method getterMethod = objClass.getMethod(getterMethodName);

        String columnName = fieldTypeDetail.getAnnotationOptionValues()
            .get("Column").get("name");
        String columnValue = getterMethod.invoke(obj).toString();

        // Don't add the id, it has to be auto generated
        if (!columnName.equals("_id") && !columnName.equals("ID")) {
          contentValues.put(columnName, columnValue);
          Log.d(this.getClass().getName() + " Insert:", "Col: " + columnName
              + ", Val: " + columnValue);
        }
      }

      Map<String, Object> discriminator = superClassDetails
          .getAnnotationOptionValues().get("DiscriminatorColumn");
      if (null != discriminator) {
        String discriminatorColumn = (String) discriminator.get("name");
        String discriminatorValue = (String) superClassDetails
            .getSubClassDetails().get(0).getAnnotationOptionValues()
            .get("DiscriminatorValue").get("value");
        contentValues.put(discriminatorColumn, discriminatorValue);
        Log.d(this.getClass().getName() + " Insert:", "DiscriminatorCol: "
            + discriminatorColumn + ", DiscriminatorVal: " + discriminatorValue);
      }

      if (-1L != id) {
        contentValues.put("_id", id);
        Log.d(this.getClass().getName() + " Insert:", "Col: _id " + ", Val: "
            + id);
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

  private void saveObjectWithInheritanceUsingTablePerClassStrategy(
      ClassDetails superClassDetails, Object obj) {
    // TODO Auto-generated method stub

  }

  private void saveObjectWithInheritanceUsingJoinedStrategy(
      ClassDetails classDetails, Object obj) {
    long id = -1L;
    ClassDetails superClassDetails = classDetails;
    while (null != superClassDetails) {
      id = saveObject(superClassDetails, obj, id);
      List<ClassDetails> subClassDetailsList = superClassDetails
          .getSubClassDetails();
      superClassDetails = (!subClassDetailsList.isEmpty()) ? subClassDetailsList
          .get(0) : null;
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
