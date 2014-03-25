package iiitb.dm.ormlibrary.ddl;

import iiitb.dm.ormlibrary.utils.Constants;
import iiitb.dm.ormlibrary.utils.RelationshipType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ClassDetails captured from Reflection
 * 
 * @author arjun
 * 
 */
public class ClassDetails {

  /**
   * Name of the Class
   */
  private String className;
  /**
   * Map of all the Annotation Names associated with Class (Key) and Map of all
   * the associated key/value pair (Value)
   */
  private Map<String, Map<String, Object>> annotationOptionValues;
  /**
   * Field Details List
   */
  private List<FieldTypeDetails> fieldTypeDetails;
  
  private String columnsDescription;

  /**
   * List of all the Sub Classes
   */
  private List<ClassDetails> subClassDetails;
  
  /**
   * Map from relationship type to list of classes to which this 
   * class is related, is owner and doesn't contain reference. 
   */
  private Map<RelationshipType, List<ClassDetails>> ownedRelations;

  public Map<RelationshipType, List<ClassDetails>> getOwnedRelations() {
	return ownedRelations;
}

public void setOwnedRelations(Map<RelationshipType, List<ClassDetails>> ownedRelations) {
	this.ownedRelations = ownedRelations;
}

public ClassDetails(String className,
      Map<String, Map<String, Object>> annotationOptionValues,
      List<FieldTypeDetails> fieldTypeDetails) {
    super();
    this.className = className;
    this.annotationOptionValues = annotationOptionValues;
    this.fieldTypeDetails = fieldTypeDetails;
    subClassDetails = new LinkedList<ClassDetails>();
    ownedRelations = new HashMap<RelationshipType,List<ClassDetails>>();
    ownedRelations.put(RelationshipType.ONE_TO_MANY, new ArrayList<ClassDetails>());
    ownedRelations.put(RelationshipType.MANY_TO_ONE, new ArrayList<ClassDetails>());
    ownedRelations.put(RelationshipType.MANY_TO_MANY, new ArrayList<ClassDetails>());
    ownedRelations.put(RelationshipType.ONE_TO_ONE, new ArrayList<ClassDetails>());
  }
  
  public String getColumnsDescription() {
		return columnsDescription;
	}

	public void setColumnsDescription(String columnsDescription) {
		this.columnsDescription = columnsDescription;
	}

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Map<String, Map<String, Object>> getAnnotationOptionValues() {
    return annotationOptionValues;
  }

  public void setAnnotationOptionValues(
      Map<String, Map<String, Object>> annotationOptionValues) {
    this.annotationOptionValues = annotationOptionValues;
  }

  public List<FieldTypeDetails> getFieldTypeDetails() {
    return fieldTypeDetails;
  }

  public void setFieldTypeDetails(List<FieldTypeDetails> fieldTypeDetails) {
    this.fieldTypeDetails = fieldTypeDetails;
  }

  public List<ClassDetails> getSubClassDetails() {
    return subClassDetails;
  }

  public void setSubClassDetails(List<ClassDetails> subClassDetails) {
    this.subClassDetails = subClassDetails;
  }

	public FieldTypeDetails getFieldTypeDetailsByColumnName(String columnName)
	{
		for (FieldTypeDetails fieldTypeDetails : getFieldTypeDetails())
		{
			Map<String, Object> fieldAnnotationOptionValues = fieldTypeDetails
					.getAnnotationOptionValues().get(Constants.COLUMN);
			if (fieldAnnotationOptionValues == null)
				continue;
			if (columnName == (String) fieldAnnotationOptionValues
					.get(Constants.NAME))
				return fieldTypeDetails;
		}
		return null;
	}

	public FieldTypeDetails getFieldTypeDetailsOfId()
	{
		// TODO: Make fieldTypeDetails as a LinkedHashMap and avoid the for loop
		for (FieldTypeDetails id : getFieldTypeDetails())
			// TODO: need to get this to constants.java
			if (id.getAnnotationOptionValues().get(Constants.ID) != null)
				return id;
		return null;
	}
	
	/**
	 * Return a FieldTypeDetails object of a field which is annotated with an 
	 * inverse many-to-many mapping to its owning side entity class through 
	 * the given field name
	 * 
	 * @param inverseMappedFieldName
	 * @return
	 */
	public FieldTypeDetails getFieldTypeDetailsByMappedByAnnotation(
			String inverseMappedFieldName)
	{
		for (FieldTypeDetails fieldTypeDetails : getFieldTypeDetails())
		{
			if (fieldTypeDetails.getAnnotationOptionValues().get(
					Constants.MANY_TO_MANY) == null)
				continue;
			if (fieldTypeDetails.getAnnotationOptionValues()
					.get(Constants.MANY_TO_MANY)
					.get(Constants.MAPPED_BY).equals(inverseMappedFieldName))
				return fieldTypeDetails;
		}
		return null;
	}	

}
