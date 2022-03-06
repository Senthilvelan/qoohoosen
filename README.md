# QooHoo Audio recorder & player
QooHoo Audio will record the voice and list in a recycler view and play

Followed MVC arch which has an activity theme of glassmorphism a semi-transparent screen.

The app made up of two version,
1st version has the Forground service to play the audio file( ie., shows on going notification)
2nd version getting ready for audio waves

QooHooAudio depends on audiovisualizer, glassmorphism and omrecorder lib.

### It has been configures for the audio frequency "44100"hz in mono which results best sound quality in wav format
Its possible to capture with background and can omit the silence using this lib

### The app needs only record mic permission and storage permission not needed,since app make use of internal memory supports upto 200 Mb for the demo purpose.permission.
App works fine for the demo, which takes care the quality of the sound, UX, UI, theme, code organization, and next level for optimization, adding waves (waves added ,need to fix the memory issue)

###awaiting for the feedback

#### For better UX - Moved the play/pause button to right side for easily access by thumb (Whole recycler Item touch is not so good)
#### Added audio waves when its playing, with animation.
#### When the user press the mic for recordring, Textbox (Edittext) for text messsage is irrelevent to show,
#### So the whole bottom sheet covered by the audio animation, to make more focus on audio rec
#### After adding  new record, the focus goes to the new item in the recycler

For next level : can add https://github.com/android/user-interface-samples for bubble chat

