package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.dml.DMLQueryBuilder;
import iiitb.dm.ormlibrary.ddl.FieldValue;
import iiitb.dm.ormlibrary.dml.impl.DMLQueryBuilderImpl;
import iiitb.dm.ormlibrary.dml.ClassDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.scanner.ClassScanner;
import iiitb.dm.ormlibrary.scanner.ScanResult;
import iiitb.dm.ormlibrary.scanner.impl.AnnotationsScannerImpl;
import iiitb.dm.ormlibrary.scanner.impl.ClassScannerImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ORM Helper
 * 
 * @author arjun
 * 
 */
public class ORMHelper extends SQLiteOpenHelper {

  Context context;
  private DMLQueryBuilder dmlQueryBuilder;
  Map<Class, List<FieldValue>> scannedClassesFieldsMap;
  Map<Class, String> scannedClassesTableMap;
  ClassScanner scanner;

  List<ClassDetails> classDetails = null;

  AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();

  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {
    super(context, name, factory, version);
    this.context = context;
//    classDetails = annotationsScanner.getEntityObjectDetails(this.context);
    dmlQueryBuilder = new DMLQueryBuilderImpl();
    scannedClassesFieldsMap = new HashMap<Class, List<FieldValue>>();
    scannedClassesTableMap = new HashMap<Class, String>();
    scanner = new ClassScannerImpl();
  }

  private String getEOPackage() throws NameNotFoundException {
    ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
        context.getPackageName(), PackageManager.GET_META_DATA);
    String ormPackage = (String) ai.metaData.get("ormPackage");
    context.getResources();
    return ormPackage;
  }
  
  
  public void persist(Object obj)
  {
	  
	  Class objClass = obj.getClass();
	  
	  if(scannedClassesFieldsMap.containsKey(objClass) == false)
	  {
		  ScanResult result = scanner.scan(objClass);
		  scannedClassesFieldsMap.put(objClass, result.getFieldValues());
		  scannedClassesTableMap.put(objClass, result.getTableName());
		  
	  }
	  List<FieldValue> fieldValues = new ArrayList<FieldValue>(scannedClassesFieldsMap.get(objClass));
	  for(FieldValue fieldValue : fieldValues)
	  {
		  try {
			fieldValue.getField().setAccessible(true);
			fieldValue.setFieldValue(fieldValue.getField().get(obj).toString());
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	  }
	  String insertQuery = dmlQueryBuilder.generateInsertQuery(scannedClassesTableMap.get(objClass), fieldValues);
	  
	  getWritableDatabase().execSQL(insertQuery);
  }

  @Override
  public void onCreate(SQLiteDatabase arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO Auto-generated method stub

  }

}
