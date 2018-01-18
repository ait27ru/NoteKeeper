package com.pluralsight.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_POSITION            = "com.pluralsight.notekeeper.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID  = "com.pluralsight.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE      = "com.pluralsight.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT       = "com.pluralsight.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteInfo mOriginalNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSpinnerCourses = findViewById(R.id.spinner_courses);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<CourseInfo>(this,
                android.R.layout.simple_spinner_dropdown_item, courses);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        Log.d(TAG, "onCreate");
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        String courseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        mOriginalNote.setCourse(course);
        mOriginalNote.setTitle(savedInstanceState.getString(ORIGINAL_NOTE_TITLE));
        mOriginalNote.setText(savedInstanceState.getString(ORIGINAL_NOTE_TEXT));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNote.getCourse().getCourseId());
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNote.getText());
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNote.getTitle());
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote || mNote == null) {
            return;
        }

        if (mOriginalNote == null) {
            mOriginalNote = new NoteInfo(null, null, null);
        }

        mOriginalNote.setCourse(mNote.getCourse());
        mOriginalNote.setTitle(mNote.getTitle());
        mOriginalNote.setText(mNote.getText());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = mNotePosition == POSITION_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
        Log.i(TAG, "mNotePosition: " + mNotePosition);
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i(TAG, "Cancelling at position: " + mNotePosition);
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                restoreNoteValuesFromOriginalNote();
            }
        } else {
            saveNoteFromUIControls();
        }
        Log.d(TAG, "onPause");
    }

    private void restoreNoteValuesFromOriginalNote() {
        if (mOriginalNote == null) {
            return;
        }
        mNote.setCourse(mOriginalNote.getCourse());
        mNote.setTitle(mOriginalNote.getTitle());
        mNote.setText(mOriginalNote.getText());
    }

    private void saveNoteFromUIControls() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }
}
