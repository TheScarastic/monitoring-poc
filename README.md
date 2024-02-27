# Activity Monitoring App Readme

## Cyberdive Interview Project

### Overview

The Activity Monitoring App is designed to serve as both a system and user application, providing comprehensive logging of various device parameters and user interactions. It utilizes a foreground service for real-time monitoring, ensuring a reliable and user-friendly experience. The app captures a wide range of information, including sensor data, location changes, battery status, network transport, app usage, accessibility actions, and website traffic.

### Features

#### 1. Sensor Logging
- **Permissions Required: None**
  - The app accesses sensor data, including accelerometer, gyroscope, light, orientation, magnetic field, and proximity, without requiring any additional permissions.

#### 2. Location Monitoring
- **Permissions Required:**
  - `ACCESS_COARSE_LOCATION`
  - `ACCESS_FINE_LOCATION`
  - `ACCESS_BACKGROUND_LOCATION`
- Enables the app to accurately monitor location changes using various providers and ensures proper functionality. Additionally, the app identifies if the provided location is fake or mocked.

#### 3. Battery Status
- **Permissions Required: None**
  - Retrieves battery status information without needing any extra permissions.

#### 4. Network Transport
- **Permissions Required:**
  - `ACCESS_COARSE_LOCATION`
  - `ACCESS_FINE_LOCATION`
  - `READ_PHONE_STATE`
- Accesses location and phone state information to provide detailed network transport metrics, including download and upload bandwidth, SIM carriers, WiFi name, SIM MCC, and MNC.

#### 5. App Usage Tracking
- **Permissions Required:**
  - `PACKAGE_USAGE_STATS`
- Utilizes the `PACKAGE_USAGE_STATS` permission to track app usage, with stats automatically updating every hour. Logs information about how much time each app has been used since the device was first booted.

#### 6. Accessibility Actions
- **Permissions Required:**
  - Manual initiation of a service from `Settings > Accessibility > Monitor Actions > Enable Use Monitor Actions`
  - A dialog will appear; click "Allow" to grant necessary permissions.

#### 7. Website Traffic Monitor
- **Permissions Required: None (at runtime)**
  - No additional runtime permissions needed for website traffic monitoring. Specific system configurations, such as sepolicy and a patch in android_packages_modules_DnsResolver, are required for this feature.

### Device Identification
- The main activity of the app displays the device name and Android ID, which could be utilized as identifiers on websites to cross-reference and check the data.

### Note on Foreground Service and WorkManager
The app employs a foreground service for real-time monitoring, as this approach ensures reliability and a better user experience. When transitioning to a system app, the same service can seamlessly convert to a background service, maintaining consistency in functionality.

### Installation and Configuration

#### For User App

1. **Download Android Studio:**
   - Ensure that you have Android Studio installed on your development machine. If not, download and install it from the official [Android Studio website](https://developer.android.com/studio).

2. **Build and Install the App:**
   - Clone the repository to your local machine.
   - Open the project in Android Studio.
   - Build the app using Android Studio's build tools.
   - Install the built APK on your device.

3. **App Initialization:**
   - Open the installed app on your device.
   - Click on "Start Service" to initiate the monitoring service.
   - Grant all necessary permissions requested by the app for proper functionality.

4. **Accessibility Service:**
   - Manually start the accessibility service by navigating to `Settings > Accessibility > Monitor Actions > Enable Use Monitor Actions`.
   - A dialog will appear; click "Allow" to grant necessary permissions.

#### For System App

1. **Replace the App in vendor_extra Repository:**
   - Clone the repository containing the vendor_extra source code for your device.
   - Replace the existing Activity Monitoring App with the built system app in the vendor_extra repository.

2. **Apply DNS Patch:**
   - Locate and apply the specified DNS patch in the `android_packages_modules_DnsResolver` module to enable website traffic monitoring.

3. **Build for the Device:**
   - Build the modified vendor_extra repository for your specific device using the appropriate build tools.
   - Install the system app on the device through the device-specific installation process.

### Note
- Ensure that you carefully follow the steps for either user or system app installation based on your specific use case.
- For system app installation, familiarity with building and modifying system-level components is essential.
- The accessibility service must be manually initiated for both user and system app installations to enable complete functionality.
