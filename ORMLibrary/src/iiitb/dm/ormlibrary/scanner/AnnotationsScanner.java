package iiitb.dm.ormlibrary.scanner;

import iiitb.dm.ormlibrary.dml.ClassDetails;

import java.util.List;

import android.content.Context;

public interface AnnotationsScanner {

  public List<ClassDetails> getEntityObjectDetails(Context context);

}
