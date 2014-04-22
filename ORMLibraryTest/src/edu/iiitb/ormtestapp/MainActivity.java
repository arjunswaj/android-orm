package edu.iiitb.ormtestapp;

import iiitb.dm.ormlibrary.ORMHelper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

  ListView listView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    List<String> menuList = new ArrayList<String>();
    menuList.add("Create");
    menuList.add("Query");
    menuList.add("Update");
    menuList.add("Delete");

    listView = (ListView) findViewById(R.id.mainList);
    listView.setAdapter(new ArrayAdapter(this,
        android.R.layout.simple_list_item_1, menuList));

    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position,
          long id) {
        switch (position) {
        case 0:
          Intent createScreen = new Intent(MainActivity.this,
              CreateActivity.class);
          startActivity(createScreen);
          break;
        case 1:
          Intent queryScreen = new Intent(MainActivity.this,
              QueryActivity.class);
          startActivity(queryScreen);
          break;
        case 2:
          Intent updateScreen = new Intent(MainActivity.this,
              UpdateActivity.class);
          startActivity(updateScreen);
          break;
        case 3:
            Intent deleteScreen = new Intent(MainActivity.this,
                DeleteActivity.class);
            startActivity(deleteScreen);
            break;

        }
      }
    });

  }
}
