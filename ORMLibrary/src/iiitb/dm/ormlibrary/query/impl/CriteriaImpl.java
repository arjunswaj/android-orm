package iiitb.dm.ormlibrary.query.impl;

import java.util.List;

import javax.persistence.Entity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.Projection;

public class CriteriaImpl implements Criteria {

  private boolean distinct;
  private String table;
  private String[] columns;
  private String selection;
  private String[] selectionArgs;
  private String groupBy;
  private String having;
  private String orderBy;
  private String limit;
  /**
   * sqliteDatabase
   */
  private SQLiteDatabase sqliteDatabase;
  /**
   * entityOrClassName
   */
  private String entityOrClassName;

  public CriteriaImpl(String entityOrClassName, SQLiteDatabase sqliteDatabase) {
    this.entityOrClassName = entityOrClassName;
    this.sqliteDatabase = sqliteDatabase;
  }

  @Override
  public Criteria add(Criterion criterion) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Criteria addOrder(Order order) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List list() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Criteria setProjection(Projection projection) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Cursor cursor() {
    Cursor cursor = null;
    try {
      Class<?> eoClass = Class.forName(entityOrClassName);
      Entity entity = eoClass.getAnnotation(Entity.class);
      table = entity.name();
      cursor = sqliteDatabase.query(distinct, table, columns, selection,
          selectionArgs, groupBy, having, orderBy, limit);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return cursor;
  }

}
