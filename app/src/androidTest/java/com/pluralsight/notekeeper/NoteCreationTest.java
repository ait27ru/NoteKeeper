package com.pluralsight.notekeeper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.*;

/**
 * Created by andrey on 21/01/2018.
 */
@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception {
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mNoteListActivityRule =
            new ActivityTestRule<NoteListActivity>(NoteListActivity.class);

    @Test
    public void createNewNote() throws Exception {
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of our test note";

        onView(withId(R.id.fab))
            .perform(click());

        onView(withId(R.id.spinner_courses))
            .perform(click());

        onData(allOf(instanceOf(CourseInfo.class), equalTo(course)))
            .perform(click());

        onView(withId(R.id.spinner_courses))
            .check(matches(withSpinnerText(containsString(course.getTitle()))));

        onView(withId(R.id.text_note_title))
            .perform(typeText(noteTitle))
            .check(matches(withText(containsString(noteTitle))));

        onView(withId(R.id.text_note_text))
            .perform(typeText(noteText), closeSoftKeyboard());

        onView(withId(R.id.text_note_text))
            .check(matches(withText(containsString(noteText))));

        pressBack();

        int index = sDataManager.getNotes().size() - 1;
        NoteInfo note = sDataManager.getNotes().get(index);
        assertEquals(course, note.getCourse());
        assertEquals(noteTitle, note.getTitle ());
        assertEquals(noteText, note.getText());
    }
}