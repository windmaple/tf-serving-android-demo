# Android sample to demonstrate sending requests to TF Serving

This sample Android app demonstrates how to send a request to a backend that runs TensorFlow 
Serving. It uses Gradle to download a [cat image](https://tensorflow.org/images/blogs/serving/cat.jpg) 
and then send the RGB values in a request to TF Serving. TF Serving runs a pretrained [Resnet model](https://tfhub.dev/tensorflow/resnet_50/classification/1) 
and returns the result back to the app to display the classification result in the UI.

## Requirements

*   Android Studio 4+ (installed on a Linux, Mac or Windows machine)
*   An Android device, or an Android Emulator

## Build and run
To run the app:
1) Download the pretrained SavedModel [Resnet model](https://tfhub.dev/tensorflow/resnet_50/classification/1)
2) Start TF Serving using: 
```
docker run -it --rm -p 8501:8501  -v "PATH_TO_RESNET_SAVEDMODEL:/models/resnet" -e MODEL_NAME=resnet tensorflow/serving
```
3) Run the app in Android Studio
4) If you are not using an Android emulator, make sure to replace '10.0.2.2' with your TF Serving host's IP address
