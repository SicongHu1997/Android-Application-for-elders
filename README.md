
# Ecare - APP Designed For Elder People
With an ever-evolving transport system, people require the means to plan and carry out their journeys more safely and efficiently than before. This is especially true of elderly people, who have the disadvantage of being unfamiliar with current mobile devices, and of having special needs in some cases.

The application is mainly for assisted people to get to their destinations with ease of access, special features to simplify their journey and everyday life. It is also an extremely useful tool for the assisted people and their carers to communicate with each other.


# Getting Started

  - Clone this repository
  - Use Android Studio to sync the project and run on the emulator or Android phone

### Prerequisites
```sh
Android version above 4.2
Latest version of Google Play
```
### Installing
on emulator
```sh
Use Android Studio to make the project (Ctrl+F9)
Run app and choose the emulator (Shift+F10)
```
on Android phone
```sh
Go to Settings > About phone, Click Build number seven-times
Then go to Settings > Developer options，enable USB test
Use Android Studio to run app and choose your phone (Shift+F10)
```

# Running the uit test cases
Explain how to run the automated tests for this system
### Break down into end to end tests
```sh

- MapFragment unit test, simply run the test to check if the destination url the app provides for
  parsing the routing details is accurate



```
### And coding style tests
Explain what these tests test and why
```sh

```

# Instructions on how to run the app and features

```sh
- 1st Time users have to register an account in order to use the app
  (mandatory to be logged in to use the app)
   
- To access the contacts list, click the top right button, User can add a contact if they provide 
  the email of another user. User can engage with video chat/Text chat with an user they added
  by clicking the user of choice from the contact list
  
- User can send thier current location to any1 in thier contact list(it will be a clickable link which
  opens the map and places the coordinates into the destination text box)
  
- The top left button is our user settings button, where the user can choose to change thier contact
  information
  
- User can press the help button (middle button) and it will send an SMS containing 
  the user's current coordinates to the number you set as your emergency contact number at the user
  settings
  
- To access the mapping functionalies, click the bottom left button. The user has to provide the start 
  and end location to view the route between them, User can either click use current location and
  the app will automatically input the user's location to the start location field, or they can manually
  type in where they want to go with text autocompelte.
  Likewise, to select thier destination location, they can use place picker where it will
  list all notable points of interest to the user and they can simply slect where they want to go.
  
- Nearby Places feature, at the bottom of the map screen, the user can choose to find near parks,
  hospitals, etc, once found, the user can click on the given markers and can choose to
  route to them from wherever they please to start from.
  
- To access our other tools, user can click the bottom right button, where it has a 
  checklist feature a alarm clock feature and the magnifier feature.
  
  The checklist feature, the user can add a note of what they want and set a initial priority of it,
  the user can delete the note once it has been done by simply swiping the note to the right/left.
  
  The alarm clock feature, the user can set a date and time and the user's phone will vibrate and notify
  the user when the set time is reached.
  
  The magnifier feature, user can click the plus button to zoom in, in the case if the zoom ratio is what
  they want, they can click the euals button to have the image temporaily stored. The user can then click
  settings button to change the colour, to further increase viewerbility of the image. Hold the screen to
  enable auto focus, no auto focus vice versa.
  

```

# The list of Implemented Features
```sh
. Implemented Text Chating Functionality
. Implmeneted Video Chat functionality
. Implemented getting user's location
. Implemented tracking of user's location
. Setted up FireBase Server
. Implemented User sign in and sign up 
. Implemented contact List and ability to add contacts
. Implemented the ability to change the nicknames of your contacts
. Implemented Location routing from one point to another
. Implemented Basic Checklist Feature
. Implemented Weather Feature 
. Implemented Setting a alarmclock Feature
. Implemented Help button Feature
. Implemented Magnifier feature 
. Implemented Nearby Locations feature
. Implemented ability to send current location through text chat
```



# Built With
```sh
- Firebase - A comprehensive mobile development platform
- Sinch - Provide a cloud based communications service
```

