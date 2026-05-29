# 🧪 Horizon Cinemas Booking System (HCBS) - Test Case Specification

This document details the software testing strategy and provides the test case suite for the Horizon Cinemas Booking System (HCBS). It covers all critical Use Cases, Role-Based Access Control (RBAC) security boundaries, form input validations, and error-injection edge cases.

---

## 📊 Table 1: Comprehensive System Test Cases

| Test Case ID | Test Case Name | Purpose | Condition / Input | Expected Result | Actual Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_001** | **Customer Secure Login** | Verify a registered customer can successfully authenticate. | Username: `guo`<br>Password: `123456` | User logs in successfully and is redirected to the movie listings homepage. | **PASS** - Successfully authenticated and redirected. |
| **TC_002** | **Failed Login Authentication** | Verify system rejects invalid login attempts gracefully. | Username: `guo`<br>Password: `wrong_pass` | Red warning error message displayed: "Invalid username or password". | **PASS** - Access denied; clear warning message displayed. |
| **TC_003** | **Security Access Boundary (RBAC)** | Verify Customers are blocked from accessing Admin pages. | Login as customer (`guo`), then manually visit `/manager` or `/admin` in the browser URL. | Vaadin Security redirects to access denied / login page, blocking entry. | **PASS** - Route access intercepted; access denied. |
| **TC_004** | **Add City Validation** | Verify a Manager can successfully add a valid city name. | Role: `MANAGER`<br>City Name: `Manchester` | City added successfully. Grid table updates in real-time. | **PASS** - City saved to database and rendered instantly in the grid. |
| **TC_005** | **Empty City Validation** | Attempt to add a city with an empty input to trigger validation. | Role: `MANAGER`<br>City Name: `[Empty String]` | Add City button action ignored or triggers a warning. No blank record added. | **PASS** - Blocked. Form ignores empty submissions. |
| **TC_006** | **Cinema Screen Auto-generation** | Verify creating a cinema automatically initializes 6 screens. | Role: `MANAGER`<br>Cinema: `Manchester Cinema 1`<br>City: `Manchester` | Cinema is saved, and exactly 6 screens (Screen 1 to 6) are auto-generated. | **PASS** - Screen repository holds 6 screen records with capacities (60-110). |
| **TC_007** | **Dynamic ComboBox Refresh** | Verify city dropdown list updates dynamically. | Add a new city `Leeds`, then open the "City" dropdown in the Cinema Form. | `Leeds` is instantly selectable in the dropdown without refreshing the browser. | **PASS** - Droplist synchronized instantly using the class-level field refresh. |
| **TC_008** | **Admin Add Film Validation** | Verify Admin can add a movie with all details populated. | Role: `ADMIN`<br>Title: `Avatar`<br>Duration: `192`<br>Age: `12A` | Movie is saved in H2 database and rendered in the movies card list. | **PASS** - Movie successfully persisted and shown on the dashboard. |
| **TC_009** | **Invalid Showing Price Validation** | Deliberately enter negative prices for showing to test validation. | Role: `ADMIN`<br>Price Lower: `-12.50`<br>Price Gallery: `-15.00` | System blocks submission, displaying validation boundary warnings. | **PASS** - Form blocks negative prices; inputs are validated. |
| **TC_010** | **Schedule Showings Successful** | Verify Admin can schedule a movie screening. | Movie: `火遮眼`<br>Screen: `Screen 1`<br>Date: `2026-06-11`<br>Time: `14:00` | Showing created. Remaining seats initialized to screen capacity. | **PASS** - Showing successfully scheduled and selectable by users. |
| **TC_011** | **Dynamic Seat Selection Loading** | Verify seating layout renders dynamically when showing is picked. | Customer selects: Film `火遮眼` ➜ `London Cinema 1` ➜ `14:00` showing. | Seat map corresponding to the screen size (e.g. 8 rows x 7 cols) loads. | **PASS** - Seating grid loaded dynamically with exact capacity layout. |
| **TC_012** | **Seat Double-Booking Prevention** | Verify a reserved seat is locked and cannot be re-selected. | Customer A buys seat `A1`. Customer B opens same showing. | Seat `A1` is rendered in grey (Reserved status) and is not clickable. | **PASS** - Double-booking prevented. Grey status prevents interaction. |
| **TC_013** | **Zero Seat Booking Constraint** | Verify user cannot book tickets without selecting seats. | Customer selects a showing, leaves selected seats at 0. | "Book Now" button remains disabled (greyed out). | **PASS** - Button disabled until at least one seat is clicked. |
| **TC_014** | **Booking Checkout Flow** | Verify successful seat booking and ticket cost calculation. | Customer selects `A2`, `A3` (Gallery seat @ £10 each). Clicks "Book Now". | Booking successful. Total cost shows £20. Reference code generated. | **PASS** - Reference generated (`BK-xxxx`). Database records successfully. |
| **TC_015** | **Staff Cancellation & Seat Release** | Verify canceling a booking releases the seats back to available. | Staff cancels Booking `BK-xxxx`. | Booking status set to "Cancelled". Showing's remaining seats count increases by 2. | **PASS** - Seats released. Showing's remaining seats updated in H2. |

---

## 📈 Testing Strategy & Boundary Validation

### 1. Robust Input Validation
Our validation testing strategy focuses on input boundary checks (e.g., **TC_005**, **TC_009**). Field bounds (such as duration, price values, and blank values) are dynamically validated before being committed to H2 database records. 

### 2. Role-Based Access Control (RBAC) Security Verification
Spring Security filters intercept all REST and UI route calls. A customer (`ROLE_CUSTOMER`) attempting to access administrative endpoints `/admin` or `/manager` is intercepted and gracefully redirected back to a safe route, ensuring robust role enforcement (**TC_003**).

### 3. Data Integrity & Concurrency
By applying `@ManyToOne` constraints and locking seat selections, the system prevents common transaction errors such as double-booking the same seat under identical showings (**TC_012**).
