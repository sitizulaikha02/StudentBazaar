# Student Bazaar (Milestone 1) 

## Table of Contents

1. Sign up & Log in
2. User Preferences
3. Homepage Layout
4. User Profile Page

## 1. Sign up & Log in

### Frontend (UI/UX Design) - Najib
- Create the UI for sign-up and log-in pages, ensuring user-friendly design and navigation.
- Files: SignUpActivity.kt, LoginActivity.kt, activity_signup.xml, activity_login.xml

#### Tasks:
- Design the layout of the sign-up and log-in forms.
- Include input validation and error messages to guide the user experience.

### Backend (Firebase Integration) - Hareez
- Set up Firebase authentication.
- Files: FirebaseAuthService.kt

#### Tasks:
- Implement Firebase API for signing in and signing up.
- Test the authentication flow, ensuring successful log-in, log-out, and proper error handling.

---

## 2. User Preferences

### Frontend (Preference UI) - Es
- Create the UI for users to set and manage preferences.
- Files: UserPreferencesActivity.kt, activity_user_preferences.xml

#### Tasks:
- Design the layout for preference settings.
- Implement the functionality to update preferences through the UI.

### Backend (Database Structure) - Zu
- Design and implement the database structure for storing user preferences.
- Files: PreferencesManager.kt

#### Tasks:
- Define necessary fields for user preferences (e.g., categories, notification settings).
- Create Firestore collections or a Realtime Database structure to store preferences.

---

## 3. Homepage Layout

### Frontend (Homepage Design) - Najib
- Design the homepage layout that displays item listings.
- Files: HomepageActivity.kt, activity_homepage.xml

#### Tasks:
- Create sections for featured items, categories, and other relevant content.
- Ensure the homepage is visually appealing and responsive across devices.

### Backend (Data Handling) - Hareez
- Implement the backend logic to store and retrieve item listings from the database.
- Files: ItemRepository.kt

#### Tasks:
- Set up API endpoints for retrieving item listings.
- Test database retrieval and ensure data is correctly displayed on the homepage.

---

## 4. User Profile Page

### Frontend (Profile Page Design) - Zu
- Create the UI for the user profile page.
- Files: UserProfileActivity.kt, activity_user_profile.xml

#### Tasks:
- Design sections for displaying user information, preferences, and item listings.
- Include functionality for users to edit and update their information.

### Backend (Profile Data Management) - Es
- Implement backend functionality to manage user profiles.
- Files: ProfileRepository.kt

#### Tasks:
- Set up API endpoints to fetch and update user profile data.
- Ensure secure access to user profile information.
