package iiitb.dm.ormlibrary.query.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.LogicalExpression;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.query.criterion.PropertyProjection;
import iiitb.dm.ormlibrary.query.criterion.SimpleExpression;
import iiitb.dm.ormlibrary.query.Projection;

public class CriteriaImpl implements Criteria {

  private boolean distinct;
  private String table;
  private String[] columns;
  private String selection;
  private String[] selectionArgs;
  private List<String> selectionArgsList = new ArrayList<String>();
  private String groupBy;
  private String having;
  private String orderBy;
  private String limit;
  private ProjectionList projectionList;
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
    addCriteria(criterion);
    return this;
  }

  private void addCriteria(Criterion criterion) {
    if (criterion instanceof SimpleExpression) {
      extractSimpleExpression((SimpleExpression) criterion);
    } else if (criterion instanceof LogicalExpression) {
      extractLogicalExpression((LogicalExpression) criterion);
    }
  }

  private void extractSimpleExpression(SimpleExpression se) {
    if (null == selection) {
      selection = "(" + se.getPropertyName() + " " + se.getOp() + " ?) ";
    } else {
      selection += "AND (" + se.getPropertyName() + " " + se.getOp() + " ?) ";
    }
    selectionArgsList.add(se.getValue().toString());
  }

  private void addCriteriaFromLogicalExpression(Criterion criterion) {
    if (criterion instanceof SimpleExpression) {
      extractSimpleExpressionFromLogicalExpression((SimpleExpression) criterion);
    } else if (criterion instanceof LogicalExpression) {
      extractLogicalExpression((LogicalExpression) criterion);
    }
  }

  private void extractSimpleExpressionFromLogicalExpression(SimpleExpression se) {
    selection += "(" + se.getPropertyName() + " " + se.getOp() + " ?) ";
    selectionArgsList.add(se.getValue().toString());
  }

  private void extractLogicalExpression(LogicalExpression le) {
    if (null == selection) {
      selection = "(";
    } else {
      selection += "AND (";
    }
    addCriteriaFromLogicalExpression(le.getLhs());
    selection += le.getOp() + " ";
    addCriteriaFromLogicalExpression(le.getRhs());
    selection += ") ";
  }

  @Override
  public Criteria addOrder(Order order) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List list() {
    List result = new ArrayList();
    Cursor cursor = null;
    try {
      Class<?> eoClass = Class.forName(entityOrClassName);
      Entity entity = eoClass.getAnnotation(Entity.class);
      table = entity.name();
      cursor = sqliteDatabase.query(distinct, table, columns, selection,
          selectionArgs, groupBy, having, orderBy, limit);
      if (cursor.moveToFirst()) {
        do {
          Object eo = eoClass.newInstance();

          result.add(eo);
        } while (cursor.moveToNext());
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public Criteria setProjection(ProjectionList projectionList) {
    this.projectionList = projectionList;
    return this;
  }

  @Override
  public Cursor cursor() {
    Cursor cursor = null;
    try {
      Class<?> eoClass = Class.forName(entityOrClassName);
      Entity entity = eoClass.getAnnotation(Entity.class);
      table = entity.name();
      if (!selectionArgsList.isEmpty()) {
        selectionArgs = new String[selectionArgsList.size()];
        int index = 0;
        for (String val : selectionArgsList) {
          selectionArgs[index] = val;
          index += 1;
        }
      }
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT ");
      if (null == this.projectionList) {
        sb.append("* ");
      } else {
        String comma = "";
        for (Projection projection : projectionList.getElements()) {
          if (projection instanceof PropertyProjection) {
            PropertyProjection propertyProjection = (PropertyProjection) projection;
            sb.append(comma).append(propertyProjection.getPropertyName());
          }
          comma = ", ";
        }
        sb.append(" ");
      }      
      sb.append("FROM ");
      sb.append(table).append(" ");
      sb.append("WHERE ").append(selection);
      sb.append(";");
      // cursor = sqliteDatabase.query(distinct, table, columns, selection,
      // selectionArgs, groupBy, having, orderBy, limit);
      String sql = sb.toString();
      Log.d("Generated SQL", sql);
      cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return cursor;
  }

}
