 CuraSync - Complete Project Workflow Explanation

**CuraSync** is a **Healthcare Management System** built in Java that helps patients find the nearest hospital, route to it efficiently, manage appointments, and handle emergency cases. It combines a desktop GUI with web-based mapping.

**PART 1: USER JOURNEY (Beginning to End)**
**Step 1: Application Launch**
```
Main.java → LoginFrame appears
```
- User opens the application
- **Main.java** loads and displays **LoginFrame** (login screen)
- Sets Modern Look & Feel using FlatLaf for better UI
- No actual authentication; just a gateway to dashboard

**Step 2: User Authentication**
- User enters credentials (username/password)
- Clicks "Login" → Main dashboard opens
- Routes to **MainFrame** (dashboard)

**Step 3: Dashboard Navigation**
Once logged in, user sees navigation menu with options:
1. ** Doctors** → Manage doctor information
2. **Hospitals** → View hospital details
3. **Patients** → Manage patient profiles
4. **Appointments** → Schedule/view appointments
5. **Emergency Cases** → Handle emergencies

**PART 2: KEY WORKFLOWS & ALGORITHMS**

 **Workflow A: Finding Nearest Hospital**

 **Use Case**: Patient needs to find the nearest hospital

**Flow:**
```
User selects Hospital → HospitalRouter.findNearestHospital()
                     ↓
                 Calculate distance
                     ↓
                 Find minimum distance hospital
                     ↓
                 Display result on map
```

**Algorithm Used: DIJKSTRA'S SHORTEST PATH ALGORITHM**

**Where**: `HospitalRouter.java`
*What it does**:Finds the shortest distance from patient location to all hospitals
- Uses a **graph** where nodes = cities (Ballari, Hospet, Mysore, Bengaluru)
- Edges = distances between cities (e.g., Ballari to Hospet = 60 km)
 **How it works**:
1. Start from patient's current node
2. Mark all distances as infinite except starting point (0)
3. For each unvisited node:
   - Find node with minimum distance
   - Update distances to its neighbors
   - Mark node as visited
4. Return shortest distances to all hospitals

**Time Complexity**: **O(E log V)** with priority queue
- E = edges, V = nodes
- Very efficient for routing
 **Code Example**:
```java
int[] dist = HospitalRouter.dijkstra(patientNode);  // Get distances to all hospitals
Hospital nearest = HospitalRouter.findNearestHospital(patientNode, hospitals)
**Workflow B: Searching Doctors by Specialization**
 **Use Case**: Find all cardiologists or pediatricians

**Flow:**
```
User searches specialization → SearchEngine.searchDoctorsBySpecialization()
                           ↓
                       DFS Search
                           ↓
                    Find matching doctors
                           ↓
                    Display results sorted
```

**Algorithm 1: DFS (Depth-First Search)**
**Where**: `SearchEngine.java`

 **How it works**:
- Recursively searches through all doctors
- Compares specialization with search term (case-insensitive)
- Returns all matching doctors

**Time Complexity**: **O(n)** - Linear search
 **Code Example**:
```java
List<Doctor> cardiologists = SearchEngine.searchDoctorsBySpecialization(
    allDoctors, "cardiology"
);
```

---

**Algorithm 2: MERGE SORT (Sorting Hospitals by Distance)**

**Where**: `SearchEngine.java`

**How it works**:
1. Divide list into two halves recursively
2. Sort each half
3. Merge sorted halves back together
4. Compare distances and maintain sorted order

**Time Complexity**: **O(n log n)** - Very efficient sorting

**Why use it**: Perfect for sorting hospital search results by distance

**Code Example**:
```java
List<SearchEngine.HospitalDistance> distances = getHospitalDistances(hospitals);
SearchEngine.mergeSortHospitalsByDistance(distances, 0, distances.size()-1);
```

---

 **Workflow C: Emergency Case Management**

 **Use Case**: Handle multiple emergency cases with priority

**Flow:**
```
Emergency calls come in → EmergencyQueue.addCase()
                      ↓
                 Priority assigned
                      ↓
                 Queued by severity
                      ↓
                 Hospital processes in order
```

**Algorithm: MAX-HEAP PRIORITY QUEUE**
 **Where**: `EmergencyQueue.java`
**How it works**:
- Uses **PriorityQueue** (max-heap by default in EmergencyCase)
- **High severity** cases get processed first
- Cases auto-sorted by severity level

 **Time Complexity**: 
- Add: **O(log n)**
- Remove: **O(log n)**

 **Why use it**: Ensures critical patients are treated first
**Code Example**:
```java
EmergencyQueue queue = new EmergencyQueue();
queue.addCase(new EmergencyCase(...severity=5...));  // High priority
queue.addCase(new EmergencyCase(...severity=2...));  // Low priority

EmergencyCase nextCase = queue.getNextCase();  // Always gets highest severity
```

---

**Workflow D: Appointment Scheduling**

**Use Case**: Find available time slots and schedule appointments optimally

**Flow:**
```
User requests time slot → Scheduler.greedySchedule()
                      ↓
              Sort slots by time
                      ↓
              Select non-overlapping slots
                      ↓
              Maximize appointments
```

**Algorithm: GREEDY ALGORITHM (Activity Selection)**

**Where**: `Scheduler.java`
 **How it works**:
1. Sort all requested time slots by start time
2. Pick the first slot
3. For each remaining slot:
   - If it doesn't overlap with last picked, add it
   - Skip if it overlaps
4. Return maximum non-overlapping slots

 **Time Complexity**: **O(n log n)** - Dominated by sorting

 **Why use it**: Maximizes number of appointments in a day

**Code Example**:
```java
List<TimeSlot> requestedSlots = Arrays.asList(
    new TimeSlot("09:00", 9),
    new TimeSlot("10:00", 10),
    new TimeSlot("10:30", 10)  // Overlaps with 10:00
);

List<TimeSlot> scheduled = Scheduler.greedySchedule(requestedSlots);
// Result: "09:00" and "11:00" (skipped "10:30" as it overlaps

**Workflow E: Mapping & Visualization**
**Use Case**: Display patient location and route on interactive map

**Flow:**
```
Patient location → HospitalMapViewer generates JSON
                        ↓
                   Patient marker
                   Hospital markers
                   Route polyline
                        ↓
                   HTML map template receives data
                        ↓
                   Browser displays Leaflet.js map
```

**Technologies**: Leaflet.js + HTML5 Canvas

 **Where**: `HospitalMapViewer.java` + `map_template.html`

 **Components**:
- **Patient Marker**: Blue circle at patient coordinates
- **Hospital Markers**: Red circles at hospital locations
- **Route Polyline**: Line showing Dijkstra's shortest path
- **Sidebar**: Shows hospital details and route steps



**PART 3: DATA FLOW (Back to Front)**

```
┌─────────────────────────────────────────────────────┐
│                  UI LAYER (Swing)                    │
│  LoginFrame → MainFrame → Various Panels             │
│  (DoctorPanel, PatientPanel, EmergencyPanel, etc)    │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│             ALGORITHM LAYER                          │
│  HospitalRouter (Dijkstra)                          │
│  SearchEngine (DFS + MergeSort)                     │
│  EmergencyQueue (Priority Queue)                    │
│  Scheduler (Greedy)                                 │
│  HospitalMapViewer (Visualization)                  │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│              MODEL LAYER                             │
│  Hospital, Patient, Doctor, Appointment             │
│  EmergencyCase, Area                                 │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────┐
│              DAO LAYER (Database)                    │
│  HospitalDAO, PatientDAO, DoctorDAO                 │
│  AppointmentDAO, EmergencyCaseDAO, AreaDAO          │
│              ↓                                        │
│          DBConnection                               │
│              ↓                                        │
│          SQL Database                               │
└──────────────────────────────────────────────────────┘
**PART 4: ALGORITHMS 
SUMMARY TABLE**

| Workflow | Algorithm | Complexity | Purpose | File |
|----------|-----------|-----------|---------|------|
| Find Nearest Hospital | Dijkstra's Shortest Path | O(E log V) | Shortest route to hospital | HospitalRouter.java |
| Search Doctors | Depth-First Search (DFS) | O(n) | Find doctors by specialization | SearchEngine.java |
| Sort Results | Merge Sort | O(n log n) | Sort hospitals by distance | SearchEngine.java |
| Emergency Queue | Max-Heap Priority Queue | O(log n) insert/remove | Priority-based case handling | EmergencyQueue.java |
| Schedule Appointments | Greedy Algorithm | O(n log n) | Maximize non-overlapping slots | Scheduler.java |
| Map Display | Graph Visualization | - | Show location & route on map | HospitalMapViewer.java |

---

**PART 5: COMPLETE USER EXAMPLE**

**Scenario**: Emergency patient needs hospital

```
1. User launches app → LoginFrame
2. Logs in → MainFrame
3. Clicks "Emergency" → EmergencyPanel
4. Reports emergency (severe, chest pain)
5. EmergencyCase added to EmergencyQueue with priority=5
6. System calls HospitalRouter.findNearestHospital(patientNode)
7. Dijkstra runs: calculates shortest distance to all hospitals
8. Nearest hospital found: Bengaluru Medical Center (60 km)
9. HospitalMapViewer generates route visualization
10. map.html opens in browser showing:
    - Patient location (red marker)
    - Hospital location (blue marker)
    - Shortest path (polyline)
11. ER staff receives notification in EmergencyPanel
12. Case processed from queue (highest priority first)
```

---

 **PART 6: KEY FILES & THEIR ROLES**

**Core Logic**:
- `Main.java` - Entry point
- `HospitalRouter.java` - Routing algorithm (Dijkstra)
- `SearchEngine.java` - Search & sorting (DFS, MergeSort)
- `EmergencyQueue.java` - Priority queue for cases
- `Scheduler.java` - Appointment scheduling (Greedy)

**UI Components**:
- `LoginFrame.java` - Login screen
- `MainFrame.java` - Dashboard
- `HospitalMapViewer.java` - Map generation
- `*Panel.java` - Various feature panels

**Data Access**:
- `*DAO.java` - Database operations
- `DBConnection.java` - Connection management

**Models**:
- `Hospital.java`, `Patient.java`, `Doctor.java` - Data structures

**Algorithms used**:
1.  **Dijkstra** - Fastest route to hospital
2. **DFS** - Find doctors by specialization
3. **Merge Sort** - Sort results efficiently
4.  **Priority Queue** - Handle emergencies by severity
5. **Greedy Algorithm** - Maximize appointment slots
6. **Graph + Visualization** - Interactive mapping

**Why these algorithms?**
- **Dijkstra**: Proven shortest-path solution
- **Priority Queue**: Real-world emergency triage
- **Greedy**: Optimal appointment scheduling
- **Merge Sort**: O(n log n) sorting efficiency.
