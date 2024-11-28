package com.example.syntaxeventlottery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class UserControllerTest {

    private UserController userController;

    @Mock
    private UserRepository mockRepository;

    @Mock
    private DataCallback<Void> mockCallback;

    @Mock
    private DataCallback<User> mockUserCallback;

    @Before
    public void setUp() {

        // Initialize mock objects
        MockitoAnnotations.openMocks(this);

        // Create the UserController with the mocked repository
        userController = new UserController(mockRepository);
    }

    @Test
    public void testGetUserByDeviceID_Found() {

        // Create mock user array
        ArrayList<User> mockUsers = new ArrayList<>();

        // Create a mew user with specified values
        User user1 = new User("Device123", "test@example.com", "1234567890", "url1", "User1", null, null);

        // Add that user to the mock users array
        mockUsers.add(user1);

        // Simulate the behavior of getLocalUsersList() to return mock data (mockUsers) for testing
        when(mockRepository.getLocalUsersList()).thenReturn(mockUsers);

        // Use the getUserByDeviceID method to find a user with ID Device123
        User result = userController.getUserByDeviceID("Device123");

        // Verify the result
        assertEquals(user1, result);
    }

    @Test
    public void testGetUserByDeviceID_NotFound() {

        // Create mock user array
        ArrayList<User> mockUsers = new ArrayList<>();

        // Create a mew user with specified values
        mockUsers.add(new User("Device456", "test2@example.com", "0987654321", "url2", "User2", null, null));

        // Simulate the behavior of getLocalUsersList() to return mock data (mockUsers) for testing
        when(mockRepository.getLocalUsersList()).thenReturn(mockUsers);

        // Use the getUserByDeviceID method to find a user with ID Device123
        User result = userController.getUserByDeviceID("Device123");

        // Check that the result is null when no user with the given ID is found
        assertEquals(null, result);
    }

    @Test
    public void testAddUser_ValidUser() {

        // Create a valid user
        User validUser = new User("Device123", "test@example.com", "1234567890", "url1", "User1", null, null);

        // Call the addUser method with a valid user, and a mock callback
        userController.addUser(validUser, null, mockUserCallback);

        // Check if the addUserToRepo method in the repository was called with the correct arguments
        verify(mockRepository).addUserToRepo(eq(validUser), eq(null), eq(mockUserCallback));
    }

    @Test
    public void testAddUser_InvalidUser() {

        // Create an invalid user
        User invalidUser = new User(null, null, null, null, null, null, null);

        // Call the addUser method with a invalid user, and a mock callback
        userController.addUser(invalidUser, null, mockUserCallback);

        // Verify that the addUserToRepo method was never called on the repository
        verify(mockRepository, never()).addUserToRepo(any(User.class), eq(null), any());

        // Verify that the onError method was called on the callback with an IllegalArgumentException
        verify(mockUserCallback).onError(any(IllegalArgumentException.class));
    }

    @Test
    public void testRefreshRepository() {

        // Call the refreshRepository method to simulate refreshing user data
        userController.refreshRepository(mockCallback);

        // Check that fetchAllUsers was called on the mockRepository with the expected callback
        verify(mockRepository).fetchAllUsers(eq(mockCallback));
    }

    @Test
    public void testDeleteUser() {

        // Create a user to delete
        User userToDelete = new User("Device123", "test@example.com", "1234567890", "url1", "User1", null, null);

        // Call the deleteUser method to simulate deleting the user
        userController.deleteUser(userToDelete, mockCallback);

        // Check that deleteUserfromRepo was called on the mockRepository with the correct user and callback
        verify(mockRepository).deleteUserfromRepo(eq(userToDelete), eq(mockCallback));
    }
}