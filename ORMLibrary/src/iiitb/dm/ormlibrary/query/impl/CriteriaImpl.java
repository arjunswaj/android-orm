package iiitb.dm.ormlibrary.query.impl;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.Projection;

public class CriteriaImpl implements Criteria {

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
    // TODO Auto-generated method stub
    return null;
  }

}
