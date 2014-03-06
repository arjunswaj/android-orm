package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.ddl.ClassDetails;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import android.content.Context;

public interface AnnotationsScanner {

  public ClassDetails getEntityObjectDetails(Class<?> classToInvestigate)
      throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException;

  public Collection<ClassDetails> getEntityObjectCollectionDetails(
      Context context);

}
