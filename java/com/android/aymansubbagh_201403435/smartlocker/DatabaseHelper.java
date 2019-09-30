
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {

    Context context;

    //Database Name
    public static final String DATABASE_NAME = "SmartLocker.db";

    //UserTable
    public static final String SmartLocker_USERTable = "SmartLocker_UserTable";
    public static final String NAME = "NAME";
    public static final String PHONE = "PHONE";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";
    public static final String FILE_KEY = "FKEY";


    //File Table
    public static final String SmartLocker_FileTable = "SmartLocker_FileTable";
    public static final String FID = "FID";
    public static final String FNAME = "FNAME";
    public static final String FPATH = "FPATH";
    public static final String FEXTENSION = "FEXTENSION";
    public static final String DATETIME = "DATETIME";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table " + SmartLocker_USERTable + " (NAME TEXT,PHONE TEXT,EMAIL TEXT PRIMARY KEY,PASSWORD TEXT,FKEY TEXT)");
        db.execSQL("Create Table " + SmartLocker_FileTable + " (FID INTEGER  PRIMARY KEY AUTOINCREMENT,EMAIL TEXT,FNAME Text,FPATH Text,FEXTENSION Text,DATETIME Text)");
      }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP table if exists " + SmartLocker_USERTable);
        db.execSQL("DROP table if exists " + SmartLocker_FileTable);
        onCreate(db);
    }

    public boolean insertUser(String name, String phone, String email, String password, String key) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues c = new ContentValues();
        c.put(NAME, name);
        c.put(PHONE, phone);
        c.put(EMAIL, email);
        c.put(PASSWORD, password);
        c.put(FILE_KEY, key);
        long result = db.insert(SmartLocker_USERTable, null, c);

        if (result == -1)
            return false;
        else {
            return true;
        }
    }

    public boolean checkEmail(String email) {

        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor ans = db.rawQuery("select * from Employee where EMAIL = "+email,null);
        Cursor ans = db.query(SmartLocker_USERTable, new String[]{PASSWORD}, EMAIL + " = '" + email + "'", null, null, null, null);

        if (ans.getCount() == 0) {
            return true;
        } else
            return false;
    }

    public Cursor getUserList(String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor res = db.query(SmartLocker_USERTable, new String[]{FILE_KEY}, EMAIL + " = '" + email + "'", null, null, null, null);

        return res;
    }

    public boolean LoginFuntion(String uname, String password) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor ans = db.query(SmartLocker_USERTable, new String[]{}, EMAIL + " = '" + uname + "'" + " and " + PASSWORD + " = '" + password + "'", null, null, null, null);

        if (ans.getCount() == 0)
            return false;
        else
            return true;

    }


    public boolean insert_FileData(String email ,String FileName, String FilePath, String FileExtension, String DateTime) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMAIL,email);
        contentValues.put(FNAME, FileName);
        contentValues.put(FPATH, FilePath);
        contentValues.put(FEXTENSION, FileExtension);
        contentValues.put(DATETIME, DateTime);

        long result = db.insert(SmartLocker_FileTable, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getFileDetails(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.query(SmartLocker_FileTable, new String[]{FID, FNAME, FPATH, FEXTENSION, DATETIME} , EMAIL + " = '" + email + "'", null, null, null, FNAME);
      return res;
    }

    public Boolean DeleteFile(String FileId) {

        Boolean ans = false;
        SQLiteDatabase db = this.getWritableDatabase();
        //edited 10:28 21/5
        long result = db.delete(SmartLocker_FileTable, FID + " = '" + FileId+"'", null);

        if (result == -1)
            return false;
        else
            return true;

    }

    public boolean UpdateKey(String userEmail, String fkey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FILE_KEY, fkey);

        long result = db.update(SmartLocker_USERTable, contentValues, EMAIL + " =  '" + userEmail + "' ", null);

        if (result == -1)
            return false;
        else
            return true;
    }


}
