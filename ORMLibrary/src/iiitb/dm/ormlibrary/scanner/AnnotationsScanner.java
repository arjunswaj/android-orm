package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.ddl.ClassDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.content.Context;

public interface AnnotationsScanner {

  public Collection<ClassDetails> getEntityObjectDetails(Context context);

}
