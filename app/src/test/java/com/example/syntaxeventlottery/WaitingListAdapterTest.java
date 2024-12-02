package com.example.syntaxeventlottery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaitingListAdapterTest {

    // Initialize a mock user list and adapter
    private List<User> mockUserList;
    private WaitingListAdapter adapter;

    @Before
    public void setUp() {

        // Create and populate a mock user list
        mockUserList = new ArrayList<>();
        mockUserList.add(new User("Device10", "test1@example.com", "1234567890", "url1", "User10", null, null));
        mockUserList.add(new User("Device20", "test2@example.com", "0987654321", "url2", "User20", null, null));

        // Initialize the adapter with the mock data
        //adapter = new WaitingListAdapter(mockUserList);
    }

    @Test
    public void testGetItemCount() {

        // Verify the item count matches the mock data size
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testOnBindViewHolder() {

        // Create a mock of the ViewHolder
        WaitingListAdapter.ViewHolder mockViewHolder = mock(WaitingListAdapter.ViewHolder.class);

        // Mock the TextView objects inside the ViewHolder
        mockViewHolder.usernameTextView = mock(TextView.class);
        mockViewHolder.phoneNumberTextView = mock(TextView.class);
        mockViewHolder.emailTextView = mock(TextView.class);

        // Call onBindViewHolder to bind the first user's data, 0 is the first position in the list
        adapter.onBindViewHolder(mockViewHolder, 0);

        // Verify that the correct data was set on the TextViews
        verify(mockViewHolder.usernameTextView).setText("Username: User10");
        verify(mockViewHolder.phoneNumberTextView).setText("Phone: 1234567890");
        verify(mockViewHolder.emailTextView).setText("Email: test1@example.com");
    }
}