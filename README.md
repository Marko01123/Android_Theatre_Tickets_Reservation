# Android_Theatre_Tickets_Reservation
Android application for booking theatre tickets. It has six activities: Registration activity, Login activity, Shows activity, Projection activity, 
Reservation activity and activity for displaying all tickets reservations. Database contains four tables: user, show, projection, reservation. 
One user can reserve up to 2 tickets per projection. Minimum SDK is 26. Everything is stored locally. Internet connection needed only for
downloading JSON API and displaying pictures. Classes contain comments in Serbian for easier code understanding. App features:

Developed with Java in Android Studio
Pulled show listing from github using JSON API (https://github.com/Marko01123/singidunum_test)
User data, show details and ticket reservations are stored locally using SQLite Database
Passwords are hashed with random generated 8 bytes nonce with SHA256 before added in database
User data in app is encrypted with AES/CBC/PKCS7Padding, key size 256bit stored in AndroidKeyStore
