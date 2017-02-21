package com.mytalker.core;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public final class TalkerDBManager {
    public final static String _DBName = "MyTalkers";
    public final static String _DBExt = ".mtks.db"; //use for file manager to filter files
    private final static int _DBVersion = 1;
    private DBConnection dbConnection;
    private final static Object lock = new Object();
    private String replace = "axDCcdXA";

    public TalkerDBManager(Context context){
        dbConnection = new DBConnection(context, _DBName + _DBExt, _DBVersion);
    }
    // Table and its attribute name
    private interface VocSchema {
        String TABLE_NAME = "Voc";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";           //COUNT
    }
    private interface RelationSchema {
        String TABLE_NAME = "Relation";          //Table Name
        String ID = "_id";                    //ID
        String ID1 = "id1";       //ID1
        String ID2 = "id2";           //ID2
        String COUNT = "count";       //COUNT
    }
    private interface SentenceSchema {
        String TABLE_NAME = "Sentence";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";       //COUNT
    }

    int insertVoc(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getWritableDatabase();
        String content_name = VocSchema.CONTENT;
        String table_name = VocSchema.TABLE_NAME;

        int newid = -1;
        ContentValues values = new ContentValues();
        values.put(content_name, content);
        try{
            newid = (int) db.insert(table_name, null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return newid;
    }

    int insertRelation(String Voc1, String Voc2) {
        int id1 = getVocID(Voc1);
        int id2 = getVocID(Voc2);
        SQLiteDatabase db = dbConnection.getWritableDatabase();
        String table_name = RelationSchema.TABLE_NAME;

        int newid = -1;
        ContentValues values = new ContentValues();
        values.put(RelationSchema.ID1, id1);
        values.put(RelationSchema.ID2, id2);
        try{
            newid = (int) db.insert(table_name, null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return newid;
    }

    int insertSentence(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getWritableDatabase();
        String content_name = SentenceSchema.CONTENT;
        String table_name = SentenceSchema.TABLE_NAME;

        int newid = -1;
        ContentValues values = new ContentValues();
        values.put(content_name, content);
        try{
            newid = (int) db.insert(table_name, null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return newid;
    }

    boolean updateVoc(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getWritableDatabase();
        String content_name = VocSchema.CONTENT;
        String table_name = VocSchema.TABLE_NAME;
        String count_name = VocSchema.COUNT;
        String id_name = VocSchema.ID;

        boolean ret = false;
        String query = "select * from " + table_name + " where "
                        + content_name + " = '" + content + "';";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount() > 0){
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex(id_name));
            int count = c.getInt(c.getColumnIndex(count_name)) + 1;
            ContentValues values = new ContentValues();
            values.put(count_name, count);
            String where=id_name + " = " + id;
            try {
                db.update(table_name, values, where, null);
                ret = true;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        c.close();
        db.close();

        return ret;
    }

    boolean updateRelation(String Voc1, String Voc2) {
        int id1 = getVocID(Voc1);
        int id2 = getVocID(Voc2);
        SQLiteDatabase db = dbConnection.getWritableDatabase();

        boolean ret = false;
        String query = "select * from " +  RelationSchema.TABLE_NAME + " where "
                + RelationSchema.ID1 + " = " + id1 + " and " + RelationSchema.ID2 + " = " + id2 + ";";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex(RelationSchema.ID));
            int count = c.getInt(c.getColumnIndex(RelationSchema.COUNT)) + 1;
            ContentValues values = new ContentValues();
            values.put(RelationSchema.COUNT, count);
            String where = RelationSchema.ID + " = " + id;
            try {
                db.update(RelationSchema.TABLE_NAME, values, where, null);
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        db.close();

        return ret;
    }

    boolean updateSentence(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getWritableDatabase();
        String content_name = SentenceSchema.CONTENT;
        String table_name = SentenceSchema.TABLE_NAME;
        String count_name = SentenceSchema.COUNT;
        String id_name = SentenceSchema.ID;

        boolean ret = false;
        String query = "select * from " + table_name + " where "
                + content_name + " = '" + content + "';";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount() > 0){
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex(id_name));
            int count = c.getInt(c.getColumnIndex(count_name)) + 1;
            ContentValues values = new ContentValues();
            values.put(count_name, count);
            String where=id_name + " = " + id;
            try {
                db.update(table_name, values, where, null);
                ret = true;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        c.close();
        db.close();

        return ret;
    }

    boolean isExistVoc(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + VocSchema.TABLE_NAME +
                        " WHERE " + VocSchema.CONTENT + " = '" + content + "';", null);
        boolean exist = c.getCount() > 0;
        c.close();
        db.close();
        return exist;
    }

    private int getVocID(String content){
        content = content.replaceAll("'",replace);
        SQLiteDatabase db = dbConnection.getReadableDatabase();
        int id = -1;
        try{
            String query = "select " + VocSchema.ID + " from " + VocSchema.TABLE_NAME +
                            " where " + VocSchema.CONTENT + " = '" + content + "';";
            Cursor c = db.rawQuery(query, null);
            if(c.getCount() > 0){
                c.moveToFirst();
                id = c.getInt(c.getColumnIndex(VocSchema.ID));
            }
            c.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        db.close();

        return id;
    }

    public Cursor getAllVoc(){
        SQLiteDatabase db = dbConnection.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + VocSchema.TABLE_NAME + " ORDER BY " + VocSchema.COUNT + " DESC;", null);
    }

    public Cursor getRelations(int id){
        SQLiteDatabase db = dbConnection.getReadableDatabase();
        String query = "select " + RelationSchema.ID2 + " from " + RelationSchema.TABLE_NAME +
                " where " + RelationSchema.ID1 + " = '" + String.valueOf(id) + "' order by " + RelationSchema.COUNT + " desc;";
        return db.rawQuery(query, null);
    }

    private void loadRelations(int[] nextIDs, Cursor c){
        synchronized (lock){
            int size = c.getCount();
            if (size > 0){
                c.moveToFirst();
                for(int i = 0; i < size; i++){
                    nextIDs[i] = c.getInt(0);
                    c.moveToNext();
                }
            }
            c.close();
        }
    }

    public void loadAllVoc(int[] map, InputData[] data, int[] nextIDs, Cursor c){
        int size = c.getCount();
        if(size > 0){
            c.moveToFirst();
            for (int i = 0; i < size; i++){
                int id = nextIDs[i] = c.getInt(c.getColumnIndex(VocSchema.ID));
                map[id] = i;
                data[i] = new InputData(c.getString(c.getColumnIndex(VocSchema.CONTENT)), id);
                c.moveToNext();
            }
        }
        c.close();
    }

    public void findSentences(String keyword, ArrayList<String> result){
        keyword = keyword.replaceAll("'", replace);
        SQLiteDatabase db = dbConnection.getReadableDatabase();
        result.clear();
        result.add("相關句");
        String query;
        if(keyword.equals("")){
            query = "select content from " + SentenceSchema.TABLE_NAME +
                    "  ORDER BY " + SentenceSchema.COUNT + " desc;";
        } else {
            query = "select content from " + SentenceSchema.TABLE_NAME +
                    " where content LIKE '%" + keyword + "%'  ORDER BY " +
                    SentenceSchema.COUNT + " desc;";
        }
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        final int SIZE = c.getCount();
        final int resultLength = 15;
        for (int i = 0; i < resultLength && i < SIZE; i++) {
            String sentence = c.getString(0).replaceAll(replace, "\'");
            result.add(sentence);
            c.moveToNext();
        }

        c.close();
        db.close();
    }

    public class LoadRelations implements Runnable{

        private int[] nextIDs;
        private Cursor cursor;

        public LoadRelations(int[] nextIDs, Cursor cursor){
            this.nextIDs = nextIDs;
            this.cursor = cursor;
        }

        @Override
        public void run() {
            loadRelations(nextIDs, cursor);
        }
    }

/*
*  DB Create
*/

    private class DBConnection extends SQLiteOpenHelper {

        private DBConnection(Context context, String DBName, int DBVersion) {
            super(context, DBName, null, DBVersion);
        }
        public void onCreate(SQLiteDatabase db) {

            try {
                String sql = "CREATE TABLE " + VocSchema.TABLE_NAME + " ("
                        + VocSchema.ID  + " INTEGER unique primary key autoincrement not null, "
                        + VocSchema.CONTENT + " ntext unique not null, "
                        + VocSchema.COUNT + " INTEGER not null default 1" + ");";
                db.execSQL(sql);

                String sql2 = "CREATE TABLE " + RelationSchema.TABLE_NAME + " ("
                        + RelationSchema.ID  + " INTEGER unique primary key autoincrement, "
                        + RelationSchema.ID1 + " INTEGER not null, "
                        + RelationSchema.ID2 + " INTEGER not null, "
                        + RelationSchema.COUNT + " INTEGER not null default 1, "
                        + "foreign key(" + RelationSchema.ID1 + ") references " + VocSchema.TABLE_NAME + "(" + VocSchema.ID + "), "
                        + "foreign key(" + RelationSchema.ID2 + ") references " + VocSchema.TABLE_NAME + "(" + VocSchema.ID + "), "
                        + "constraint candidate_key unique (" + RelationSchema.ID1 + "," + RelationSchema.ID2 + ") " + ");";
                db.execSQL(sql2);

                String sql3 = "CREATE TABLE " + SentenceSchema.TABLE_NAME + " ("
                        + SentenceSchema.ID  + " INTEGER unique primary key autoincrement not null, "
                        + SentenceSchema.CONTENT + " ntext unique not null, "
                        + SentenceSchema.COUNT + " INTEGER not null default 1" + ");";
                db.execSQL(sql3);
            }catch (Exception e){
                System.out.println(e.toString());
            }
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
