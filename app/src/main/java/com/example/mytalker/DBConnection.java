package com.example.mytalker;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBConnection extends SQLiteOpenHelper {

    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Database.db";

    public interface VocSchema {
        String TABLE_NAME = "Voc";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";           //COUNT
    }

    public interface RelationSchema {
        String TABLE_NAME = "Relation";          //Table Name
        String ID = "_id";                    //ID
        String ID1 = "id1";       //ID1
        String ID2 = "id2";           //ID2
        String COUNT = "count";       //COUNT
    }
    public interface SentenceSchema {
        String TABLE_NAME = "Sentence";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";       //COUNT
    }
    public DBConnection(Context ctx) {
        super(ctx, _DBName, null, _DBVersion);
    }
    public void onCreate(SQLiteDatabase db) {

        try {
            String sql = "CREATE TABLE " + VocSchema.TABLE_NAME + " ("
                    + VocSchema.ID  + " INTEGER unique primary key autoincrement not null, "
                    + VocSchema.CONTENT + " text unique not null, "
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
                    + SentenceSchema.ID  + " INTEGER unique primary key autoincrement, "
                    + SentenceSchema.CONTENT + " text unique not null, "
                    + SentenceSchema.COUNT + " INTEGER not null default 1" + ");";
            db.execSQL(sql3);
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int insert(boolean mode,String content, SQLiteDatabase db){

        String content_name=(mode?VocSchema.CONTENT:SentenceSchema.CONTENT);
        String table_name=(mode?VocSchema.TABLE_NAME:SentenceSchema.TABLE_NAME);

        int newid=0;
        try{
            ContentValues values=new ContentValues();
            values.put(content_name,content);
            try{
                newid=(int)db.insert(table_name,null,values);
            }catch (Exception e){
                e.printStackTrace();
                if(!mode)
                System.out.println("Insert ERROR<<<============================================");
            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return newid;
    }

    public int insert(int id1,int id2, SQLiteDatabase db){
        int newid=0;
        try{
            ContentValues values=new ContentValues();
            values.put(RelationSchema.ID1,id1);
            values.put(RelationSchema.ID2,id2);
            try{
                newid=(int)db.insert(RelationSchema.TABLE_NAME,null,values);
            }catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return newid;
    }

    public int getVocID(String content,SQLiteDatabase db){
        int id=0;
        try{
            String query="select "+VocSchema.ID+" from "+VocSchema.TABLE_NAME+
                    " where "+VocSchema.CONTENT+" = '"+content+"';";
            Cursor c=db.rawQuery(query,null);
            if(c.getCount()>0){
                c.moveToFirst();
                id=c.getInt(c.getColumnIndex(VocSchema.ID));
            }
            c.close();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return id;
    }

    public boolean update(boolean mode,String content, SQLiteDatabase db){

        String content_name=(mode?VocSchema.CONTENT:SentenceSchema.CONTENT);
        String count_name=(mode?VocSchema.COUNT:SentenceSchema.COUNT);
        String table_name=(mode?VocSchema.TABLE_NAME:SentenceSchema.TABLE_NAME);
        String id_name=(mode?VocSchema.ID:SentenceSchema.ID);

        boolean ret=false;
        try{
            String query="select * from "+table_name+" where "
                    +content_name+" = '"+content+"';";
            Cursor c=db.rawQuery(query,null);
            if(c.getCount()>0){
                c.moveToFirst();
                int id=c.getInt(c.getColumnIndex(id_name));
                int count=c.getInt(c.getColumnIndex(count_name))+1;
                ContentValues values=new ContentValues();
                values.put(count_name,count);
                String where=id_name+ " = "+id;
                try {
                    db.update(table_name,values,where,null);
                    ret=true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            c.close();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return ret;
    }

    public boolean update(int id1,int id2, SQLiteDatabase db){
        boolean ret=false;
        try{
            String query="select * from "+RelationSchema.TABLE_NAME+" where "
                    +RelationSchema.ID1+" = '"+id1+"' and "+RelationSchema.ID2+" = '"+id2+"';";
            Cursor c=db.rawQuery(query,null);
            if(c.getCount()>0){
                c.moveToFirst();
                int id=c.getInt(c.getColumnIndex(RelationSchema.ID));
                int count=c.getInt(c.getColumnIndex(RelationSchema.COUNT))+1;
                ContentValues values=new ContentValues();
                values.put(RelationSchema.COUNT,count);
                String where=RelationSchema.ID+ " = "+id;
                try {
                    db.update(RelationSchema.TABLE_NAME,values,where,null);
                    ret=true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                c.close();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.close();
        }

        return ret;
    }
}