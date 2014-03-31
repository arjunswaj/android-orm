package iiitb.dm.ormlibrary.query.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import javax.persistence.Entity;
import javax.persistence.InheritanceType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.query.Criteria;
import iiitb.dm.ormlibrary.query.Criterion;
import iiitb.dm.ormlibrary.query.criterion.LogicalExpression;
import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;
import iiitb.dm.ormlibrary.query.criterion.PropertyProjection;
import iiitb.dm.ormlibrary.query.criterion.SimpleExpression;
import iiitb.dm.ormlibrary.query.Projection;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.scanner.impl.AnnotationsScannerImpl;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

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
  private AnnotationsScanner annotationsScanner = new AnnotationsScannerImpl();
  private Map<String, ClassDetails> mappingCache;
  /**
   * sqliteDatabase
   */
  private SQLiteDatabase sqliteDatabase;
  /**
   * entityOrClassName
   */
  private String entityOrClassName;
  private Context context;

  public CriteriaImpl(String entityOrClassName, SQLiteDatabase sqliteDatabase, Map<String, ClassDetails> mappingCache, Context context) {
    this.entityOrClassName = entityOrClassName;
    this.sqliteDatabase = sqliteDatabase;
    this.mappingCache = mappingCache;
    this.context = context;
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

  private ClassDetails fetchClassDetailsMapping(Class<?> objClass) {
    ClassDetails subClassDetails = null;
    String objClassName = objClass.getName();
    ClassDetails superClassDetails = mappingCache.get(objClassName);
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
      mappingCache.put(objClassName, superClassDetails);
    }
    return superClassDetails;
  }

  private void queryBuilder(String tableName, ClassDetails superClassDetails,
      StringBuilder sb, Map<FieldTypeDetails, String> colFieldMap,
      List<String> tableNames, List<FieldTypeDetails> otherAttributes, boolean first) {
    String comma = ", ";
    if (first) {
      comma = "";
    }
    for (FieldTypeDetails ftd : superClassDetails.getFieldTypeDetails()) {
      String col = (String) ftd.getAnnotationOptionValues()
          .get(Constants.COLUMN).get(Constants.NAME);
      String asCol = tableName.toLowerCase() + "_" + col;
      colFieldMap.put(ftd, asCol);      
      sb.append(comma).append(tableName.toLowerCase()).append(".").append(col)
          .append(" AS ").append(asCol);
      comma = ", ";
    }
    if (null != otherAttributes) {
      for (FieldTypeDetails ftd : otherAttributes) {
        String col = (String) ftd.getAnnotationOptionValues()
            .get(Constants.COLUMN).get(Constants.NAME);
        String asCol = tableName.toLowerCase() + "_" + col;
        colFieldMap.put(ftd, asCol);
        if (0 == sb.length()) {
          comma = "";
        }
        sb.append(comma).append(tableName.toLowerCase()).append(".")
            .append(col).append(" AS ").append(asCol);
        comma = ", ";
      }
      otherAttributes.clear();
    }
    tableNames.add(tableName);
  }

	// TODO: Ugly???
	private List<String> getEntityObjectsNamesFromManifest(Context context)
			throws XmlPullParserException, IOException {
		String XML_TAG = "XML in CRITERIA";
		Resources resources = context.getResources();
		// TODO: Should get from AndroidManifest
		String uri = "xml/" + "entity_objects";
		XmlResourceParser xpp = resources.getXml(resources.getIdentifier(uri, null,
				context.getPackageName()));
		xpp.next();
		int eventType = xpp.getEventType();
		List<String> eoNames = new ArrayList<String>();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.START_TAG) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.END_TAG) {
				// Log.v(XML_TAG, "We don't need this for now.");
			} else if (eventType == XmlPullParser.TEXT) {
				eoNames.add(xpp.getText());
				Log.v(XML_TAG, "ClassName: " + xpp.getText());
			}
			eventType = xpp.next();
		}
		return eoNames;
	}
	

	@Override
	/**
	 * Get the result of the query
	 */
	public List<?> list()
	{
		// TODO: can I create a superclass list?
		List<?> result = new LinkedList();
		try
		{
			// TODO: Ugly??? Should I be using subClassDetails field here??
			List<String> eoNames = getEntityObjectsNamesFromManifest(context);
			result = list(entityOrClassName, eoNames);
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Get the result of the query for the specified entity class and all its subclasses
	 * @param entityOrClassName Specified entity class
	 * @param eoNames List containing all entity class names in the application
	 * @return result of the query for the specified entity class and all its subclasses
	 */
	private List<?> list(String entityClassName, List<String> eoNames)
	{
		ClassDetails classDetails = null;
		try
		{
			classDetails = new AnnotationsScannerImpl().getEntityObjectDetails(Utils
					.getClassObject(entityClassName));
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		if (classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE) == null)
			return list(entityClassName);

		List result = new LinkedList();
		
		// Collect the result of the specified class
		if (classDetails.getAnnotationOptionValues().get(Constants.INHERITANCE)
				.get(Constants.STRATEGY)
				.equals(InheritanceType.TABLE_PER_CLASS)
				|| (classDetails.getAnnotationOptionValues()
						.get(Constants.INHERITANCE).get(Constants.STRATEGY)
						.equals(InheritanceType.JOINED) && !Modifier
						.isAbstract(Utils.getClassObject(entityClassName)
								.getModifiers())))
			result.addAll(list(entityClassName));

		// Collect the result of all its subclasses
		for (String eoName : eoNames)
			if (Utils.getClassObject(eoName).getSuperclass()
					.equals(Utils.getClassObject(entityClassName)))
				result.addAll(list(eoName, eoNames));
		return result;
	}
  
  private List list(String entityClassName) {
    List result = new ArrayList();
    Cursor cursor = null;
    try {
      Class<?> eoClass = Class.forName(entityClassName);
      ClassDetails classDetails = fetchClassDetailsMapping(eoClass);
      ClassDetails superClassDetails = classDetails;
      if (!selectionArgsList.isEmpty()) {
        selectionArgs = new String[selectionArgsList.size()];
        int index = 0;
        for (String val : selectionArgsList) {
          selectionArgs[index] = val;
          index += 1;
        }
      }

      Map<FieldTypeDetails, String> colFieldMap = new HashMap<FieldTypeDetails, String>();
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT ");

      List<String> tableNames = new ArrayList<String>();
      List<FieldTypeDetails> otherAttributes = new ArrayList<FieldTypeDetails>();
      boolean first = true;
      while (null != superClassDetails) {
        String tableName = (String) superClassDetails
            .getAnnotationOptionValues().get(Constants.ENTITY)
            .get(Constants.NAME);
        Map<String, Object> inheritanceOptions = superClassDetails
            .getAnnotationOptionValues().get(Constants.INHERITANCE);
        if (null == inheritanceOptions) {
          queryBuilder(tableName, superClassDetails, sb, colFieldMap,
              tableNames, otherAttributes, first);
          first = false;
        } else {
          InheritanceType strategy = (InheritanceType) inheritanceOptions
              .get(Constants.STRATEGY);
          switch (strategy) {
          case JOINED:
            queryBuilder(tableName, superClassDetails, sb, colFieldMap,
                tableNames, otherAttributes, first);
            first = false;
            break;
          case TABLE_PER_CLASS:            
            otherAttributes.addAll(superClassDetails.getFieldTypeDetails());
            if (null == superClassDetails.getSubClassDetails()
                || 0 == superClassDetails.getSubClassDetails().size()) {
              queryBuilder(tableName, superClassDetails, sb, colFieldMap,
                  tableNames, otherAttributes, first);
              first = false;
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
      sb.append(" FROM ");
      String prevTable = tableNames.remove(0);
      sb.append(prevTable).append(" ").append(prevTable.toLowerCase());

      for (String tableName : tableNames) {
        sb.append(" JOIN ").append(tableName).append(" ")
            .append(tableName.toLowerCase()).append(" ON ")
            .append(prevTable.toLowerCase()).append("._id").append(" = ")
            .append(tableName.toLowerCase()).append("._id");
        prevTable = tableName;
      }

      if (null != selection) {
        sb.append(" WHERE ").append(selection);
      }
      sb.append(";");
      String sql = sb.toString();
      Log.d("Generated SQL", sql);
      cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
      if (cursor.moveToFirst()) {
        do {
          Object eo = eoClass.newInstance();
          for (Entry<FieldTypeDetails, String> colField : colFieldMap
              .entrySet()) {
            String field = colField.getKey().getFieldName();
            String setterMethodName = Utils.getSetterMethodName(field);

            String fieldType = colField.getKey().getFieldType().getSimpleName();
            String colName = colField.getValue();
            if (fieldType.equals("String")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  String.class);
              String args = cursor.getString(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("Float")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  Float.class);
              float args = cursor.getFloat(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("float")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  float.class);
              float args = cursor.getFloat(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("Integer")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  Integer.class);
              int args = cursor.getInt(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("int")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  int.class);
              int args = cursor.getInt(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("Long")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  Long.class);
              long args = cursor.getLong(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            } else if (fieldType.equals("long")) {
              Method setterMethod = eoClass.getMethod(setterMethodName,
                  long.class);
              long args = cursor.getLong(cursor.getColumnIndex(colName));
              setterMethod.invoke(eo, args);
            }

          }
          result.add(eo);
        } while (cursor.moveToNext());
        cursor.close();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
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

/* (non-Javadoc)
 * @see iiitb.dm.ormlibrary.query.Criteria#createCriteria(java.lang.String)
 */
@Override
public Criteria createCriteria(String relatedEntityFieldName)
{
	// TODO Auto-generated method stub
	return null;
}

}
