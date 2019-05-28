package com.example.transmission;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 2/28/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DB_Name= "neighbor.db";
    public static final String table_name= "routing_table";
    public static final String col1= "mac_address";
    public static final String col2= "next_hop";
    public static final String col3= "hop_count";
    public static final String col4= "status";


    public DatabaseHelper(Context context) {
        super(context, DB_Name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ table_name+ "(mac_address text primary key, next_hop text, hop_count integer, status boolean)");
        //String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +COL2 +" TEXT)";
        //db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ table_name);
        onCreate(db);
    }

    public boolean addData(String mac_address, String next_hop, int hop_count, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col1, mac_address);
        contentValues.put(col2, next_hop);
        contentValues.put(col3, hop_count);
        contentValues.put(col4, status);

        //Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);

        long result= db.insert(table_name, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }


    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + table_name;
        Cursor data = db.rawQuery(query, null);
        return data;
    }


   /* public Cursor getItemID(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }*/


   /* public void updateName(String newName, int id, String oldName){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + newName + "' WHERE " + COL1 + " = '" + id + "'" +
                " AND " + COL2 + " = '" + oldName + "'";
        Log.d(TAG, "updateName: query: " + query);
        Log.d(TAG, "updateName: Setting name to " + newName);
        db.execSQL(query);
    }*/

  /*  *//*
     * Delete from database
     * @param id
     * @param name
     */
    public void deleteName(String fieldValue){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM routing_table where mac_address='"+ fieldValue+";";
        //Log.d(TAG, "deleteName: query: " + query);
        //Log.d(TAG, "deleteName: Deleting " + name + " from database.");
        db.execSQL(query);
    }

    public Cursor dataExists(String fieldValue)
    {

        SQLiteDatabase db= this.getWritableDatabase();
        String Query= "Select * from routing_table where mac_address ='" + fieldValue+ "'";
        Cursor res = db.rawQuery(Query, null);
        return res;
    }

    public  boolean CheckData(String fieldValue)
    {
        Cursor res= dataExists(fieldValue);
        if(res.getCount()==0)
        {
            return false;
        }
        else
            return true;
        //return false;
    }

}
