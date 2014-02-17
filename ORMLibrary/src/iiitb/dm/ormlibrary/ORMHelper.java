package iiitb.dm.ormlibrary;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ORM Helper
 * @author arjun
 *
 */
public class ORMHelper extends SQLiteOpenHelper {

  Context context;
  public ORMHelper(Context context, String name, CursorFactory factory,
      int version) {    
    super(context, name, factory, version);
    this.context = context;
    // TODO Auto-generated constructor stub
  }

  private String getEOPackage() throws NameNotFoundException {
    ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
    String ormPackage = (String) ai.metaData.get("ormPackage");
    return ormPackage;
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
