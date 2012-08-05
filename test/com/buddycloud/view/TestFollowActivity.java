package com.buddycloud.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.runner.RunWith;

import org.junit.Before;
import org.junit.Test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.R;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TestFollowActivity {
    private FollowActivity activity;
    private Button abortButton;
    private Button followActivityButton;

    @Before
    public void setUp() throws Exception {
        activity = new FollowActivity();
        activity.onCreate(null);
        abortButton = (Button) activity.findViewById(R.id.abort);
        followActivityButton = (Button) activity.findViewById(R.id.follow);
    }

    @Test
    public void shouldHaveAButtonThatSaysAbort() throws Exception {
        assertThat((String) abortButton.getText(), equalTo("Tests (would) Rock!"));
    }

    @Test
    public void shouldHaveAButtonThatSaysFollow() throws Exception {
        assertThat((String) followActivityButton.getText(), equalTo("Tests (would) Rock!"));
    }

    @Test
    public void pressingTheAbortButtonShouldAbortThis() throws Exception {
        abortButton.performClick();
        // ...
    }

    @Test
    public void testOnNewIntentIntent() {
	Intent intent = emptyIntent();
	new FollowActivity().onNewIntent(intent);
	// what to test
    }

    /** creates a test data intent */
    private Intent emptyIntent() {
	Intent i = new Intent() {
	    public char[] getCharArrayExtra(String x) {
		char[] values = null;
		return values;
	    }
	};
	return i;
    }
}
