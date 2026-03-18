# CatPoint Security System

## Overview
CatPoint is a home security system that integrates sensor monitoring and image analysis to detect potential intruders. The system is designed to distinguish between cats and other objects, preventing false alarms when pets are present.

## Architecture
The project consists of two main modules:
- **ImageService**: Handles image processing and cat detection using AWS Rekognition
- **SecurityService**: Manages security logic, sensor states, alarm statuses, and system arming

## Features
- Sensor monitoring (doors, windows)
- Image-based cat detection
- Alarm escalation based on sensor activity
- System arming modes (Disarmed, Armed Home, Armed Away)
- Status listener notifications

## Technologies
- Java 17
- Maven for build management
- JUnit 5 for testing
- Mockito for mocking
- AWS SDK for Rekognition
- SLF4J for logging
- Gson for JSON processing

## Building the Project
To build the project, ensure you have Java 17 and Maven installed.

```bash
mvn clean install
```

## Running Tests
```bash
mvn test
```

## Configuration
- Configure AWS credentials for Rekognition usage in ImageService
- Adjust sensor configurations as needed

## License
This project is part of the Udacity Java Developer Nanodegree program.
