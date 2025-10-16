# Mobile Network Reset - Architecture & Flow

## Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Mobile Network Reset App                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
         ┌────────────────────────────────────────┐
         │         MainActivity.kt                 │
         │  ┌──────────────────────────────────┐  │
         │  │  UI Components:                  │  │
         │  │  - Reset Button                  │  │
         │  │  - Settings Button               │  │
         │  │  - Status Text                   │  │
         │  │  - Instructions Card             │  │
         │  └──────────────────────────────────┘  │
         │                                        │
         │  ┌──────────────────────────────────┐  │
         │  │  Business Logic:                 │  │
         │  │  - performNetworkReset()         │  │
         │  │  - openNetworkOperatorSettings() │  │
         │  │  - openNetworkSettings()         │  │
         │  │  - updateStatus()                │  │
         │  └──────────────────────────────────┘  │
         └────────────────┬───────────────────────┘
                          │
              ┌───────────┴───────────┐
              │                       │
              ▼                       ▼
    ┌──────────────────┐    ┌──────────────────┐
    │ TelephonyManager │    │ Settings Intents │
    │  (Check Device)  │    │  (Open Settings) │
    └──────────────────┘    └──────────────────┘
                                      │
                                      ▼
                          ┌──────────────────────┐
                          │   Android Settings   │
                          │  Network Operators   │
                          └──────────────────────┘
```

## User Flow Diagram

```
┌─────────────┐
│  App Start  │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────────────┐
│  MainActivity UI Displayed               │
│  ┌────────────────────────────────────┐  │
│  │ • Title and Description            │  │
│  │ • How It Works Card                │  │
│  │ • Status: "Press button to reset"  │  │
│  │ • [RESET NETWORK] Button           │  │
│  │ • [OPEN SETTINGS] Button           │  │
│  └────────────────────────────────────┘  │
└──────────────┬────────────────────────────┘
               │
     ┌─────────┴──────────┐
     │                    │
     ▼                    ▼
┌─────────────┐    ┌──────────────┐
│ User Taps   │    │  User Taps   │
│ Reset Button│    │Settings Btn  │
└──────┬──────┘    └──────┬───────┘
       │                  │
       ▼                  │
┌──────────────┐          │
│ Check Device │          │
│ Has Telephony│          │
└──────┬───────┘          │
       │                  │
       ▼                  │
┌──────────────┐          │
│ Show Toast   │          │
│ Instructions │          │
└──────┬───────┘          │
       │                  │
       ▼                  │
┌──────────────┐          │
│ Update Status│          │
│  "Resetting" │          │
└──────┬───────┘          │
       │                  │
       └──────────┬───────┘
                  │
                  ▼
       ┌────────────────────┐
       │ Open Android        │
       │ Network Operator    │
       │ Settings Screen     │
       └──────┬─────────────┘
              │
              ▼
    ┌──────────────────────┐
    │ User Follows Steps:  │
    │ 1. Disable Auto      │
    │ 2. Select Network    │
    │ 3. Go Back           │
    │ 4. Enable Auto       │
    └──────┬───────────────┘
           │
           ▼
    ┌──────────────┐
    │ Network Reset│
    │  Complete!   │
    └──────────────┘
```

## Component Interaction Flow

```
User Action              MainActivity           Android System
    │                        │                       │
    │   Tap Reset Button     │                       │
    ├───────────────────────>│                       │
    │                        │                       │
    │                        │  Check Telephony      │
    │                        ├──────────────────────>│
    │                        │<──────────────────────┤
    │                        │  Device OK            │
    │                        │                       │
    │  <Toast Message>       │                       │
    │<───────────────────────┤                       │
    │  "Follow steps 1-4"    │                       │
    │                        │                       │
    │                        │  Launch Settings      │
    │                        ├──────────────────────>│
    │                        │                       │
    │                                                │
    │<───────────────────────────────────────────────┤
    │           Settings Screen Opens                │
    │                                                │
    │   [User performs manual steps]                 │
    │                                                │
    │   Return to App        │                       │
    ├───────────────────────>│                       │
    │                        │                       │
    │  Status Update         │                       │
    │<───────────────────────┤                       │
    │  "Ready for next reset"│                       │
    │                        │                       │
```

## File Structure & Dependencies

```
Project Root
│
├── app/
│   ├── src/main/
│   │   ├── java/com/christistking/mobilenetworkreset/
│   │   │   └── MainActivity.kt ──────────┐
│   │   │                                  │
│   │   ├── res/                           │
│   │   │   ├── layout/                    │
│   │   │   │   └── activity_main.xml <────┤ Uses
│   │   │   ├── values/                    │
│   │   │   │   ├── strings.xml <──────────┤
│   │   │   │   ├── colors.xml <───────────┤
│   │   │   │   └── themes.xml <───────────┤
│   │   │   └── drawable/                  │
│   │   │       └── ic_launcher_*.xml <────┘
│   │   │
│   │   └── AndroidManifest.xml ──> Declares MainActivity
│   │
│   └── build.gradle ──> App dependencies
│
├── build.gradle ──> Project config
├── settings.gradle ──> Module config
└── .github/workflows/
    ├── android-ci.yml ──> Builds on push
    └── android-release.yml ──> Creates releases
```

## CI/CD Pipeline Flow

```
┌──────────────┐
│ Git Push or  │
│ Pull Request │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│ GitHub Actions   │
│ Workflow Trigger │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ Setup JDK 17     │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ Checkout Code    │
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│ Build with       │
│ Gradle           │
└──────┬───────────┘
       │
       ├──────────────┐
       │              │
       ▼              ▼
┌──────────┐   ┌──────────┐
│Run Lint  │   │Run Tests │
└──────┬───┘   └──────┬───┘
       │              │
       └──────┬───────┘
              │
              ▼
       ┌──────────────┐
       │ Build APK    │
       └──────┬───────┘
              │
              ▼
       ┌──────────────┐
       │ Upload       │
       │ Artifacts    │
       └──────────────┘
```

## Release Process Flow

```
Developer                  GitHub                 Users
    │                        │                      │
    │  Create Tag (v1.0.0)   │                      │
    ├───────────────────────>│                      │
    │                        │                      │
    │                        │  Trigger Release     │
    │                        │  Workflow            │
    │                        ├──────────────┐       │
    │                        │              │       │
    │                        │  Build APK   │       │
    │                        │<─────────────┘       │
    │                        │                      │
    │                        │  Create Release      │
    │                        ├──────────────┐       │
    │                        │              │       │
    │                        │  Attach APK  │       │
    │                        │<─────────────┘       │
    │                        │                      │
    │  Release Created       │                      │
    │<───────────────────────┤                      │
    │                        │                      │
    │                        │  Download APK        │
    │                        │<─────────────────────┤
    │                        │                      │
    │                        │  APK File            │
    │                        ├─────────────────────>│
    │                        │                      │
    │                        │                   Install
    │                        │                   on Device
```

## Data Flow

```
┌─────────────────┐
│  User Input     │
│  (Button Tap)   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  Event Handler          │
│  (OnClickListener)      │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Business Logic         │
│  (performNetworkReset)  │
└────────┬────────────────┘
         │
         ├─────────────────┐
         │                 │
         ▼                 ▼
┌──────────────┐   ┌───────────────┐
│ Update UI    │   │ Check System  │
│ (Status)     │   │ (Telephony)   │
└──────────────┘   └───────┬───────┘
                           │
                           ▼
                   ┌───────────────┐
                   │ Launch Intent │
                   │ (Settings)    │
                   └───────────────┘
```

## Permission Flow

```
App Installation
    │
    ▼
┌─────────────────────────┐
│ System checks           │
│ AndroidManifest.xml     │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Granted Automatically:  │
│ - ACCESS_NETWORK_STATE  │
│ - CHANGE_NETWORK_STATE  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ NOT Granted (System):   │
│ - MODIFY_PHONE_STATE    │
│   (Requires system app) │
└─────────────────────────┘
         │
         ▼
┌─────────────────────────┐
│ App uses workaround:    │
│ Opens Settings for user │
└─────────────────────────┘
```

## Notes

- Solid lines (──) represent direct dependencies
- Arrows (►/▼) show flow direction
- Boxes represent components/steps
- This architecture provides maximum compatibility while working within Android security constraints
