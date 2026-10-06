# Ders Takip ve Akıllı Çalışma Programı Sistemi 🎓📱

İngilizce öğretmenleri ve öğrencileri için YouTube oynatma listelerine (playlist) dayalı, akıllı ve modern bir **Ders Takip ve Akıllı Çalışma Programı** Android uygulaması.

---

## 🏗️ Mimari ve Teknoloji Yığını

Uygulama, Google tarafından önerilen **Modern Android Architecture (Clean Architecture + MVVM)** prensiplerine tam uyumlu olarak geliştirilmiştir:

- **Dil:** Kotlin (100% Idiomatic Kotlin, Coroutines & Flow)
- **Arayüz (UI):** Jetpack Compose, Material Design 3
- **Mimari:** MVVM (Model-View-ViewModel) + Clean Architecture (Data, Domain, UI katmanları)
- **Yerel Depolama:** AndroidX Room Database 2.6+ (Entities, DAOs, Relations, TypeConverters)
- **Bağımlılık Enjeksiyonu (DI):** Dagger Hilt 2.51+
- **Ağ/API:** Retrofit 2.11+ & OkHttp (YouTube Data API v3)
- **Asenkron Yapı:** Kotlin Coroutines & Reactive StateFlow
- **Test:** JUnit 4 & Kotlinx Coroutines Test
- **Minimum SDK:** 24 (Android 7.0+) | **Hedef SDK:** 34 (Android 14)

---

## 🌟 Temel Özellikler

### 1. YouTube Oynatma Listesi Entegrasyonu ve Veri Çekme
- Kullanıcı geçerli bir YouTube playlist URL'si veya Playlist ID'si girebilir.
- `YouTubeUrlParser` ile farklı YouTube link formatları (web, mobil, paylaşılan linkler) otomatik ayıklanır.
- `YouTubeApiService` üzerinden oynatma listesi detayları, video listesi ve video süreleri (ISO 8601: `PT15M33S` vb.) çekilir.
- `IsoDurationParser` süreleri saniye ve Türkçe kullanıcı dostu biçime ("1 sa 25 dk", "45 dk") çevirir.
- `TopicExtractor` video başlıklarındaki pedagojik anahtar kelimeleri analiz ederek ilgili konuyu otomatik sınıflandırır (örneğin *Gramer*, *Kelime Bilgisi*, *Dinleme*, *Konuşma & Telaffuz*, *Okuma & Çeviri*, *Sınav Hazırlığı*).
- API anahtarının girilmediği veya internetin olmadığı durumlarda kullanıcıyı engellememek adına zengin çevrimdışı örnek ders kütüphanesi devreye girer.

### 2. Akıllı Programlama Motoru (Smart Scheduling Engine)
- **Günlük Kapasite:** Kullanıcı günde kaç dakika (örneğin 120 dakika) çalışabileceğini belirler.
- **Haftalık Çalışma Günleri:** Pazartesi'den Pazar'a kadar hangi günlerde çalışılacağı seçilebilir (örneğin sadece Pazartesi, Çarşamba, Cuma).
- **Video Bölünmeme Kuralı:** Tek bir video, süresi tüm günlük kapasiteyi tek başına aşmadığı sürece asla günlere bölünmez. Günlük kapasite dolduğunda sonraki video bir sonraki çalışma gününe ertelenir.
- **Konu Bütünlüğü:** Derslerin pedagojik ve mantıksal sırası korunur.

### 3. Kontrol Paneli ve İlerleme Takibi
- **Bugün Ekranı (Daily View):**
  - Bugünün çalışma hedefi, tamamlanan ders sayısı, kalan süre.
  - Video bazlı anlık "Tamamlandı" onay kutuları (Checkbox).
  - Günlük hedefler bittiğinde tebrik bildirimi.
- **Program Ekranı (Schedule View):**
  - Kapasite kaydırıcısı ve haftalık gün seçici.
  - Tüm günlerin zaman çizelgesi kartları (açılır/kapanır video listeleriyle).
- **İlerleme & İstatistikler Ekranı (Progress View):**
  - Genel kurs tamamlama yüzdesi ve saat bazlı sayaçlar.
  - Konu/Ders bazlı ilerleme çubukları (örneğin *"Gramer: 20 videodan 12'si tamamlandı, 5 saat kaldı"*).

---

## 📁 Dizin Yapısı

```
app/src/main/java/com/derstakip/app/
├── DersTakipApplication.kt          # @HiltAndroidApp uygulama sınıfı
├── MainActivity.kt                  # Edge-to-edge Compose Activity
│
├── data/                            # Veri Katmanı
│   ├── local/                       # Room Veritabanı
│   │   ├── AppDatabase.kt
│   │   ├── converter/Converters.kt
│   │   ├── dao/                     # PlaylistDao, VideoDao, DailyScheduleDao, StudySettingsDao
│   │   ├── entity/                  # PlaylistEntity, VideoEntity, DailyScheduleEntity...
│   │   └── relation/                # DailyScheduleWithVideos, TopicStats
│   ├── remote/                      # YouTube Data API
│   │   ├── YouTubeApiService.kt
│   │   └── dto/YouTubeDtos.kt
│   └── repository/                  # PlaylistRepositoryImpl, ScheduleRepositoryImpl
│
├── domain/                          # İş Mantığı & Domain Katmanı
│   ├── engine/                      # SmartSchedulingEngine (Akıllı Program Algoritması)
│   ├── model/                       # Playlist, Video, DailySchedulePlan, TopicProgress...
│   └── repository/                  # PlaylistRepository, ScheduleRepository (Arayüzler)
│
├── di/                              # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── ui/                              # Sunum (UI) Katmanı
│   ├── components/                  # VideoItemCard, TopicProgressBar, DailyTimelineCard, BottomBar
│   ├── navigation/                  # Screen, AppNavigation
│   ├── screens/
│   │   ├── today/                   # TodayScreen, TodayViewModel
│   │   ├── playlist/                # PlaylistImportScreen, PlaylistViewModel
│   │   ├── schedule/                # ScheduleSettingsScreen, ScheduleViewModel
│   │   └── progress/                # ProgressScreen, ProgressViewModel
│   └── theme/                       # Color, Theme, Type (Material 3)
│
└── util/                            # Yardımcı Araçlar
    ├── IsoDurationParser.kt         # ISO 8601 Süre Çözümleyici
    ├── TopicExtractor.kt            # Başlıktan Konu Çıkarıcı
    └── YouTubeUrlParser.kt          # YouTube Link Çözümleyici
```

---

## 🧪 Birim Testleri (Unit Tests)

`app/src/test/java/com/derstakip/app/` altında kapsamlı test senaryoları mevcuttur:
1. `SmartSchedulingEngineTest`: Günlük kapasite kontrolü, video bölünmeme kuralı, aktif gün filtreleme, büyük video davranışı.
2. `IsoDurationParserTest`: ISO 8601 (`PT#H#M#S`) ayrıştırma ve Türkçe biçimlendirme doğrulaması.
3. `YouTubeUrlParserTest`: Farklı YouTube playlist URL biçimlerinin doğru ayrıştırılması.
4. `TopicExtractorTest`: İngilizce eğitim içerikleri için konu sınıflandırma kurallarının testi.

---

## 🚀 Projeyi Çalıştırma

1. **Android Studio**'yu açın.
2. **File -> Open** menüsünden `Ders Takip Uygulaması` klasörünü seçin.
3. Android Studio Gradle senkronizasyonunu tamamlayacaktır.
4. Gerçek YouTube API anahtarınızı kullanmak isterseniz `app/build.gradle.kts` içerisindeki `YOUTUBE_API_KEY` alanına kendi anahtarınızı yazabilirsiniz. (Varsayılan olarak yerleşik örnek İngilizce kurs kütüphanesi anında çalışacak şekilde yapılandırılmıştır).
5. Emülatör veya fiziksel Android cihaz seçerek **Run 'app'** butonuna basın.
