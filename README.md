# Café Manager — Android V1

A clean, offline-first Android café business manager.

## GitHub-ready project

This archive is prepared to be uploaded directly to a new GitHub repository. **Upload the contents of this folder, not the ZIP file itself.**

The project is configured for AGP 9.3.1's built-in Kotlin support and uses the Android legacy KAPT compatibility plugin for Room.

## Important

- The Room database starts empty on first launch.
- No sample café transactions are included.
- No ads, subscriptions, or revenue model are included in V1.
- No Firebase/cloud credentials are included.
- The app is offline-first and stores its current data locally.

## Current build stack

- Android Gradle Plugin 9.3.1
- Gradle 9.7.0
- Kotlin 2.4.20 / AGP built-in Kotlin
- Jetpack Compose BOM 2026.09.00
- Material 3
- Room 2.8.5
- Lifecycle 2.11.0
- Java 17

## Build APK on GitHub Actions

1. Create a new GitHub repository.
2. Upload the **contents** of this archive to the repository root.
3. Commit everything to `main`.
4. Open **Actions**.
5. Select **Build Android APK**.
6. Run the workflow if it has not started automatically.
7. After a successful run, open the run and download the `cafe-manager-debug-apk` artifact.
8. Extract the artifact and install the debug APK on an Android phone.

The workflow installs Gradle 9.7.0 itself, so a Gradle wrapper is not required for GitHub Actions.

## Local Android Studio

If you later want to work locally:
- Install Android Studio.
- Install Android SDK Platform 36 and Build Tools 36.
- Use JDK 17.
- Open this repository as an Android project.

## V1 data

The current Android V1 includes:
- Home dashboard
- Sales with Cash + UPI split validation
- Expenses with simple required fields
- Inventory and low-stock status
- Analytics date filters
- Delete confirmation
- Local Room database

The following larger features remain future implementation work: edit transactions, supplier management UI, backup/restore, CSV/Excel export, cloud sync, daily open/closed status, and full purchase-to-inventory transaction history.

## Play Store preparation

Before release, the project still needs release signing, final branding/icon work, privacy policy, data-safety declaration, store screenshots, final versioning, release AAB generation, and Play Console testing/release steps.
