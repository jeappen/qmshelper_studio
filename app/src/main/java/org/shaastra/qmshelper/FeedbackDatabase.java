package org.shaastra.qmshelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FeedbackDatabase {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_USERID = "user_id";
	public static final String KEY_EVENT = "event_name";
	public static final String KEY_EVENTID = "event_id";
	//public static final String KEY_SENT = "yes_no";
    public static final String KEY_RATING = "rating";
    public static final String KEY_COMMENT = "comments";// ho
    public static final String KEY_SENT = "sent";
    public static final String[] KEY_Q = "Q1\tQ2\tQ3\tQ4\tQ5\tQ6\tQ7\tQ8\tQ9\tQ10\tQ11\tQ12\tQ13\tQ14\tQ15".split("\t");//"i\tii\tiii\tiv\tv\tvi\tvii\tviii\tix\tx\txi\txii\txiii\txiv\txv".split("\t");//"'Q1'\t'Q2'\t'Q3'\t'Q4'\t'Q5'\t'Q6'\t'Q7'\t'Q8'\t'Q9'\t'Q10'\t'Q11'\t'Q12'\t'Q13'\t'Q14'\t'Q15'".split("\t");
    public static final String KEY_CHOICE = "choice";

	private static final String DATABASE_NAME = "Feedback";
	private static final String DATABASE_TABLE = "feedbackTable";
	private static final int DATABASE_VERSION = 1;

    private static String savelocation ="",backupsavelocation="";
	private Data myData;
	private final Context myContext;
	private SQLiteDatabase myDatabase;

	private static class Data extends SQLiteOpenHelper{

		public Data(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String makedb="CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_USERID + " TEXT NOT NULL, " +
                    KEY_EVENT + " TEXT NOT NULL, "+
                    KEY_EVENTID + " TEXT NOT NULL, "+
                    KEY_COMMENT + " TEXT, "+
                    TextUtils.join(" TEXT , ",KEY_Q)+" TEXT , "+
                    KEY_SENT+" TEXT NOT NULL "
                    +");";
            db.execSQL(makedb);
            Log.d("makeFeedbackDB",makedb);
			ContentValues cv = new ContentValues();
			cv.put(KEY_ROWID,0);
			cv.put(KEY_USERID, "USERID");
			cv.put(KEY_EVENTID, "0");
			cv.put(KEY_EVENT, "EVENT");
            for(int i=0;i<KEY_Q.length;i++){
                cv.put(KEY_Q[i], "-2");
                Log.d("storeing",KEY_Q[i]);
                }

           // cv.put(KEY_RATING, "-1");
            cv.put(KEY_COMMENT, "COMMENT");
           cv.put(KEY_SENT, "sent");
           // cv.put(KEY_CHOICE, -1);
            //cv.put(KEY_EVENT, "EVENT");

			//cv.put(KEY_SENT, 2);
            Log.d("cv", cv.toString());
            long chk=db.insert(DATABASE_TABLE, null, cv);
            if(chk!=0)
                Log.d("dberr","yes");


            Log.d("Created Feedback database?", "yes");
            Cursor dbCursor = db.query(DATABASE_TABLE, null, null, null, null, null, null);
            String columnNames = TextUtils.join(",",dbCursor.getColumnNames());
            Log.d("Columnnames",columnNames);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}
    public void makeSent(){
        myData = new Data(myContext);
        myDatabase = myData.getWritableDatabase();
        myDatabase.execSQL("UPDATE "+DATABASE_TABLE+" SET "+KEY_SENT+" = \'yes\' WHERE "+KEY_ROWID + "!= 0");
    }
    public void delete(){
        myData = new Data(myContext);
        myDatabase = myData.getWritableDatabase();
      myDatabase.delete(DATABASE_TABLE, KEY_ROWID + "!="+0, null);
    }
	public FeedbackDatabase(Context c){

        Resources res = c.getResources();
        savelocation=""+res.getString(R.string.feedback_csv);
        backupsavelocation=""+res.getString(R.string.feedback_backup_csv);

        myContext = c;
	}
	public FeedbackDatabase open(){
		myData = new Data(myContext);
		myDatabase = myData.getWritableDatabase();
		return this;
	}
	public long createEntry(String userid, String event,String eventid,int rating[], String comment, String sent){
		ContentValues cv = new ContentValues();
		cv.put(KEY_EVENT, event);
		cv.put(KEY_USERID, userid);
		cv.put(KEY_EVENTID, eventid);
        for(int i=0;i<KEY_Q.length;i++) {
            cv.put(KEY_Q[i], String.valueOf(rating[i]));
            Log.d("ts",String.valueOf(rating[i]));
        }
        cv.put(KEY_COMMENT,comment);
       cv.put(KEY_SENT,sent);

        // cv.put(KEY_SENT, sent);
		return myDatabase.insert(DATABASE_TABLE, null, cv);
	}
	public long deleteEntry(String userid, String event){
		return myDatabase.delete(DATABASE_TABLE,KEY_USERID+"='"+userid+"' and "+KEY_EVENT+"='"+event+"'", null);
	}
	public long deleteEntryAlso(String userid, String eventid){
		return myDatabase.delete(DATABASE_TABLE,KEY_USERID+"='"+userid+"' and "+KEY_EVENTID+"='"+eventid+"'", null);
	}
    private String[] concat(String[] A, String[] B) {
        int aLen = A.length;
        int bLen = B.length;
        String[] C= new String[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }
	public String[][] getData(){
		int count=0;
		int i=0;
		String[] columns = concat(new String[]{KEY_ROWID,KEY_USERID,KEY_EVENT,KEY_EVENTID,KEY_COMMENT,KEY_SENT},KEY_Q);
       // Log.d("test",TextUtils.join(",,,",concat(new String[]{KEY_ROWID,KEY_USERID,KEY_EVENT,KEY_EVENTID,KEY_COMMENT,KEY_SENT},KEY_Q)));
		Cursor c = myDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int userid = c.getColumnIndex(KEY_USERID);
        //Log.d("colid",String.valueOf(userid));
		int event = c.getColumnIndex(KEY_EVENT);
		int row = c.getColumnIndex(KEY_ROWID);
        int[] q=new int[KEY_Q.length];
        String dbg="";
        for(int j=0;j<KEY_Q.length;j++) {
            q[j] = c.getColumnIndex(KEY_Q[j]);
            dbg=dbg+","+String.valueOf(q[i]);
        }
        //Log.d("dbg getdata",dbg);
        int comment = c.getColumnIndex(KEY_COMMENT);
        int sent = c.getColumnIndex(KEY_SENT);

       // Log.d("colind",String.valueOf(sent));
      //  int choice = c.getColumnIndex(KEY_CHOICE);

		//int sent = c.getColumnIndex(KEY_SENT);
		
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			count++;
		}
		String[][] data = new String[20][count];
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                if(c.getString(row).equals("0"))
                    data[0][i] = "#";
                else
				data[0][i] = c.getString(row);
				data[1][i] = c.getString(userid);
				data[2][i] = c.getString(event);
                data[3][i]=c.getString(comment);

            int j;
            for(j=0;j<KEY_Q.length;j++) {
                if(c.getInt(q[j])==-2)
                    data[j+4][i] = "Q"+String.valueOf(j+1);
                else
                    data[j+4][i] =String.valueOf(c.getInt(q[j]));
                //Log.d("dbgQ",String.valueOf(c.getInt(q[j]))+"index"+String.valueOf((q[j])));

            }

            data[j+4][i]=c.getString(sent);
				i++;
		}
		return data;
	}
	/*public String[][] getDataToSend(){
		int count=0;
		int i=0;
		String[] columns = new String[]{KEY_ROWID,KEY_USERID,KEY_EVENT,KEY_EVENTID,KEY_SENT};
		Cursor c = myDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int userid = c.getColumnIndex(KEY_USERID);
		int eventid = c.getColumnIndex(KEY_EVENTID);
		int sent = c.getColumnIndex(KEY_SENT);
		
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			if(c.getInt(sent)==0)
				count++;
		}
		String[][] data = new String[2][count];
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			if(c.getInt(sent)==0){
				data[0][i] = c.getString(eventid);
				data[1][i] = c.getString(userid);
				i++;
			}
		}
		return data;
	}*/
	public int getCount(){
		int count=0;
		String[] columns = new String[]{KEY_ROWID,KEY_USERID,KEY_EVENT,KEY_EVENTID};//,KEY_SENT};
		Cursor c = myDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
		//int sent = c.getColumnIndex(KEY_SENT);
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			//if(c.getInt(sent)==0)
				count++;
		}
		return count;
	}
	public void updateEntry(String userid, String eventid, int put) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		//cv.put(KEY_SENT, put);
		myDatabase.update(DATABASE_TABLE, cv, KEY_USERID+"='"+userid+"' AND "+KEY_EVENTID+"='"+eventid+"'", null);
	}
	public void close(){
		myData.close();
	}
	public void dbToCsv(boolean backup) throws IOException {
		String[][] data = getData();
		File file;
        if(backup)

            file= new File(backupsavelocation);
        else
        file= new File(savelocation);
		// if file doesnt exists, then create it
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		int numEntries = data[0].length;
		int numbCols = data.length;
        numbCols=numbCols-1;

		for (int i = 0; i < numEntries; i++) {
			for (int j = 0; j < numbCols; j++) {
				bw.write(data[j][i] + ",");
			}
			bw.write("\n");
		}
		bw.close();
		fw.close();
	}
	public boolean findEntry(String userid, String eventid){
		String[] columns = new String[]{KEY_ROWID,KEY_USERID,KEY_EVENT,KEY_EVENTID};
		Cursor c = myDatabase.query(DATABASE_TABLE, columns, KEY_USERID+"='"+userid+"' AND "+KEY_EVENTID+"='"+eventid+"'", null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		if(c.getCount()!=0)
			return true;
		return false;
	}
}
