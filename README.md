# RecordSearch
```
Chase M. Peak
Digital Transformation Hub
November  2019
```

The RecordSearch application utilizes Amazon Web Services in order to transcribe spoken record information, interpret and format the user's command, and present a practical database query available for query by law enforcement. This is achieved by providing a local application that is installed on the user's computer, and taking advantage of the Amazon Transcribe Streaming API. The commands received by the application follow a rigid format that involves listing the individual characters of a record (i.e. license plate or driver's license) with the NATO phonetic alphabet or the standard US Law Enforcement phonetic alphabet (for [reference](https://en.wikipedia.org/wiki/APCO_radiotelephony_spelling_alphabet)).

## How to get started
There are a few prerequisites to get this application running on a local device.

### Prerequisites: software
The following is/are the base software need(s) that must be met in order to run the application:

* [Java SE 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html) or later

### Prerequisites: AWS configuration
In order to use web services provided by AWS, the current edition of the application requires that the user's computer has programmatic access. This can be set up for an [IAM user](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users_create.html) within the AWS Account that is using Transcribe. When creating this user, they will require the policy *AmazonTranscribeFullAccess*, and the [public/private keys](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html) will need to be accessible to the application so that it can authenticate the API calls. To change this, access the source code in  **Transcriber.java** in the method initiateTranscription() and manipulate the (as of November 2019) commented out section. Once the access configuration has been completed, Amazon Transcribe has to be equipped with the correct terminology to accurately transcribe the user's audio to text. A copy of the vocabulary must be upload to Amazon Transcribe. To upload the provided file **AlphaNumericAndStates.txt** (found within the vocabulary directory), enter the [developer's console](https://aws.amazon.com/console/) and go to: Amazon Transcribe -> Custom Vocabulary -> Create Vocabulary. From there, upload the file, and name the vocabulary 'AlphaNumericAndStates'. To fit a different vocabulary, a change can be made in  **Transcriber.java** in the getRequest() method where the name of the vocabulary is specified. To manipulate/expand the vocabulary, check [this](https://docs.aws.amazon.com/transcribe/latest/dg/how-vocabulary.html#create-vocabulary-list) out.

### Installation
The program can be installed by downloading the RecordSearchApplication directory containing **RecordSearch.jar**. This runnable jar file can be executed within it's directory by the following command:
```
java -jar ./RecordSearch.jar
```

By adding a *-debug* flag following the previous command, the application spawns a subdirectory in the same space with a file **RSDebug.out**. This contains a log of the commands that the application recieves, showing the progression of the transcription process as the audio data is being streamed to Amazon Transcribe in real-time.

## Testing
The commands are restricted to the following format:
```
run [state] [license plate/driver's license] [record information]

ex: run Iowa license plate adam boy charles one two three
```
To run the program, press and hold [enter] for the duration of the voice command. In order to modify this procedure for a different button, find the setupKeyListener() method in **myGUI.java**. See the KeyEvent class static fields [here](https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html) for help.

## Need-to-knows
The jar file must be accompanied by the vocabulary directory. This folder contains two text files: **states.txt** and **phoneticAlphabet.txt**. These can be modified in order to change the acceptable inputs for the [state] and individual values within the [record information] parameters. Look to **RecordInterpreter.java** to see how the values are parsed from the text files in case additions/changes need to be made. Also, to manipulate the ouput of the query, look to **RecordInterpreter.java** in the method constructRecordQuery(). Here, the application's output can be manipulated to present the preferred database query.

## Acknowledgments
Portions of the code for this application have been adapted from the following source(s):
* [Amazon Documentation](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/examples-transcribe-bidirectional-streaming.html)

## Reference
For more information on Amazon Transcribe and how it functions, check out the following links:
* [developer's page](https://aws.amazon.com/transcribe/)
* [How transcription-streaming works](https://docs.aws.amazon.com/transcribe/latest/dg/streaming.html)
