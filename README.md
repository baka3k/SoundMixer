
# SoundUtilities  

This module is used to:
 - #### Concat audio files
 - #### Mix audio 
 - #### Convert file from mp3 to wav
## Todo(Maybe)
 - #### Boost audio file
 ## NOTE
Target of this project is not performance while mix/concat/boost audio, It just provides an idea or solution for developer if they don't want to use library such as ffmpeg,etc...
## Requirements
- Android 5.0+ (API 21)
## Gradle
build.gradle:
```groovy
allprojects {
    repositories {
        .....
        maven { url 'https://jitpack.io' } // add this line to build.gradle
    }
}
```
Add the dependency
```groovy
dependencies {
    implementation "com.github.baka3k:SoundMixer:0.0.1"
}
```
## Usage   
Ensure that your application has WRITE_EXTERNAL_STORAGE permission for run sample app

### Sound concat  
```  
val soundConcat = SoundConcat(applicationContext.cacheDir)
val inputFile1 = "/storage/emulated/0/Download/A.mp3"
val inputFile2 = "/storage/emulated/0/Download/B.mp3"
val outPutFileConcated = "/storage/emulated/0/Download/concat.wav"
soundConcat.concatMP3File(
    inputFile1 = inputFile1,
    inputFile2 = inputFile2,
     outputPath = outPutFileConcated
)
```

### Sound Mixer  
```  
/**  
* Mix sound, Reduce the volume in the parts with voice  
*/  
fun mix(musicPath: String?, voicePath:String, listSoundInfo: ArrayList<SoundInfo>, duration: Int): String {  

val mixedFolder = buildMixedFolder()
val outputMixing = mixedSoundFileName(mixedFolder)

// Mix sound  
val soundMixer = SoundMixer(applicationContext.cacheDir)
val inputFile1 = "/storage/emulated/0/Download/A.mp3"
val inputFile2 = "/storage/emulated/0/Download/B.mp3"
val outPutFileMixed = "/storage/emulated/0/Download/mixed.wav"
val duration = 15000 // mix and split 15 seconds
soundMixer.mix(
    inputFile1 = inputFile1,
    inputFile2 = inputFile2,
    outPutMixing = outPutFileMixed,
    duration = duration
)  
}  
```  

## Authors
- baka3k@gmail.com

## License  

  

SoundUtilities is released under the [The MIT License](LICENSE).  
