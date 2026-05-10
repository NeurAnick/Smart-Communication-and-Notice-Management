# NoticeSync — Smart Communication & Notice Management

NoticeSync is a modern Android application designed for educational institutions to streamline communication between administrators, teachers, and students. It features real-time notice broadcasting, semester-specific group chats, and a robust profile management system.

## 🚀 Key Features

### 📢 Notice Management
- **Real-time Updates**: Notices appear instantly across all devices using Firestore snapshot listeners.
- **Image Support**: Admins can attach images to notices for better visual communication.
- **Seen Tracking**: See exactly how many students have viewed each notice in real-time.
- **Priority Alerts**: Urgent notices trigger high-priority system notifications.

### 💬 Semester-Specific Chat
- **Privacy & Focus**: Students are automatically joined to a group chat room for their specific semester.
- **Real-time Messaging**: Built on Firebase Realtime Database for zero-latency communication.
- **Restricted Access**: Students can only view and participate in their own semester's chat, while admins/teachers can manage all rooms.

### 👤 Profile & Role Management
- **Self-Service Editing**: Users can update their Name, Phone, Bio, Department, and Semester/ID without admin intervention.
- **Role-Based Access**: Specialized dashboards for Admins, Teachers, Class Representatives (CR), and Students.
- **Real-time Sync**: Profile changes are instantly reflected in chats and notices.

### 🔔 Smart Notifications
- **In-App & System Alerts**: Receive real-time notifications for new notices and group messages.
- **Semester Filtering**: You only get notified for messages that matter to your specific semester.

## 🛠️ Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern Declarative UI)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection**: Dagger Hilt
- **Backend**: Firebase (Auth, Firestore, Realtime Database, Storage, Messaging)
- **Image Loading**: Coil
- **Navigation**: Compose Navigation

## 📸 Screenshots
*(Coming soon — Check the app in action!)*

## 📥 Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/NeurAnick/Smart-Communication-and-Notice-Management.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Add your `google-services.json` file to the `app/` directory.
4. Build and run on an emulator or physical device.

## 🤝 Contributing
Contributions are welcome! Feel free to open an issue or submit a pull request.

## 📄 License
This project is licensed under the MIT License.
