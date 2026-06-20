# CuraSync - Run & Map Report

## I. Introduction
This report documents building and running the CuraSync desktop app and verifying the interactive Leaflet map that shows the patient location and hospital details.

## II. Objectives
- Build the Java project and capture build/run output.
- Generate and open `map.html` showing patient marker, route, and hospital details.
- Collect artifacts for troubleshooting and sharing: `run_output.txt`, `map.html`, `map_screenshot.png`.

## III. Design (Algorithm, Technique, Time Complexity)
- Nearest hospital: Uses geographical distance (Haversine / Euclidean) computation in `HospitalRouter`.
- Route: Dijkstra algorithm implemented in `HospitalRouter` to compute shortest path between nodes (hospitals). Time complexity: O(E log V) with a priority queue.
- Frontend: `HospitalMapViewer` serializes hospital and route data to JSON and injects into `map_template.html` to render with Leaflet.js.

## IV. Problem Statement
Given a patient location (latitude, longitude), determine the nearest available hospital in the district, compute the shortest route using hospital network edges, and provide a web map that:
- Plots the patient marker and hospital markers
- Shows the computed Dijkstra route as a polyline
- Displays a sidebar with the recommended hospital and route steps

## V. Results
Actions taken (fill after running):
- OS: Windows (fill version)
- Java: output of `java -version` and `javac -version` (paste here)
- Commands executed:
  - `dir /s /b src\main\java\*.java > sources.txt`
  - `javac -d bin -cp "lib\*" @sources.txt`
  - `java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" com.curasync.main.Main`
- Build status: (Success / Failed) — paste summary and any errors.
- Interactive map: (Opened in browser / Not opened)
  - Observations: patient marker visible at (lat, lng), recommended hospital card present, route polyline displayed, clicking hospital markers shows details.

Attach files:
- `run_output.txt` — Console output captured during build/run
- `map.html` — Generated map page (copy from workspace root)
- `map_screenshot.png` — Screenshot of the interactive map

## VI. Conclusion
Summarize whether the map and routing behaved as expected, list any issues found (exceptions, missing hospitals, incorrect coordinates), and recommended next steps (fix coordinates, improve UI, persist maps, etc.).

## VII. References
- Source files: `src/main/java/com/curasync/ui/HospitalMapViewer.java`
- Map template: `map_template.html`
- Map server: `src/tools/MapServer.java`
- Java docs and Leaflet docs

---

### Quick commands to collect artifacts
In Command Prompt (recommended):

```
REM Capture build+run output
run.bat > run_output.txt 2>&1
REM Copy the generated map
copy map.html map.html
```

In PowerShell (alternative):

```powershell
Start-Process -FilePath .\run.bat -NoNewWindow -Wait -RedirectStandardOutput run_output.txt -RedirectStandardError run_output.txt
Copy-Item -Path map.html -Destination map.html -Force
```

After running, take a browser screenshot and save as `map_screenshot.png`.


Fill the placeholders and attach the three files when you paste this report to ChatGPT or to a ticket.
