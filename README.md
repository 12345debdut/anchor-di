# 🍉 KMP Wizard Template — AGP 9.0.0 Ready

A clean **Kotlin Multiplatform (KMP)** starter generated with the **Kotlin Multiplatform Wizard** and then updated to work smoothly with **Android Gradle Plugin (AGP) 9.0.0**.

Here is [a Medium article](https://blog.watermelonkode.com/how-to-update-the-android-gradle-plugin-to-version-9-0-0-in-a-kotlin-multiplatform-kmp-agp-9-6a2261a6a8fd) to explain all changes. Moreover, this repository is published as a **GitHub Template** — click **“Use this template”** to create your own project in seconds.

<img width="600" alt="watermelonKode KMP" src="https://github.com/user-attachments/assets/0fd9986c-b656-4e69-a075-e9643d6151e0" />

<img width="600" alt="Screenshot 2026-01-23 at 19 55 24" src="https://github.com/user-attachments/assets/e0af20d2-a736-45ca-baa9-f2164c5e1b24" />

---

## Why this template?

If you’ve ever generated a project from the KMP Wizard and then hit friction upgrading to **AGP 9**, this template is meant to be the “already-migrated” starting point.

It follows the modern recommendation of having:
- a **shared KMP module** (for Compose Multiplatform shared UI + shared logic)
- a dedicated **Android app module** (Android entry point)
- a dedicated **iOS app** (Xcode entry point)

(Those modules are already present in this repo.) 

---

## What’s inside?

- **Kotlin Multiplatform** project targeting **Android + iOS**
- **Compose Multiplatform** shared UI in `composeApp`
- Android entry point in `androidApp`
- iOS entry point in `iosApp`
- Gradle Kotlin DSL + version-catalog style dependency management (typical wizard output)
- Updated build setup aligned with AGP 9 / “built-in Kotlin” direction

> Repo description: “This repository is created by KMP Wizard but updated to use AGP 9.”  

---

## Project structure

```
.
├── androidApp/     # Android application module (entry point)
├── composeApp/     # Shared KMP module (Compose Multiplatform UI + shared code)
└── iosApp/         # iOS application (Xcode project)
```

---

## Requirements

- **Android Studio** recent enough to support modern KMP + AGP 9 tooling
- **Xcode** (to run the iOS app)
- A JDK compatible with your Android Studio / Gradle / AGP setup

See:
- Kotlin Multiplatform Wizard: https://kmp.jetbrains.com/
- Android KMP Gradle plugin guidance: https://developer.android.com/kotlin/multiplatform/plugin
- AGP 9 release notes: https://developer.android.com/build/releases/agp-9-0-0-release-notes

---

## Getting started

### 1) Create a new repo from this template
1. Open this GitHub repository.
2. Click **Use this template → Create a new repository**
3. Clone your new repository locally.

### 2) Run the Android app
Open the project in Android Studio and run the **androidApp** configuration.

If you prefer the terminal, the wizard-style README in this repo includes:

```bash
./gradlew :composeApp:assembleDebug
```

> Depending on your Android module setup, you may also want to use tasks like:
> `./gradlew :androidApp:assembleDebug` or `./gradlew :androidApp:installDebug`.
> Run `./gradlew tasks` to see what’s available in your environment.

### 3) Run the iOS app
Open `iosApp/` in **Xcode** and press **Run**.

---

## Notes about AGP 9 + KMP (the “why it works” part)

AGP 9 introduces / solidifies changes that matter for KMP projects, including:
- **Built-in Kotlin** support on the Android side (reduces reliance on legacy Kotlin Android plugin wiring)
- A dedicated **Android KMP library plugin** (`com.android.kotlin.multiplatform.library`) recommended for KMP library modules
- Guidance to keep `com.android.application` **in a separate module** from the shared KMP module

This template is structured with those realities in mind.

---

## watermelonKode🍉 on social media

We are new but try to help Kotlin Multiplatform community. Feel free to follow us on socail media for more KMP + CMP contents, apps, GitHub repositories, Medium articles, news and more: https://linktr.ee/watermelonKode

---

## License

Apache-2.0 (see `LICENSE`).
