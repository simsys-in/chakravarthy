package in.chakravarthygroup.ip.chakravarthy2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    String TAG ="DBH";
    public DBHelper(Context context) {
        super(context, config.DB_NAME, null, config.DB_VERSION);
    }
    SQLiteDatabase db;
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "create table loan_order(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "refno TEXT, " +
                        "vou_date TEXT NOT NULL, " +
                        "ledger TEXT, " +
                        "product TEXT, " +
                        "fixed_due_amount TEXT," +
                        "ledger_category TEXT," +
                        "shedule TEXT," +
                        "ledger_outstanding TEXT," +
                        "company TEXT," +
                        "amount TEXT" + ");"
                ;
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG,"upgrade migration process to New ver :"+newVersion+" from old Ver: "+oldVersion);

        try {
            for (int i = oldVersion; i <= newVersion; ++i) {
                String migrationName = String.format("from_%d_to_%d.sql", i, (i + 1));
                Log.d(TAG, "Looking for migration file: " + migrationName);
                if (migrationName == "from_3_to_4.sql") {
                    db.execSQL("create table loan_receipt(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "refno TEXT, " +
                            "ledger TEXT, " +
                            "amount TEXT, " +
                            "vou_date TEXT, " +
                            "user_id TEXT, " +
                            "narration TEXT, " +
                            "export_tally_sts boolean NOT NULL default 0"+
                            ");");
                    db.execSQL("create table user(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT, " +
                            "password TEXT, " +
                            "company TEXT, " +
                            "narration TEXT" +
                            ");");
                }else{
                    Log.d(TAG,"Upgrade sql not found. for "+migrationName);
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "Exception running upgrade script:", exception);
        }
    }
}
