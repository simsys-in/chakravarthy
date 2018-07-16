package in.chakravarthygroup.ip.chakravarthy2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = HttpHandler.class.getSimpleName();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LinearLayout LinearCollection;
    private LinearLayout LLimportfromPHP;

    private DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        LinearCollection = (LinearLayout) findViewById(R.id.LinearCollection);
        LLimportfromPHP =  (LinearLayout) findViewById(R.id.LLimportfromPHP);
        LinearCollection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openActivity2();
            }
        });

        LLimportfromPHP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new ImportfromPHP().execute();
            }
        });
    }
    public void openActivity2(){
        Intent intent = new Intent(this,CollectionActivity.class);
        startActivity(intent);
    }

    private class ImportfromPHP extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),"Json Data is downloading",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://api.simsys.org/inventory_voucher/chakravarthy_xml";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    DBHelper dbHelper = new DBHelper(DashboardActivity.this);
                    SQLiteDatabase sqldb = dbHelper.getWritableDatabase();

                    CollectionReference FSvoucher = db.collection("voucher");

                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray records = jsonObj.getJSONArray("data");
                    // looping through All Contacts
                    delete_all_loan_order();
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject c = records.getJSONObject(i);
                        Map<String, Object> data = new HashMap<>();
                        data.put("ledger", c.getString("PARTYNAME"));
                        data.put("refno", c.getString("VOUCHERNUMBER"));
                        data.put("vou_date", c.getString("DATE"));
                        data.put("product", c.getString("STOCKITEMNAME"));
                        data.put("fixed_due_amount", c.getString("FIXEDDUE"));
                        data.put("ledger_category", c.getString("EIAREANAME"));
                        data.put("shedule", c.getString("EISHEDULE"));
                        data.put("ledger_outstanding", c.getString("LBALANCE"));
                        data.put("company", c.getString("SVCURRENTCOMPANY"));
                        //data.put("XML",c.getString("XML"));
                        FSvoucher.document().set(data);

                        ContentValues values = new ContentValues();
                        values.put("ledger", c.getString("PARTYNAME"));
                        values.put("refno", c.getString("VOUCHERNUMBER"));
                        values.put("vou_date", c.getString("DATE"));
                        values.put("product", c.getString("STOCKITEMNAME"));
                        values.put("fixed_due_amount", c.getString("FIXEDDUE"));
                        values.put("ledger_category", c.getString("EIAREANAME"));
                        values.put("shedule", c.getString("EISHEDULE"));
                        values.put("ledger_outstanding", c.getString("LBALANCE"));
                        values.put("company", c.getString("SVCURRENTCOMPANY"));
                        long newRowId = sqldb.insert("loan_order", null, values);
                        Log.e("test","insert successfully id:"+ newRowId);
                        data.clear();
                    }
                    Log.e(TAG, "Updated Successfully");
                } catch (final JSONException e) {
                    Log.e("test", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Json parsing error1: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
        private void delete_all_loan_order(){
// Define 'where' part of query.
            //String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
// Specify arguments in placeholder order.
            //String[] selectionArgs = { "MyTitle" };
// Issue SQL statement.

            DBHelper dbHelper = new DBHelper(DashboardActivity.this);
            SQLiteDatabase sqldb = dbHelper.getWritableDatabase();

            int deletedRows = sqldb.delete("loan_order", null, null);
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
