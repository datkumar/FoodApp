# 🍽️ Zwiggy

A food app proof-of-concept built with recommended modern Android development practices such as Kotlin, Jetpack Compose, MVVM with
repositories, Room DB, Coroutines and Flows

## 🌟 Key Features

* **Secure Authentication Flow:** Client-side form validation, secure session management using **DataStore Preferences**, and
  automated mock auth via custom OkHttp Interceptors.
* **Dynamic Location & Geocoding:** Location permission handling with fallback coordinates (Pune), integrated with a bottom sheet
  location picker powered by the Geocoding API.
* **Swiggy & PokeAPI Integration:** Dynamic restaurant listing with category chips, menu filtering (Veg/Non-Veg), search, and an
  infinite-scrolling Pokémon fallback explorer powered by Paging 3.
* **Offline-First Cart (Room SQLite):** Persistent local cart management using **Jetpack Room** with custom DAOs for relational
  restaurant and dish item storage.
* **Immersive UI & Animations:** Built entirely with **Jetpack Compose**, featuring shared-element transitions, custom skeletons,
  loading spinners, and connectivity status banners.
* **Profile Management:** Local user profile editing with state persistence and image path caching.

---

## 📱 Screenshots & Demos

- Login & Auth
- Home & Restaurant Listing
- Cart & Room DB
- Shared-Element Transition

