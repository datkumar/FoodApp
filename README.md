# 🍽️ Zwiggy

A robust proof-of-concept for a food app built with modern Android development practices such as Jetpack Compose, Kotlin,
Coroutines and Flows, MVVM pattern with repositories as well as local database for persistence

## Demos

https://github.com/user-attachments/assets/82366563-17ef-4408-bbb9-a79424cff5d4


https://github.com/user-attachments/assets/45846660-af01-4b90-a7ea-86618e7df0db


https://github.com/user-attachments/assets/49fcf68d-a208-43f9-bd0d-c5c853f14727


https://github.com/user-attachments/assets/d184c503-be92-4933-a874-8ca8ba37c5db


https://github.com/user-attachments/assets/3d1d75f9-37f0-4090-9dfb-7177b73b3bc6


https://github.com/user-attachments/assets/592ea1cc-4cbb-4cb8-a9f0-5206973ff88f


https://github.com/user-attachments/assets/c99892f3-32cd-4ee5-8613-85f4d9bfd005


## 🌟 Key Features

- **Authentication Flow**: Client-side input validation on Login screen, session data stored in **SharedPreferences DataStore**
  and checked to decide auth state. The login API call is mocked via an OkHttp interceptor. Logout from Settings tab clears stored
  session data
- **Location detection and manual search**: Permission handling and detection of user's location coordinates (falls back to Pune
  if denied) as well as manual location selection via entering place name (searched via geocoding API)
- **Swiggy API integration**: Live restaurant list and menu details for user's location fetched from Swiggy's public API
  endpoints. Restaurant listing page and menu details page populated accordingly
- **Pagination with Pokemons**: Since Swiggy backend's AWS WAF blocks external requests for next page fetches, I have integrated
  pagination and **infinite-scroll** with the PokeApi having similar listing of pokemons on cards, clicking which takes you into
  details page of that pokemon
- **Cart with Room DB**: Persistent cart management across app restarts via Room ORM for SQLite database along with custom DAOs
  for handling relational mappings.
- **Editable User Profile**: Email and role is fixed but user can change username, phone number which are also input-validated.
  Can also update profile picture by either picking from Gallery or capturing photo via Camera with permission-handling in place
- **Reactive UI**: ViewModels for state management (decoupling business logic from the UI) with state-driven rendering,
  shared-element transitions between restaurant listing cards and detail pages for seamless visual continuity, Flows for reactive
  UI updates and a connectivity banner for network updates.
- **Testing**: Unit tests and Instrumentation tests with JUnit for input validators, ViewModels, Cart-DB operations

## 🛠️ Building APK from source

If you wish to build the APK from this source code, make sure to create a `secrets.properties` file at the root of project and add
your API key from [Geoapify](https://www.geoapify.com/). See [`secrets.properties.sample`](./secrets.properties.sample) for the key names
