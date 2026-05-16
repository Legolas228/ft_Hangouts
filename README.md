# ft_hangouts

A simple Android SMS app to manage contacts and chat threads. This project is designed for the 42 ft_hangouts assignment.

## Features

- Create, edit, and delete contacts
- Home screen with a contact summary list
- Send and receive SMS messages
- Conversation history showing sender and receiver
- Header color selection
- Two languages (English and Spanish)
- Toast showing the last time the app went to background
- Portrait and landscape support
- App icon uses the 42 logo

## Tech Stack

- Android (Kotlin)
- RecyclerView
- SQLite (local storage)
- Material Components

## Project Structure

- `app/src/main/java/com/pborrull/ft_hangouts/` - app source code
- `app/src/main/res/` - layouts, drawables, strings
- `app/src/main/AndroidManifest.xml` - app manifest

## Requirements

- Android Studio (latest stable recommended)
- Android SDK (API 24+)
- Gradle (uses the wrapper)

## Build and Run

1) Open the project in Android Studio
2) Let Gradle sync
3) Run the `app` configuration on an emulator or device

To build from terminal:

```zsh
./gradlew :app:assembleDebug
```

## SMS Testing on Emulator

The Android Emulator console can inject SMS messages.

1) Start an emulator
2) Find its console port (usually `5554` or `5556`)
3) Authenticate and send messages:

```zsh
TOKEN=$(cat ~/.emulator_console_auth_token)
(echo "auth $TOKEN"; echo "sms send 1234567890 Hello"; echo "sms send 9876543210 Another message"; sleep 1) | telnet localhost 5556
```

Tip: you can keep a continuous session open and type commands interactively:

```zsh
TOKEN=$(cat ~/.emulator_console_auth_token)
(echo "auth $TOKEN"; cat) | telnet localhost 5556
```

Then type lines like:

```
sms send 1234567890 Hello from telnet
```

## Notes

- If you change the locale inside the app, Android Studio may recreate activities.
- SMS receiving requires emulator/device permissions.

## License

This project is for educational purposes as part of the 42 curriculum.

