package iiitb.dm.ormlibrary;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.InheritanceType;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class PersistenceHelper {

	private static final String SAVE_OBJECT_TAG = "SAVE_OBJECT";
	  
	private Map<String, ClassDetails> mappingCache = new HashMap<String, ClassDetails>();
	private SQLiteDatabase writableDatabase;
	private SQLiteDatabase readableDatabase;
	
	public PersistenceHelper(SQLiteDatabase readableDatabase, SQLiteDatabase writableDatabase)
	{
		this.writableDatabase = writableDatabase;
		this.readableDatabase = readableDatabase;
	}
	  /**
	   * Gets the ClassDetails object corresponding to the specified object.
	   * Does caching to ensure that the scanning itself is done only once.
	   * 
	   * 
	   * @param obj
	   * @return
	   */
	  public ClassDetails fetchClassDetailsMapping(Object obj) {
	    Class<?> objClass = obj.getClass();
	    ClassDetails subClassDetails = null;
	    ClassDetails superClassDetails = mappingCache.get(obj.getClass().getName());
	    if (null == superClassDetails) {
	      Log.e("CACHE MISS", "CACHE MISS for " + objClass.getName());
	      do {
	        try {
	          superClassDetails = AnnotationsScanner.getInstance()
	              .getEntityObjectBranch(objClass.getName());
	          if (null != subClassDetails) {
	            superClassDetails.getSubClassDetails().add(subClassDetails);
	          }
	          subClassDetails = superClassDetails;
	        } catch (IllegalArgumentException e) {
	          e.printStackTrace();
	        }

	      } while (Object.class != (objClass = objClass.getSuperclass()));
	      mappingCache.put(obj.getClass().getName(), superClassDetails);
	    }
	    return superClassDetails;
	  }

	  public long getId(Object obj)
	  {
			Long id = Long.valueOf(-1L);
			ClassDetails superClassDetails = fetchClassDetailsMapping(obj);
			for (FieldTypeDetails fieldTypeDetail : superClassDetails
					.getFieldTypeDetails())
			{
				// TODO: What if the object being saved inherits and does not have
				// an @ID annotation??
				// This can't probably happen as I am dealing with superClassDetails
				// Need to think this over.
				if (fieldTypeDetail.getAnnotationOptionValues().get(Constants.ID) != null)
				{
					String getterMethodName = Utils
							.getGetterMethodName(fieldTypeDetail.getFieldName());
					Method getterMethod;
					try
					{
						getterMethod = obj.getClass().getMethod(getterMethodName);
						if ((Long) getterMethod.invoke(obj) != 0)
							id = (Long) getterMethod.invoke(obj);
					}
					catch (NoSuchMethodException e)
					{
						e.printStackTrace();
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
					break;
				}
			}
			return id;
	  }
	  
	  public long save(Object obj, long id, Map<String, String> passedKVPs) {
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
		    Method setterMethod;
		    try {
		    	// TODO: Get the setter method instead of assuming that it will be "setId"
		    	// TODO: Get the type of the formal parameter instead of assuming it to be long
		      setterMethod = obj.getClass().getMethod("setId", long.class);
		      setterMethod.invoke(obj, id);
		    } catch (NoSuchMethodException e) {
		      e.printStackTrace();
		    } catch (IllegalAccessException e) {
		      e.printStackTrace();
		    } catch (IllegalArgumentException e) {
		      e.printStackTrace();
		    } catch (InvocationTargetException e) {
		      e.printStackTrace();
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
	      contentValues.put(Constants.ID_VALUE, id);
	      Log.v(SAVE_OBJECT_TAG, "Col: _id " + ", Val: " + id);
	    }
	    genId = writableDatabase.insert(tableName, null, contentValues);
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
	   *          ID of the object to be saved. -1L if needs to be ignored and else
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
	          Object columnValue = getterMethod.invoke(obj);

	          // Don't add the id obtained from getter, it has to be auto generated
            if (!columnName.equals(Constants.ID_VALUE)
                && !columnName.equals(Constants.ID_VALUE_CAPS)
                && null != columnValue) {
              contentValues.put(columnName, columnValue.toString());
              Log.v(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: "
                  + columnValue);
            }
	        } else if (null != fieldTypeDetail.getAnnotationOptionValues().get(Constants.ONE_TO_ONE)) {
	          // Handle 1-1 Composition here
	          handleOneToOneComposition(getterMethod, obj, fieldTypeDetail, kvp);          
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
	        Log.v(SAVE_OBJECT_TAG, "DiscriminatorCol: " + discriminatorColumn
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
	        contentValues.put(Constants.ID_VALUE, id);
	        Log.v(SAVE_OBJECT_TAG, "Col: _id " + ", Val: " + id);
	      }

	      // Insert into the database only if not already present
	      genId = insertIntoDBIfNotAlreadyPresent(id, tableName, contentValues);

	      // Save 1-Many and other Composed Objects
	      handleOneToManyAndManyToMany(superClassDetails, objClass, obj, genId);
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

	  private void handleOneToManyAndManyToMany(ClassDetails superClassDetails,
	      Class<?> objClass, Object obj, long genId) {
	    try {
	      for (FieldTypeDetails fieldTypeDetail : superClassDetails
	          .getFieldTypeDetails()) {
	        String getterMethodName = Utils.getGetterMethodName(fieldTypeDetail
	            .getFieldName());
	        Method getterMethod = null;

	        getterMethod = objClass.getMethod(getterMethodName);

	        if (null != fieldTypeDetail.getAnnotationOptionValues().get(
	            Constants.ONE_TO_MANY)) {
	          Collection<Object> composedObjectCollection = (Collection<Object>) getterMethod
	              .invoke(obj);
	          String joinColumnName = (String) fieldTypeDetail
	              .getAnnotationOptionValues().get(Constants.JOIN_COLUMN)
	              .get(Constants.NAME);
	          Map<String, String> newKVPs = new HashMap<String, String>();
	          for (Object composedObject : composedObjectCollection) {
	            newKVPs.put(joinColumnName, String.valueOf(genId));
	            // TODO : Make use of these newKVP's when bidirectional is
	            // implemented
	            long saveId = save(composedObject, -1L, newKVPs);
	          }
	        } else if (fieldTypeDetail.getAnnotationOptionValues().get(
	            Constants.MANY_TO_MANY) != null
	            && fieldTypeDetail.getAnnotationOptionValues()
	                .get(Constants.MANY_TO_MANY).get(Constants.MAPPED_BY)
	                .equals("")) {
	          // for clarity of semantics for Abhijith
	          // TODO: Will this will fail when the owning side is a subclass
	          // because of the semantics of superClassDetails??
	          ClassDetails owningSide = superClassDetails;
	          Collection<Object> composedObjectCollection = (Collection<Object>) getterMethod
	              .invoke(obj);

	          ParameterizedType pType = (ParameterizedType) fieldTypeDetail
	              .getFieldGenericType();
	          Class<?> inverseClass = (Class<?>) pType.getActualTypeArguments()[0];
	          ClassDetails inverseSide = AnnotationsScanner.getInstance()
	              .getEntityObjectBranch(inverseClass.getName());

	          // joinColumnName is different if there is a reverse mapping
	          FieldTypeDetails joinColumnFieldTypeDetails = inverseSide
	              .getFieldTypeDetailsByMappedByAnnotation(fieldTypeDetail
	                  .getFieldName());
	          String joinColumnName;
	          if (joinColumnFieldTypeDetails == null)
	            joinColumnName = (String) owningSide.getAnnotationOptionValues()
	                .get(Constants.ENTITY).get(Constants.NAME);
	          else
	            joinColumnName = joinColumnFieldTypeDetails.getFieldName();
	          joinColumnName += "_"
	              + owningSide.getFieldTypeDetailsOfId()
	                  .getAnnotationOptionValues().get(Constants.COLUMN)
	                  .get(Constants.NAME);

	          String inverseJoinColumnName = fieldTypeDetail.getFieldName()
	              + "_"
	              + inverseSide.getFieldTypeDetailsOfId()
	                  .getAnnotationOptionValues().get(Constants.COLUMN)
	                  .get(Constants.NAME);

	          String joinTableName = owningSide.getAnnotationOptionValues()
	              .get(Constants.ENTITY).get(Constants.NAME)
	              + "_"
	              + inverseSide.getAnnotationOptionValues().get(Constants.ENTITY)
	                  .get(Constants.NAME);

            if (null != composedObjectCollection) {
              for (Object composedObject : composedObjectCollection) {
                ContentValues joinTableContentValues = new ContentValues();
                joinTableContentValues.put(joinColumnName, genId);
                joinTableContentValues.put(inverseJoinColumnName,
                    save(composedObject, getId(composedObject), null));
                if (writableDatabase.insert(joinTableName, null,
                    joinTableContentValues) == -1)
                  Log.e(SAVE_OBJECT_TAG, "Error inserting into database");
              }
            }
	        }
	      }
	    } catch (NoSuchMethodException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    } catch (IllegalAccessException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    } catch (IllegalArgumentException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    } catch (InvocationTargetException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	  }

	  private long insertIntoDBIfNotAlreadyPresent(long id, String tableName,
	      ContentValues contentValues) {
	    long genId = 0;
	    String[] strs = new String[1];
	    strs[0] = Long.valueOf(id).toString();
	    if (readableDatabase.rawQuery(
	        "select * from " + tableName + " where " + Constants.ID_VALUE + " = ?",
	        strs).getCount() == 0)
	    // TODO: can I use '*' here??
	    {
	      genId = writableDatabase.insert(tableName, null, contentValues);
	      Log.d(SAVE_OBJECT_TAG, genId + ": Inserted into " + tableName);
	    } else {
	      Log.d(SAVE_OBJECT_TAG, id + ": Already present in " + tableName);
	      genId = id;
	    }
	    return genId;
	  }

	  private void handleOneToOneComposition(Method getterMethod, Object obj,
	      FieldTypeDetails fieldTypeDetail, Map<String, String> kvp)
	      throws IllegalAccessException, IllegalArgumentException,
	      InvocationTargetException {
	    Object composedObject = getterMethod.invoke(obj);
	    String joinColumnName = (String) fieldTypeDetail
	        .getAnnotationOptionValues().get(Constants.JOIN_COLUMN)
	        .get(Constants.NAME);
      if (null != composedObject) {
        long saveId = save(composedObject, -1L, null);
        if (null == kvp) {
          kvp = new HashMap<String, String>();
        }
        kvp.put(joinColumnName, String.valueOf(saveId));
      }
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

	        if (null != fieldTypeDetail.getAnnotationOptionValues().get(
	            Constants.COLUMN)) {
	          String columnName = (String) fieldTypeDetail
	              .getAnnotationOptionValues().get(Constants.COLUMN)
	              .get(Constants.NAME);
	          Object columnValue = getterMethod.invoke(obj);

	          // Don't add the id from getter, it has to be auto generated
            if (!columnName.equals(Constants.ID_VALUE)
                && !columnName.equals(Constants.ID_VALUE_CAPS)
                && null != columnValue) {
              kvp.put(columnName, columnValue.toString());
              Log.v(SAVE_OBJECT_TAG, "Col: " + columnName + ", Val: "
                  + columnValue);
            }
	        } else {
	          new UnsupportedOperationException(
	              "We aren't supporting Composition with TABLE_PER_CLASS strategy for now");
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


}
