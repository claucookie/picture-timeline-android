# picture-timeline-android
This repo represents the implementation of a basic app created to display a list of pictures as the user moves around.

## Purpose
The purpose of this implementation is to play with the location services.

## UI
| List                       | Detail                         |
| -------------------------- | ------------------------------ |
| TBC) | TBC |


## Architecture and Implementation approach
I've decided to go for the presentation pattern `MVVM` and `Clean Architecture` to decouple layers and concerns as much as possible.

Those layers will be held in the same module, separated by packages.

![layers](./packages.png)

If you would like to see a sample implementation with `MVP + CleanArch + Dagger2 + Feature modules`, feel free to go to this repo https://github.com/claucookie/android_lastfm_kotlin

The ViewModel has been implemented using `LiveData` and `Data Binding`.

![class diagram](./main_activity_diagram.png)

## Tools

- Kotlin : https://kotlinlang.org/
- Android Jetpack Components : https://developer.android.com/jetpack
- Glide : https://github.com/bumptech/glide
- Retrofit 2 : https://github.com/square/retrofit
- Detekt : https://github.com/arturbosch/detekt
- Koin : https://github.com/InsertKoinIO/koin
- Location Services: https://developer.android.com/training/location

## Testing

No tests for now, they were not part of the exercise.