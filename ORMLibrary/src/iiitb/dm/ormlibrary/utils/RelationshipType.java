/**
 * 
 */
package iiitb.dm.ormlibrary.utils;

/**
 * @author kempa
 *
 */
public enum RelationshipType {
	ONE_TO_ONE
	{
		public String toString() 
		{
			return Constants.ONE_TO_ONE;
		}
	},
	
	ONE_TO_MANY
	{
		public String toString() 
		{
			return Constants.ONE_TO_MANY;
		}
	},
	
	MANY_TO_ONE
	{
		public String toString()
		{
			return Constants.MANY_TO_ONE;
		}
	},
	
	MANY_TO_MANY
	{
		public String toString()
		{
			return Constants.MANY_TO_MANY;
		}
	}
}
