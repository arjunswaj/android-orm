package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.ddl.ClassDetails;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import android.content.Context;

public interface AnnotationsScanner {

  public ClassDetails getEntityObjectDetails(Class<?> classToInvestigate)
      throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException;

  public Map<String, ClassDetails> getEntityObjectCollectionDetails(
      Context context);

}
