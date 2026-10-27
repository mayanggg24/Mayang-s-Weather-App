[README.md](https://github.com/user-attachments/files/33993572/README.md)
# Java Weather App

A Java Swing desktop app that retrieves current weather from OpenWeatherMap and parses the JSON response using JSON-java (`org.json`).

## Requirements
- JDK 17 or newer
- VS Code with the Java Extension Pack (optional, but recommended)
- OpenWeatherMap API key
- JSON-java JAR in the `lib` folder

## Project structure
```text
WeatherApp/
├── src/
│   └── WeatherApp.java
├── lib/
│   └── json-20240303.jar   # Add this file yourself
└── README.md
```

## 1. Get the JSON library
Download a JSON-java JAR from the [JSON-java releases](https://github.com/stleary/JSON-java) and place it in `lib/`. Rename it to `json-20240303.jar`, or update the commands below to match its actual filename.

## 2. Get an API key
Create an account at [OpenWeatherMap](https://home.openweathermap.org/users/sign_up) and obtain an API key. The key may take time to activate.

## 3. Set the API key

Windows PowerShell:
```powershell
$env:WEATHER_API_KEY="YOUR_ACTUAL_API_KEY"
```

macOS/Linux:
```bash
export WEATHER_API_KEY="YOUR_ACTUAL_API_KEY"
```

Run the commands below in the same terminal session where you set the variable.

## 4. Compile and run

Windows:
```powershell
javac -cp "lib/json-20240303.jar" -d out src/WeatherApp.java
java -cp "out;lib/json-20240303.jar" WeatherApp
```

macOS/Linux:
```bash
javac -cp "lib/json-20240303.jar" -d out src/WeatherApp.java
java -cp "out:lib/json-20240303.jar" WeatherApp
```

## Features
- Search weather by city name
- Displays temperature in Celsius, condition, humidity, and wind speed
- Handles empty city input, missing API key, unknown cities, invalid/inactive keys, request limits, and other API errors
- Uses `SwingWorker` so the GUI remains responsive during the network request

## Notes
- The JSON JAR is not bundled in this project; download it from the official repository.
- Keep your API key private. Do not commit it to source control.
- Requires internet access and a valid OpenWeatherMap API key.
