# Network Scanner App

A Java-based Windows application to detect devices on the local network and display their IP addresses in a grid layout.

## Features
- Detects the local network base using `ipconfig`.
- Scans all IPs in the subnet using `ping`.
- Displays up to 256 detected IP addresses in a 12x21 grid.

## Requirements
- Java 8 or higher.
- Windows OS (uses `ipconfig` and `ping`).

## Usage
1. Compile and run `NetworkScannerApp.java`.
2. Click the **Detect** button to start scanning the local network.
