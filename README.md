<div align="center">

<div align="center">

⚠️ **PROJECT STATUS: DEVELOPMENT & REFACTORING IN PROGRESS** ⚠️

> **Docify is currently undergoing a major architectural overhaul and is not accepting new client requests or integrations at this time.**

We are actively working on executing our planned roadmap, including architectural refactoring and building the hybrid cloud infrastructure. During this development phase, public access to the AI chat backend is temporarily disabled. Thank you for your patience and support while we build a more scalable platform!

</div>

<table align="center" border="0">
  <tr>
    <td align="center" valign="middle">
      <img src="assets/logo.png" width="70" />
    </td>
    <td align="center" valign="middle">
      <h1>Docify: AI PDF & Knowledge Chat</h1>
    </td>
  </tr>
</table>

**Docify** is a next-generation productivity tool that goes beyond managing your documents—it allows you to "converse" with them, powered entirely by on-device AI.

<a href="https://play.google.com/store/apps/details?id=com.batuscode.docify&hl=en">
  <img alt="Get it on Google Play" src="assets/GetItOnGooglePlay_Badge_Web_color_English.png" width="220"/>
</a>

<br>

![App Screenshots](assets/docify_preview.png)

</div>

---

## 🧠 On-Device AI: Gemma & Privacy-First

Docify prioritizes user data privacy by utilizing completely on-device models rather than cloud-based AI.

* **Model:** `gemma3_1b_it_int4.task` (MediaPipe LLM Inference).
* **Privacy:** All "Chat with Document" and "Summarization" processes execute locally; your documents are never uploaded to an external server.
* **Dynamic Delivery:** To optimize the application size, the AI model is downloaded in the background post-installation via **Android Asset Pack (Fast-Follow)**.

---

## 🛠️ Technical Engineering (Native & JNI)

The core of the application is a hybrid combination of high-performance native libraries and modern Android architecture.

### 📄 Native PDF Engine (Pdfium & JNI Interop)
Google’s open-source **Pdfium** library is used for PDF rendering and manipulation.
* **Native Interop:** Methods from the Pdfium C++ layer are bound to Kotlin via **JNI (Java Native Interface)** at the `libs/obj` level.
* **Annotation & Drawing:** A custom native rendering layer enables users to draw on PDFs, highlight text, and take notes.
* **Memory Management:** Native memory management is highly optimized to ensure fluid performance even with large files.

### 🏗️ Architecture Approach
* **Modularization:** The AI Inference and PDF Processing layers are decoupled into independent modules.
* **Clean Architecture & MVVM:** Industry standards are applied to ensure sustainability and testability.

---

## ✨ Core Features

* **💬 AI Knowledge Chat:** Real-time Q&A over PDF content using an AI model that understands document context.
* **📝 Smart Summarization:** Summarize long academic papers, contracts, or reports in seconds.
* **🎨 PDF Editing & Annotation:** Tools for **free-hand drawing**, **highlighting**, and adding notes on document pages.
* **📂 PDF Toolkit:**
    * **Creation:** Generate PDFs instantly from text and images.
    * **Merge & Split:** Manage multi-file workflows.
    * **Folder Management:** Group files and organize workspaces.
* **📷 Document Scanner:** Scan physical documents and convert them into digital PDFs using OCR.

---

## 🚀 Tech Stack

| Layer | Technologies Used |
| :--- | :--- |
| **LLM Engine** | **Google Gemma** (MediaPipe LLM Inference) |
| **PDF Engine** | **Pdfium** (C++ / JNI Native Interop) |
| **UI Framework** | **Jetpack Compose** & Material Design 3 |
| **Dependency Injection** | **Hilt-Dagger** |
| **Backend** | **Firebase** (Auth, Firestore, Storage) |
| **Model Delivery** | **Play Asset Delivery** (Fast-Follow) |

---

## 🌍 Data Source & Acknowledgments
Docify owes its PDF processing capabilities to the open-source **Pdfium** project. The advanced AI features are powered by **Google Gemma** models.

---

## 📈 Monitoring & Monetization

* **Firebase Crashlytics:** Real-time tracking of errors across both Native and Kotlin layers.
* **Firebase Analytics:** Anonymous usage statistics and AI performance measurements.

---

## 🚀 Roadmap & Planned Improvements
To ensure the sustainability and scalability of the project, the following developments are planned:
- [ ] **Architecture Refactoring:** Transition the current package structure to a "Feature-based" architecture to increase modularity.
- [ ] **Hybrid Cloud Integration:** Add a **RESTful API** layer to support more complex processing using cloud-based alternatives when necessary.
- [ ] **Unit & UI Testing:** Write comprehensive test scenarios (JUnit, Espresso) for business logic and JNI bridges.
- [ ] **Advanced OCR:** Integrate cloud-based advanced OCR engines for higher accuracy on scanned documents.
