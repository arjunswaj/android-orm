package iiitb.dm.ormlibrary.scanner.impl;

import iiitb.dm.ormlibrary.ddl.ClassDetails;
import iiitb.dm.ormlibrary.ddl.FieldTypeDetails;
import iiitb.dm.ormlibrary.scanner.AnnotationsScanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        Log.i(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.START_TAG) {
        Log.i(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.END_TAG) {
        Log.i(XML_TAG, "We don't need this for now.");
      } else if (eventType == XmlPullParser.TEXT) {
        eoNames.add(xpp.getText());
        Log.i(XML_TAG, "ClassName: " + xpp.getText());
      }
      eventType = xpp.next();
    }
    return eoNames;
  }

  @Override
  public List<ClassDetails> getEntityObjectDetails(Context context) {
    List<ClassDetails> classDetailsList = new ArrayList<ClassDetails>();
    List<String> eoClassNames;
    try {
      eoClassNames = getEntityObjectsNamesFromManifest(context);
      for (String className : eoClassNames) {
        List<FieldTypeDetails> fieldTypeDetailList = new ArrayList<FieldTypeDetails>();
        Map<String, Map<String, String>> classAnnotationOptionValues = new HashMap<String, Map<String, String>>();
        try {
          Class<?> classToInvestigate = Class.forName(className);
          Annotation[] classAnnotations = classToInvestigate.getAnnotations();

          for (Annotation annotation : classAnnotations) {
            Map<String, String> classOptionValues = new HashMap<String, String>();
            String classAnnotationName = annotation.annotationType()
                .getSimpleName();
            Log.i(ANNOTATION_TAG, "ClassAnnotationName: " + classAnnotationName);

            // All the Key/Value Props
            for (Method method : annotation.annotationType()
                .getDeclaredMethods()) {
              String propKey = method.getName();
              String propVal = (String) method.invoke(annotation, null);
              Log.i(ANNOTATION_TAG, "Class Annotations Props: " + propKey
                  + ": " + propVal);
              classOptionValues.put(propKey, propVal);
            }
            classAnnotationOptionValues.put(classAnnotationName,
                classOptionValues);
          }

          // Fields
          Field[] fields = classToInvestigate.getDeclaredFields();

          for (Field field : fields) {
            FieldTypeDetails fieldTypeDetail = new FieldTypeDetails();
            Log.i(ANNOTATION_TAG, "FieldName: " + field.getName());
            Annotation[] fieldAnnotations = field.getAnnotations();

            Map<String, Map<String, String>> fieldAnnotationOptionValues = new HashMap<String, Map<String, String>>();

            for (Annotation annotation : fieldAnnotations) {
              Map<String, String> fieldOptionValues = new HashMap<String, String>();
              String fieldAnnotationName = annotation.annotationType()
                  .getSimpleName();
              Log.i(ANNOTATION_TAG, "fieldAnnotationName: "
                  + fieldAnnotationName);

              // All the Key/Value Props
              for (Method method : annotation.annotationType()
                  .getDeclaredMethods()) {
                String propKey = method.getName();
                String propVal = (String) method.invoke(annotation, null);
                Log.i(ANNOTATION_TAG, "Field Annotations Props: " + propKey
                    + ": " + propVal);
                fieldOptionValues.put(propKey, propVal);
              }
              fieldAnnotationOptionValues.put(fieldAnnotationName,
                  fieldOptionValues);
            }

            fieldTypeDetail.setFieldName(field.getName());
            fieldTypeDetail.setFieldType(field.getType());
            fieldTypeDetail
                .setAnnotationOptionValues(fieldAnnotationOptionValues);
            fieldTypeDetailList.add(fieldTypeDetail);

          }

        } catch (ClassNotFoundException e) {
          // Class not found!
        } catch (Exception e) {
          // Unknown exception
        }
        ClassDetails classDetails = new ClassDetails(className,
            classAnnotationOptionValues, fieldTypeDetailList);
        classDetailsList.add(classDetails);
      }
    } catch (XmlPullParserException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return classDetailsList;
  }
}
