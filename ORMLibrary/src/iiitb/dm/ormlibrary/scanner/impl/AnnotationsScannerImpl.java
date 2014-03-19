package iiitb.dm.ormlibrary.scanner.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class AnnotationsScannerImpl implements AnnotationsScanner {

  private static final String XML_TAG = "XML Scanner";
  private static final String ANNOTATION_TAG = "Annotation Scanner";

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
        // Log.d(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.START_TAG) {
        // Log.d(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.END_TAG) {
        // Log.d(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.TEXT) {
        eoNames.add(xpp.getText());
        Log.d(XML_TAG, "ClassName: " + xpp.getText());
      }
      eventType = xpp.next();
    }
    return eoNames;
  }

  @Override
  public Map<String, ClassDetails> getEntityObjectCollectionDetails(
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
          Log.d(this.getClass().getName(), subClass.getName() + " extends "
              + superClassDetails.getClassName());
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

  @Override
  public ClassDetails getEntityObjectDetails(Class<?> classToInvestigate)
      throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    List<FieldTypeDetails> fieldTypeDetailList = new ArrayList<FieldTypeDetails>();
    Map<String, Map<String, Object>> classAnnotationOptionValues = new HashMap<String, Map<String, Object>>();
    Annotation[] classAnnotations = classToInvestigate.getAnnotations();

    for (Annotation annotation : classAnnotations) {
      Map<String, Object> classOptionValues = new HashMap<String, Object>();
      String classAnnotationName = annotation.annotationType().getSimpleName();
      Log.d(ANNOTATION_TAG, "ClassAnnotationName: " + classAnnotationName);

      // All the Key/Value Props
      for (Method method : annotation.annotationType().getDeclaredMethods()) {
        String propKey = method.getName();
        Object propVal = method.invoke(annotation, null);
        Log.d(ANNOTATION_TAG, "Class Annotations Props: " + propKey + ": "
            + propVal);
        classOptionValues.put(propKey, propVal);
      }
      classAnnotationOptionValues.put(classAnnotationName, classOptionValues);
    }

    // Fields
    Field[] fields = classToInvestigate.getDeclaredFields();

    for (Field field : fields) {
      FieldTypeDetails fieldTypeDetail = new FieldTypeDetails();
      Log.d(ANNOTATION_TAG, "FieldName: " + field.getName());
      Annotation[] fieldAnnotations = field.getAnnotations();

      Map<String, Map<String, String>> fieldAnnotationOptionValues = new HashMap<String, Map<String, String>>();

      for (Annotation annotation : fieldAnnotations) {
        Map<String, String> fieldOptionValues = new HashMap<String, String>();
        String fieldAnnotationName = annotation.annotationType()
            .getSimpleName();
        Log.d(ANNOTATION_TAG, "fieldAnnotationName: " + fieldAnnotationName);

        // All the Key/Value Props
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
          String propKey = method.getName();
          String propVal = (String) method.invoke(annotation, null);
          Log.d(ANNOTATION_TAG, "Field Annotations Props: " + propKey + ": "
              + propVal);
          fieldOptionValues.put(propKey, propVal);
        }
        fieldAnnotationOptionValues.put(fieldAnnotationName, fieldOptionValues);
      }

      fieldTypeDetail.setFieldName(field.getName());
      fieldTypeDetail.setFieldType(field.getType());
      fieldTypeDetail.setAnnotationOptionValues(fieldAnnotationOptionValues);
      fieldTypeDetailList.add(fieldTypeDetail);
    }
    return new ClassDetails(classToInvestigate.getName(),
        classAnnotationOptionValues, fieldTypeDetailList);
  }
}
