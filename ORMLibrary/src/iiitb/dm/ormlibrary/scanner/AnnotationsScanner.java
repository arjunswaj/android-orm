package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.RelationshipType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class AnnotationsScanner {

	private Map<String, ClassDetails> classDetailsMap = null;
	private static final String XML_TAG = "XML Scanner";
	private static final String ANNOTATION_TAG = "Annotation Scanner";
	private static AnnotationsScanner instance;

	private AnnotationsScanner(Context context)
	{
		classDetailsMap = getAllEntityObjectDetails(context); 
	}
	
	public static AnnotationsScanner getInstance(Context context)
	{
		if(instance == null)
			instance = new AnnotationsScanner(context);
		return instance;
	}
	public static AnnotationsScanner getInstance()
	{
		if (instance == null)
			throw new RuntimeException("Internal Error: Wasn't ORMHelper used at all before this??");
		return instance;
	}
	
	public ClassDetails getEntityObjectDetails(String className)
	{
		return classDetailsMap.get(className);
	}
	
	/**
	 * getEntityObjectBranch is used in ORM Helper.
	 * Don't change this. Bad things will happen.
	 * @param className className
	 * @return ClassDetails branch - NOT the complete Hierarchy
	 */
	public ClassDetails getEntityObjectBranch(String className) {
	  ClassDetails classDetails = null;
    try {
      classDetails =  getEntityObjectDetails(Class.forName(className));
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return classDetails;
	}
	
	public Map<String, ClassDetails> getAllEntityObjectDetails()
	{
		return classDetailsMap;
	}
	
	private List<String> getEntityObjectsNamesFromManifest(Context context)
			throws XmlPullParserException, IOException {
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

	private Map<String, ClassDetails> getAllEntityObjectDetails(
			Context context) {
		Map<String, ClassDetails> classDetailsMap = new HashMap<String, ClassDetails>();
		List<String> eoClassNames;
		try {
			eoClassNames = getEntityObjectsNamesFromManifest(context);
			for (String className : eoClassNames) {

				ClassDetails classDetails = null;
				try {
					Class<?> classToInvestigate = Class.forName(className);
					classDetails = getEntityObjectDetails(classToInvestigate);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {          
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				classDetailsMap.put(className, classDetails);
			}

			// Fill in details about inheritance hierarchies
			for (String cName : eoClassNames) {
				Class<?> subClass = Class.forName(classDetailsMap.get(cName)
						.getClassName());
				ClassDetails superClassDetails = classDetailsMap.get(subClass
						.getSuperclass().getName());
				if (superClassDetails != null) {
					superClassDetails.getSubClassDetails().add(
							classDetailsMap.get(subClass.getName()));
					Log.v(this.getClass().getName(), subClass.getName() + " extends "
							+ superClassDetails.getClassName());
				}
			}

			// Fill in Details about Relationships.
			for (String cName : eoClassNames) {
				ClassDetails classDetails = classDetailsMap.get(cName);
				for(FieldTypeDetails fieldTypeDetails : classDetails.getFieldTypeDetails())
				{
					if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY) != null)
					{
						ParameterizedType genericType = null;
						try {
							genericType = (ParameterizedType)Class.forName(classDetails.getClassName())
									.getDeclaredField(fieldTypeDetails.getFieldName())
									.getGenericType();
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						}
						catch(ClassCastException ex)
						{
							Log.e(ANNOTATION_TAG, "OneToMany should be of collection Type");
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
						Class<?> relatedClass = (Class<?>)genericType.getActualTypeArguments()[0];
						ClassDetails relatedClassDetails = classDetailsMap.get(relatedClass.getName());
						boolean bidirectional = false;

						// Then check if related class contains a reference back to this class.
						if(fieldTypeDetails.getAnnotationOptionValues().get(Constants.ONE_TO_MANY).get(Constants.MAPPED_BY) != "")
						{
							for(FieldTypeDetails relatedFieldTypeDetails : relatedClassDetails.getFieldTypeDetails())
							{
								if(relatedFieldTypeDetails.getFieldName()
										.equals(fieldTypeDetails.getAnnotationOptionValues()
												.get(Constants.ONE_TO_MANY).get(Constants.MAPPED_BY))
												&& relatedFieldTypeDetails.getAnnotationOptionValues().get(Constants.MANY_TO_ONE) != null)
								{
									bidirectional = true;
									break;
								}
							}
						}
						// Don't add if bidirectional(ManyToOne) as foreign key constraint will be created as a part of regular table creation
						if(bidirectional == false)
							relatedClassDetails.getOwnedRelations().get(RelationshipType.MANY_TO_ONE).add(classDetails);

					}
				}
			}
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classDetailsMap;
	}

	private ClassDetails getEntityObjectDetails(Class<?> classToInvestigate)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		List<FieldTypeDetails> fieldTypeDetailList = new ArrayList<FieldTypeDetails>();
		Map<String, Map<String, Object>> classAnnotationOptionValues = new HashMap<String, Map<String, Object>>();
		Annotation[] classAnnotations = classToInvestigate.getAnnotations();

		for (Annotation annotation : classAnnotations) {
			Map<String, Object> classOptionValues = new HashMap<String, Object>();
			String classAnnotationName = annotation.annotationType().getSimpleName();
			Log.v(ANNOTATION_TAG, "ClassAnnotationName: " + classAnnotationName);

			// All the Key/Value Props
			for (Method method : annotation.annotationType().getDeclaredMethods()) {
				String propKey = method.getName();
				Object propVal = method.invoke(annotation, null);
				Log.v(ANNOTATION_TAG, "Class Annotations Props: " + propKey + ": "
						+ propVal);
				classOptionValues.put(propKey, propVal);
			}
			classAnnotationOptionValues.put(classAnnotationName, classOptionValues);
		}

		// Fields
		Field[] fields = classToInvestigate.getDeclaredFields();

		for (Field field : fields) {
			FieldTypeDetails fieldTypeDetail = new FieldTypeDetails();
			Log.v(ANNOTATION_TAG, "FieldName: " + field.getName());
			Annotation[] fieldAnnotations = field.getAnnotations();

			Map<String, Map<String, Object>> fieldAnnotationOptionValues = new HashMap<String, Map<String, Object>>();

			for (Annotation annotation : fieldAnnotations) {
				Map<String, Object> fieldOptionValues = new HashMap<String, Object>();
				String fieldAnnotationName = annotation.annotationType()
						.getSimpleName();
				Log.v(ANNOTATION_TAG, "fieldAnnotationName: " + fieldAnnotationName);

				// All the Key/Value Props
				for (Method method : annotation.annotationType().getDeclaredMethods()) {
					String propKey = method.getName();
					Object propVal = method.invoke(annotation, null);
					Log.v(ANNOTATION_TAG, "Field Annotations Props: " + propKey + ": "
							+ propVal);
					fieldOptionValues.put(propKey, propVal);
				}
				fieldAnnotationOptionValues.put(fieldAnnotationName, fieldOptionValues);
			}

			fieldTypeDetail.setFieldName(field.getName());
			fieldTypeDetail.setFieldType(field.getType());
			fieldTypeDetail.setFieldGenericType(field.getGenericType());
			fieldTypeDetail.setAnnotationOptionValues(fieldAnnotationOptionValues);
			fieldTypeDetailList.add(fieldTypeDetail);
		}
		return new ClassDetails(classToInvestigate.getName(),
				classAnnotationOptionValues, fieldTypeDetailList);
	}
}
