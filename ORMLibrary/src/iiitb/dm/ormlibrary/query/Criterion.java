package iiitb.dm.ormlibrary.query;

public interface Criterion {
  /*
   * Is this projection fragment (SELECT clause) also part of the GROUP BY
   */
  public boolean isGrouped();
  
   
}
