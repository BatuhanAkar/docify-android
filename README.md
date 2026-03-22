<div align="center">

<table align="center" border="0">
  <tr>
    <td align="center" valign="middle">
      <img src="assets/logo.png" width="70" />
    </td>
    <td align="center" valign="middle">
      <h1>Docify: AI PDF - Bilgi Sohbeti</h1>
    </td>
  </tr>
</table>

**Docify**, belgelerinizi sadece yönetmekle kalmayıp onlarla "konuşmanızı" sağlayan, gücünü tamamen cihaz içi (on-device) yapay zekadan alan yeni nesil bir üretkenlik aracıdır.

<a href="https://play.google.com/store/apps/details?id=com.batuscode.docify&hl=tr">
  <img alt="Get it on Google Play" src="assets/GetItOnGooglePlay_Badge_Web_color_English.png" width="220"/>
</a>

<br>

![Uygulama Ekran Görüntüleri](assets/docify_preview.png)

</div>

---

## 🧠 On-Device AI: Gemma & Privacy-First

Docify, kullanıcı verilerinin gizliliğini en üst seviyede tutmak için bulut tabanlı AI yerine tamamen cihaz üzerinde çalışan modelleri kullanır.

* **Model:** `gemma3_1b_it_int4.task` (MediaPipe LLM Inference).
* **Gizlilik:** Tüm "Belge ile Sohbet" ve "Özetleme" işlemleri yerel cihazda döner; dokümanlarınız asla bir sunucuya yüklenmez.
* **Dynamic Delivery:** Uygulama boyutunu optimize etmek için AI modeli, **Android Asset Pack (Fast-Follow)** yöntemiyle kurulum sonrası arka planda dinamik olarak indirilir.

---

## 🛠️ Teknik Mühendislik (Native & JNI)

Uygulamanın çekirdeği, yüksek performanslı yerel kütüphaneler ile modern Android mimarisinin hibrit bir birleşimidir.

### 📄 Native PDF Engine (Pdfium & JNI Interop)
PDF görüntüleme ve manipülasyon işlemleri için Google'ın açık kaynaklı **Pdfium** kütüphanesi kullanılmıştır.
* **Native Interop:** C++ katmanındaki Pdfium metodları, **JNI (Java Native Interface)** üzerinden Kotlin tarafına bağlanarak `libs/obj` seviyesinde entegre edilmiştir.
* **Annotation & Drawing:** Kullanıcıların PDF üzerinde çizim yapması, metinleri vurgulaması (highlight) ve notlar alması için özel bir yerel render katmanı kurgulanmıştır.
* **Memory Management:** Büyük dosyalarda dahi akıcı performans için yerel bellek (native memory) yönetimi optimize edilmiştir.

### 🏗️ Mimari Yaklaşım
* **Modularization:** AI Inference ve PDF Processing katmanları birbirinden bağımsız modüller olarak tasarlanmıştır.
* **Clean Architecture & MVVM:** Kodun sürdürülebilirliği ve test edilebilirliği için endüstri standartları uygulanmıştır.

---

## ✨ Temel Özellikler

* **💬 AI Bilgi Sohbeti:** Doküman içeriğini anlayan yapay zeka ile PDF üzerinden gerçek zamanlı soru-cevap.
* **📝 Akıllı Özetleme:** Uzun akademik makaleleri, sözleşmeleri veya raporları saniyeler içinde özetleme.
* **🎨 PDF Düzenleme & İşaretleme:** Sayfalar üzerine **serbest çizim**, metin **vurgulama** ve not ekleme araçları.
* **📂 PDF Araç Seti:**
    * **Oluşturma:** Metin ve görsellerden anında PDF üretme.
    * **Birleştirme & Bölme:** Birden fazla dosyayı yönetme.
    * **Klasör Yönetimi:** Dosyaları gruplandırma ve çalışma alanlarını düzenleme.
* **📷 Belge Tarayıcı:** Fiziksel kağıtları tarayıp OCR ile dijital PDF'e dönüştürme.

---

## 🚀 Teknolojik Yığın (Tech Stack)

| Alan | Kullanılan Teknolojiler |
| :--- | :--- |
| **LLM Engine** | **Google Gemma** (Mediapipe LLM Inference) |
| **PDF Engine** | **Pdfium** (C++ / JNI Native Interop) |
| **UI Framework** | **Jetpack Compose** & Material Design 3 |
| **Dependency Injection** | **Hilt-Dagger** |
| **Backend** | **Firebase** (Auth, Firestore, Storage) |
| **Model Delivery** | **Play Asset Delivery** (Fast-Follow) |

---

## 🌍 Veri Kaynağı & Teşekkür
Docify, PDF işleme yeteneklerini **Pdfium** açık kaynak projesine borçludur. Gelişmiş AI özellikleri için ise **Google Gemma** modelleri kullanılmıştır.

---

## 📈 Monitoring & Monetization
* **Firebase Crashlytics:** Yerel (Native) ve Kotlin katmanındaki hataların takibi.
* **Firebase Analytics:** Anonim kullanım istatistikleri ve AI performans ölçümleri.

## 🚀 Gelecek Planları & İyileştirmeler (Roadmap)
Projenin sürdürülebilirliği ve ölçeklenebilirliği için aşağıdaki geliştirmeler planlanmaktadır:
- [ ] **Mimari Refactoring:** Mevcut paket yapısının "Feature-based" (özellik tabanlı) mimariye taşınarak modülerliğin artırılması.
- [ ] **Hybrid Cloud Integration:** Cihaz üzerindeki AI model yerine, daha karmaşık analizler için bir **RESTful API** katmanı eklenerek işlemlerin bulut (Cloud) tabanlı opsiyonlarla desteklenmesi.
- [ ] **Unit & UI Testing:** İş mantığı ve JNI köprüleri için kapsamlı test senaryolarının (JUnit, Espresso) yazılması.
- [ ] **Gelişmiş OCR:** Taranan belgelerde daha yüksek doğruluk için bulut tabanlı gelişmiş OCR motorlarının entegrasyonu.
