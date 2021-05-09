# OPSWAT assessment
The candidate assessment assignment for OPSWAT.

Candidate name - Vinay Ambre

## Requirements

1. Java

2. Maven

## Installation

1. Clone the repository
```aidl
git clone https://github.com/vinaya8/opswat-assessment.git
```
2. In the root directory of the project run the following two commands to build the project.
```aidl
mvn compile
mvn package
```

## Setting up the project

1. In the root directory of the project create a ```.env``` file and copy the contents of ```.env.example``` into it.
2. Next, In the ```.env``` file update the ```API_KEY``` parameter with your api key.
```aidl
API_KEY=<ENTER YOUR API KEY HERE>
```

## Running the project

1. In the root directory of the project, run the following command and also specify the file path as a parameter.
```
java -jar target/opswat-assessment-1.0-SNAPSHOT.jar <YOUR_FILE_PATH>
```