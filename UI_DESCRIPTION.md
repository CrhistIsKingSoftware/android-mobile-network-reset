# App UI Description

## Main Screen Layout

The Mobile Network Reset app features a clean, Material Design interface with the following components:

### Header Section
- **Title**: "Mobile Network Reset" (centered, 24sp, bold)
- **Description**: Explanation text about the app's purpose (centered, 16sp)

### Information Card
A Material Card containing:
- **"How it works:"** header (18sp, bold)
- Four numbered steps explaining the reset process:
  1. Disables automatic network selection
  2. Scans for available networks
  3. Selects the first available network
  4. Re-enables automatic network selection

### Status Section
- **Status Text**: Dynamic text showing current operation status
  - Idle: "Press the button below to reset your mobile network connection"
  - Resetting: "Resetting network connection..."
  - Success: "Network reset completed successfully!"
  - Error: Various error messages based on the issue

### Action Buttons
1. **"RESET NETWORK" Button** (Primary Material Button)
   - Full-width
   - Blue background (#2196F3)
   - White text (18sp)
   - Initiates the network reset process

2. **"OPEN NETWORK SETTINGS" Button** (Outlined Material Button)
   - Full-width
   - Outlined style
   - Secondary action
   - Opens Android network settings directly

### Footer
- **Note**: Small italic text explaining permission requirements
  - Mentions that the app may need system-level permissions
  - Explains limitations on regular apps

### Color Scheme
- **Primary**: Blue (#2196F3)
- **Primary Dark**: Dark Blue (#1976D2)
- **Accent**: Green (#4CAF50)
- **Background**: White (Material Light Theme)
- **Text**: Black/Gray (Material default text colors)

### Layout Type
- ScrollView container (for small screens)
- ConstraintLayout for positioning
- Responsive padding (16dp)
- Material Design 3 components

## User Flow

1. User opens the app
2. Reads the instructions on how the process works
3. Taps "RESET NETWORK" button
4. Sees a toast message with step-by-step instructions
5. App automatically opens Network Operator Settings
6. User follows the guided steps in settings:
   - Disable "Select automatically"
   - Select a network
   - Go back
   - Re-enable "Select automatically"
7. User returns to the app
8. Status updates to show completion

## Alternative Flow

1. User taps "OPEN NETWORK SETTINGS"
2. App opens network settings directly
3. User performs manual network operations as needed

## Accessibility Features
- All text is readable (minimum 12sp)
- Sufficient color contrast
- Touch targets are appropriately sized
- Support for screen readers (via ContentDescription)
- Scrollable content for small screens

## App Icon
- Simple network signal icon design
- Blue circular background
- White foreground graphics
- Adaptive icon support for Android 8.0+

## Toast Messages
- Guidance toast with 4-step process when reset is initiated
- Error toasts for specific failure cases
- Duration: LONG for important messages, SHORT for confirmations
