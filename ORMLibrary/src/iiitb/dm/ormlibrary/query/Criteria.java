package iiitb.dm.ormlibrary.query;

import iiitb.dm.ormlibrary.query.criterion.Order;
import iiitb.dm.ormlibrary.query.criterion.ProjectionList;

import java.util.List;

import android.database.Cursor;
public interface Criteria {

  Criteria add(Criterion criterion);

  Criteria addOrder(Order order);

  List list();

  Cursor cursor();
  
  Criteria setProjection(ProjectionList projectionList);
  
}
