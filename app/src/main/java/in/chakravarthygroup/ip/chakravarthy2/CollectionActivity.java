package in.chakravarthygroup.ip.chakravarthy2;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CollectionActivity extends AppCompatActivity {
    Button btnSave;
    private ListView lv;
    private TextView tvVOUCHERNUMBER;
    private TextView tvDATE;
    private TextView etAMOUNT;
    private AutoCompleteTextView actvPARTYNAME;
    private static final String TAG = HttpHandler.class.getSimpleName();
    ProgressBar pgsBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DBHelper dbHelper = new DBHelper(CollectionActivity.this);

    TextView result;
    String[] fruits = {"லட்சுமி சுப்பிரமணி ராஜீவ்நகர்", "வேலுசாமி வளநாள் அனுபட்டி", "கமலா பவித்ரா அனுபட்டி", "Date", "Grape", "Kiwi", "Mango", "Pear"};
    ArrayList<String> voucherNameList = new ArrayList<String>();
    ArrayList<String> SampleArrayList = new ArrayList<String>();

    ArrayList<HashMap<String, String>> contactList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        tvVOUCHERNUMBER =  (TextView) findViewById(R.id.tvVOUCHERNUMBER);
        tvDATE = (TextView) findViewById(R.id.tvDATE);
        etAMOUNT = (EditText) findViewById(R.id.etAMOUNT);
        actvPARTYNAME = (AutoCompleteTextView) findViewById(R.id.actvPARTYNAME);
        result =  (TextView) findViewById(R.id.result);
        btnSave = findViewById(R.id.btnSave);
        pgsBar =  (ProgressBar)findViewById(R.id.pBar);
        pgsBar.setVisibility(View.GONE);

        tvDATE.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //writeNewUser("123","senthil","bestsenthil@gmail.com");
                //addReceipt();
                insert_receipt();
            }
        });

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        //getListItems1();
        //new GetContacts().execute();
        //getsVoucher();
        getVouchers();
    }
    private void addReceipt(){
        Map<String, Object> data = new HashMap<>();
        data.put("PARTYNAME", actvPARTYNAME.getText().toString());
        data.put("VOUCHERNUMBER", tvVOUCHERNUMBER.getText().toString());
        data.put("AMOUNT", etAMOUNT.getText().toString());
        data.put("DATE", etAMOUNT.getText().toString());
        data.put("exportedTally", false);
        pgsBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Source source = Source.CACHE;
        db.collection("receipt")
        .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Log.d(TAG, "Event document added - id: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Receipt document has been added"+documentReference.getId(), Toast.LENGTH_SHORT).show();
                        export2tally();
                        formClear();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding event document", e);
                        Toast.makeText(getApplicationContext(), "Receipt document could not be added", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void insert_receipt(){
        ContentValues data = new ContentValues();
        data.put("ledger", actvPARTYNAME.getText().toString());
        data.put("refno", tvVOUCHERNUMBER.getText().toString());
        data.put("amount", etAMOUNT.getText().toString());
        data.put("vou_date", tvDATE.getText().toString());
        //data.put("exportedTally", false);
        pgsBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Gets the data repository in write mode
        SQLiteDatabase sqldb = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        // Insert the new row, returning the primary key value of the new row
        long newRowId = sqldb.insert("loan_receipt", null, data);
        Log.d("Saved","Id :"+ newRowId);
        Toast.makeText(getApplicationContext(), "Receipt has been added"+newRowId, Toast.LENGTH_SHORT).show();
        export2tally();
        formClear();
    }
    private void formClear(){
        actvPARTYNAME.setText("");
        tvVOUCHERNUMBER.setText("");
        etAMOUNT.setText("");
        actvPARTYNAME.setFocusable(true);
        tvDATE.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        pgsBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
    }
    public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);


    }
    private void test(){
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Collection", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(),"added with id",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error adding document", e);
                        Toast.makeText(getApplicationContext(),"Error Adding",Toast.LENGTH_SHORT).show();

                    }
                });
        Toast.makeText(getApplicationContext(),"Test func",Toast.LENGTH_SHORT).show();


    }
    private void saveCities(){
        CollectionReference cities = db.collection("cities");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "San Francisco");
        data1.put("state", "CA");
        data1.put("country", "USA");
        data1.put("capital", false);
        data1.put("population", 860000);
        cities.document("SF").set(data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Los Angeles");
        data2.put("state", "CA");
        data2.put("country", "USA");
        data2.put("capital", false);
        data2.put("population", 3900000);
        cities.document("LA").set(data2);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("name", "Washington D.C.");
        data3.put("state", null);
        data3.put("country", "USA");
        data3.put("capital", true);
        data3.put("population", 680000);
        cities.document("DC").set(data3);

        Map<String, Object> data4 = new HashMap<>();
        data4.put("name", "Tokyo");
        data4.put("state", null);
        data4.put("country", "Japan");
        data4.put("capital", true);
        data4.put("population", 9000000);
        cities.document("TOK").set(data4);

        Map<String, Object> data5 = new HashMap<>();
        data5.put("name", "Beijing");
        data5.put("state", null);
        data5.put("country", "China");
        data5.put("capital", true);
        data5.put("population", 21500000);
        cities.document("BJ").set(data5);
    }
    private void getVouchers(){
        SQLiteDatabase sqldb = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                "id",
                "ledger",
                "ledger_category"
        };

// Filter results WHERE "title" = 'My Title'
        String selection = "id" + " = ?";
        String[] selectionArgs = { "10" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                "ledger" + " DESC";

        Cursor cursor = sqldb.query(
                "loan_order",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow("id"));
            itemIds.add(itemId);
            String ledger = cursor.getString(cursor.getColumnIndexOrThrow("ledger"));
            voucherNameList.add(ledger);

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (CollectionActivity.this, android.R.layout.select_dialog_item, voucherNameList);

        actvPARTYNAME.setThreshold(1);//will start working from first character
        actvPARTYNAME.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actvPARTYNAME.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                get_loan_order(item);
                Log.d("your selected item", "" + item);
            }
        });

        cursor.close();
    }
    private void getsVoucher(){
        db.collection("voucher").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        voucherNameList.add(document.getData().get("PARTYNAME").toString());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String> (CollectionActivity.this, android.R.layout.select_dialog_item, voucherNameList);

                    actvPARTYNAME.setThreshold(1);//will start working from first character
                    actvPARTYNAME.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                    actvPARTYNAME.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                            String item = parent.getItemAtPosition(position).toString();
                            getVoucher(item);
                            //Log.d("your selected item", "" + item);
                        }
                    });
                } else {
                    Log.d("getsVoucher", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    private static ArrayList<Type> mArrayList = new ArrayList<>();

    private void getData(){

        result.setText("getData");
        DocumentReference docRef = db.collection("cities").document("SF");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        result.setText("DocumentSnapshot data: "+document.getData());
                        Log.d("test", "DocumentSnapshot data: " + document.getData());

                    } else {
                        result.setText("No such document");
                        Log.d("Test2", "No such document");
                    }
                } else {
                    result.setText("get failed with "+ task.getException());
                    Log.d("tes4", "get failed with ", task.getException());
                }
            }
        });
    }

    private void getListItems() {
        db.collection("some collection").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d("Collection", "onSuccess: LIST EMPTY");
                            return;
                        } else {
                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    Log.d("Collection", "onSuccess: DOCUMENT" + documentSnapshot.getId() + " ; " + documentSnapshot.getData());
                                    DocumentReference documentReference1 = FirebaseFirestore.getInstance().document("some path");
                                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Type type= documentSnapshot.toObject(Type.class);
                                            Log.d("Collection", "onSuccess: " + type.toString());
                                            mArrayList.add(type);
                                            Log.d("Collection", "onSuccess: " + mArrayList);
                                        /* these logs here display correct data but when
                                         I log it in onCreate() method it's empty*/
                                        }
                                    });
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error getting data!!!", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getListItems1() {
        db.collection("cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Collection", document.getId() + " => " + document.getData());
                                SampleArrayList.add(document.getData().get("name").toString());
                            }
                        } else {
                            Log.d("Collection", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void ArrayValueAddFunction() {
        SampleArrayList.add("APPLE");
        SampleArrayList.add("BOY");
        SampleArrayList.add("CAT");
        SampleArrayList.add("DOG");
        SampleArrayList.add("ELEPHANT");
        SampleArrayList.add("FISH");

        Toast.makeText(CollectionActivity.this, "Values added successfully.", Toast.LENGTH_SHORT).show();
    }
    private void getDatafromUrl(){
        String url ="";
        try {
            String line, newjson = "http://api.simsys.org/inventory_voucher/chakravarthy_xml";
            URL urls = new URL(url);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urls.openStream(), "UTF-8"))) {
                while ((line = reader.readLine()) != null) {
                    newjson += line;
                    // System.out.println(line);
                }
                // System.out.println(newjson);
                String json = newjson.toString();
                JSONObject jObj = new JSONObject(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CollectionActivity.this,"Json Data is downloading",Toast.LENGTH_LONG).show();

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
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    //JSONArray contacts = jsonObj.getJSONArray("contacts");
                    JSONArray records = jsonObj.getJSONArray("data");
                    // looping through All Contacts
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject c = records.getJSONObject(i);
                        String VOUCHERNUMBER = c.getString("VOUCHERNUMBER");
                        String PARTYNAME = c.getString("PARTYNAME");
                        //String email = c.getString("email");
                        //String address = c.getString("address");
                        //String gender = c.getString("gender");

                        // Phone node is JSON Object
                        JSONObject dues = c.getJSONObject("dues");
                        String due1 = dues.getString("due1");
                        //String home = phone.getString("home");
                        //String office = phone.getString("office");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", VOUCHERNUMBER);
                        //contact.put("name", name);
                        //contact.put("email", email);
                        //contact.put("mobile", mobile);

                        // adding contact to contact list
                       // contactList.add(contact);
                        SampleArrayList.add(PARTYNAME);
                    }
                } catch (final JSONException e) {
                    Log.e("test", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error1: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //ListAdapter adapter = new SimpleAdapter(CollectionActivity.this, contactList, R.layout.list_item, new String[]{ "email","mobile"}, new int[]{R.id.email, R.id.mobile});
            //lv.setAdapter(adapter);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CollectionActivity.this, android.R.layout.select_dialog_item, SampleArrayList);

            AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.actvPARTYNAME);
            actv.setThreshold(1);//will start working from first character
            actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
            actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                    String item = parent.getItemAtPosition(position).toString();
                    getVoucher(item);
                    Log.d("your selected item", "" + item);
                }
            });
        }
    }

    private void getVoucher(String PARTYNAME){
        //Toast.makeText(CollectionActivity.this, "Selected Item is: \t" + PARTYNAME, Toast.LENGTH_LONG).show();

        // Source can be CACHE, SERVER, or DEFAULT.
        Source source = Source.CACHE;

        // Get the document, forcing the SDK to use the offline cache
        db.collection("voucher")
                .whereEqualTo("PARTYNAME", PARTYNAME)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, "Firestore: "+document.getId() + " => " + document.getData());
                                //Log.d(TAG, "Firestore: " + " => " + document.getData().get("PARTYNAME").toString());
                                tvVOUCHERNUMBER.setText(document.getData().get("VOUCHERNUMBER").toString());
                                //tvDATE.setText(document.getData().get("DATE").toString());
                                etAMOUNT.setText(document.getData().get("AMOUNT").toString());
                                etAMOUNT.setFocusable(true);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void get_loan_order(String ledger){
        SQLiteDatabase sqldb = dbHelper.getReadableDatabase();
        String[] select = {"id","ledger","fixed_due_amount"};
        // Define 'where' part of query.
        String selection = "ledger" + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ledger };
// Issue SQL statement.
        //Cursor query = sqldb.query("loan_order", select, selection, selectionArgs,null,null,null);
        Cursor cursor = sqldb.rawQuery("SELECT id,ledger,refno,fixed_due_amount FROM loan_order WHERE ledger=?", new String[] {ledger + ""});
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d("ledger",cursor.getString(cursor.getColumnIndex("ledger")));
            tvVOUCHERNUMBER.setText(cursor.getString(cursor.getColumnIndex("refno")));
            //tvDATE.setText(document.getData().get("DATE").toString());
            etAMOUNT.setText(cursor.getString(cursor.getColumnIndex("fixed_due_amount")));
            etAMOUNT.setFocusable(true);
        }
        Toast.makeText(CollectionActivity.this,"test",Toast.LENGTH_LONG).show();
    }

    private void export2tally(){
        final String VOUCHERNUMBER = tvVOUCHERNUMBER.getText().toString().trim();
        final String AMOUNT = etAMOUNT.getText().toString().trim();
        final String PARTYNAME = actvPARTYNAME.getText().toString().trim();

        class UpdateEmployee extends AsyncTask<Void,Void,String>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(CollectionActivity.this,"Updating...","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(s);
                    String status = jsonObj.getString("status");
                    String msg = jsonObj.getString("msg");
                    Log.d(TAG,"Status : "+ status);
                    if (status.equals("success")){
                        Toast.makeText(CollectionActivity.this,"refno : "+VOUCHERNUMBER,Toast.LENGTH_LONG).show();
                        SQLiteDatabase sqldb = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("export_tally_sts", 1);
                        int count = sqldb.update("loan_receipt", values, "refno ='"+VOUCHERNUMBER+"'", null);
                        Log.d(TAG,"sql update export tally : "+count);
                    }else{
                        Toast.makeText(CollectionActivity.this,status+" not refno : "+VOUCHERNUMBER,Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(CollectionActivity.this,msg,Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading.dismiss();
                //Toast.makeText(CollectionActivity.this,s,Toast.LENGTH_LONG).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("VOUCHERNUMBER",VOUCHERNUMBER);
                hashMap.put("AMOUNT",AMOUNT);
                hashMap.put("PARTYNAME",PARTYNAME);

                RequestHandler rh = new RequestHandler();

                String s = rh.sendPostRequest("http://api.simsys.org/inventory_voucher/chakravarthy_export2tally",hashMap);

                return s;
            }
        }

        new UpdateEmployee().execute();
    }
}


