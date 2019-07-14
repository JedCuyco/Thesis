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

    public static DatabaseHelper db_instance;

    private static final String TAG = "DatabaseHelper";

    public static final String DB_Name= "neighbor.db";
    public static final String table_name= "routing_table";
    public static final String col1= "mac_address";
    public static final String col2= "next_hop";
    public static final String col3= "hop_count";
    public static final String col4= "status";
    public static final String col5="device_name";
    public static final String col6="battery_percentage";
    public static final String col7="signal";

    public static final String table_name2="message_history";
    public static final String cl1="message_id";
    public static final String cl2="mac_addressf";
    public static final String cl3="message";


    public DatabaseHelper(Context context) {
        super(context, DB_Name, null, 1);
    }

    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if(db_instance==null)
        {
            db_instance= new DatabaseHelper(context.getApplicationContext());
        }

        return db_instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ table_name+ "(mac_address text primary key, next_hop text, hop_count integer, status boolean, device_name text, battery_percentage int, signal int)");
        /*db.execSQL("CREATE TABLE "+ table_name2+ "(message_id int primary key autoincrement, mac_addressf text, message text, foreign key (mac_addressf) references "+table_name+"("+col1+"))");*/
        //String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +COL2 +" TEXT)";
        //db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ table_name);
        /*db.execSQL("DROP TABLE IF EXISTS "+ table_name2);*/
        onCreate(db);
    }

    public boolean addData(String mac_address, String next_hop, int hop_count, boolean status, String device_name, int battery_percentage, int signal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col1, mac_address);
        contentValues.put(col2, next_hop);
        contentValues.put(col3, hop_count);
        contentValues.put(col4, status);
        contentValues.put(col5, device_name);
        contentValues.put(col6, battery_percentage);
        contentValues.put(col7, signal);

        //Log.d(TAG, "addData: Adding " + item + " to " + TABLE_NAME);
        long result= db.insert(table_name, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getGateways()
    {
        int fieldValue=1;
        SQLiteDatabase db = this.getWritableDatabase();
        String Query= "Select * from routing_table where status ='" + fieldValue+ "'";
        Cursor res=  db.rawQuery(Query, null);
        res.close();
        return res;
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
        String query = "DELETE FROM routing_table where mac_address='"+ fieldValue+"'";
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

    public Cursor getDirectlyConnected()
    {
        int fieldValue=1;
        SQLiteDatabase db= this.getWritableDatabase();
        String Query= "Select * from routing_table where hop_count ='" + fieldValue+ "'";
        Cursor res = db.rawQuery(Query, null);
        return res;
    }

    public Cursor getCandidateGateways()
    {
        int status=1;
        SQLiteDatabase db= this.getWritableDatabase();
        String Query= "Select * from routing_table where status ='" + status+ "'";
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

    public int getHopCount(String fieldValue)
    {
        Cursor res= dataExists(fieldValue);
        res.moveToNext();

        return res.getInt(2);
    }

    public String getNextHop(String fieldValue)
    {
        Cursor res= dataExists(fieldValue);
        res.moveToNext();

        return res.getString(1);
    }

    public void updateBattery( String fieldValue, int battery)
    {
        String TABLE_NAME= "routing_table";
        String ColumnName="battery_percentage";
        String Column="mac_address";
        SQLiteDatabase db=this.getWritableDatabase();
        String query="UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+battery+"' WHERE "+Column+ " = '"+fieldValue+"'";
        db.execSQL(query);
    }

    public void updateSignal( String fieldValue, int status)
    {
        String TABLE_NAME= "routing_table";
        String ColumnName="signal";
        String Column="mac_address";
        SQLiteDatabase db=this.getWritableDatabase();
        String query="UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+status+"' WHERE "+Column+ " = '"+fieldValue+"'";
        db.execSQL(query);
    }

    public void updateStatus( String fieldValue, int signal)
    {
        String TABLE_NAME= "routing_table";
        String ColumnName="status";
        String Column="mac_address";
        SQLiteDatabase db=this.getWritableDatabase();
        String query="UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+signal+"' WHERE "+Column+ " = '"+fieldValue+"'";
        db.execSQL(query);
    }

    public void updateHopCount(String fieldValue, int hopcount)
    {
        String TABLE_NAME= "routing_table";
        String ColumnName= "hop_count";
        String Column="mac_address";
        SQLiteDatabase db= this.getWritableDatabase();
        String sql = "UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+hopcount+"' WHERE "+Column+ " = '"+fieldValue+"'";
        db.execSQL(sql);
    }

    public void updateNextHop(String fieldValue, String newNextHop)
    {
        String TABLE_NAME= "routing_table";
        String ColumnName= "next_hop";
        String Column="mac_address";
        SQLiteDatabase db= this.getWritableDatabase();
        /*ContentValues cv= new ContentValues();
        cv.put("next_hop", newNextHop);*/
        String sql = "UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+newNextHop+"' WHERE "+Column+ " = '"+fieldValue+"'";
        //String query= "update routing_table set next_hop='"+newNextHop+"' where mac_adress='"+fieldValue+"'";
        db.execSQL(sql);
        //db.update("routing_table", cv, "mac_address= ?", new String[]{fieldValue});
    }



}
