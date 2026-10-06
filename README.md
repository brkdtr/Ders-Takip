# Ders Takip ve Akıllı Çalışma Programı (PWA) 🎓📱⚡

İngilizce öğretmenleri ve öğrencileri için YouTube oynatma listelerine dayalı, akıllı ve modern bir **Ders Takip ve Akıllı Çalışma Programı Progressive Web App (PWA)**.

Herhangi bir derleyiciye, Android Studio kurulumuna veya APK dosyasına gerek olmadan; doğrudan tarayıcıda çalışır, iPhone (Safari) ve Android (Chrome) cihazlarda **"Ana Ekrana Ekle"** ile yerel bir mobil uygulama gibi kurulabilir.

---

## 🌟 Öne Çıkan Özellikler

1. **Çevrimdışı ve Yerel Depolama (LocalStorage):**
   - Tüm oynatma listeleri, video dersler, tamamlanma durumları ve oluşturulan akıllı takvim tarayıcının `localStorage` alanında kalıcı olarak saklanır. Sayfa yenilense de veriler kaybolmaz.

2. **Akıllı Programlama Motoru (Smart Scheduling Engine):**
   - Kullanıcının belirlediği **günlük çalışma kapasitesine** (örneğin 120 dk) ve **haftalık çalışma günlerine** (örneğin Pazartesi, Çarşamba, Cuma) göre videoları günlere böler.
   - **Video Bölünmeme Kuralı:** Tek bir video (süresi günlük kapasiteyi aşmadıkça) asla günlere bölünmez. Konu bütünlüğü korunarak sepetlere dağıtılır.

3. **4 Ana Ekran / Sekme:**
   - **Bugün (Today):** Günlük hedef özeti, anlık onay kutuları (checkbox), kalan süre sayacı ve hedefler bittiğinde kutlama konfetisi 🎉.
   - **Program (Schedule):** Kapasite kaydırıcısı, haftalık gün seçici ve açılır/kapanır akordeon gün kartları.
   - **İlerleme (Progress):** Genel kurs tamamlama yüzdesi ve konu bazlı ilerleme çubukları (*Gramer, Kelime Bilgisi, Dinleme, Konuşma, Okuma, Sınav*).
   - **Liste (Playlist):** YouTube linki yapıştırma, arama/filtreleme ve tek tıkla örnek İngilizce kursu yükleme.

4. **PWA & Mobil Uygulama Deneyimi:**
   - `manifest.json` ve `sw.js` (Service Worker) ile çevrimdışı çalışma ve anında açılma desteği.
   - iOS çentik ve alt çubuk uyumlu safe-area desteği (`viewport-fit=cover`).

---

## 📁 PWA Proje Dosyaları

- [`index.html`](file:///Users/burak/Desktop/Ders%20Takip%20Uygulamas%C4%B1/index.html): Uygulama iskeleti, 4 sekme, modal pencereleri ve Tailwind CSS tasarımı.
- [`style.css`](file:///Users/burak/Desktop/Ders%20Takip%20Uygulamas%C4%B1/style.css): Mobil uygulama animasyonları, özel kaydırma çubukları ve safe-area stilleri.
- [`app.js`](file:///Users/burak/Desktop/Ders%20Takip%20Uygulamas%C4%B1/app.js): LocalStorage servisi, YouTube API/veri çekicisi, Akıllı Zamanlama Motoru ve reaktif UI kontrolörü.
- [`manifest.json`](file:///Users/burak/Desktop/Ders%20Takip%20Uygulamas%C4%B1/manifest.json): PWA web uygulama manifestosu.
- [`sw.js`](file:///Users/burak/Desktop/Ders%20Takip%20Uygulamas%C4%B1/sw.js): Çevrimdışı önbellekleme Service Worker'ı.
- `icons/`: PWA uygulama simgeleri (`icon.svg`, `icon-192.png`, `icon-512.png`).

---

## 🚀 GitHub Pages Üzerinde Yayına Alma (1 Dakikada)

Bu projeyi doğrudan internette canlıya almak için:

1. **GitHub'da Yeni Bir Depo (Repository) Oluşturun:**
   - GitHub hesabınıza girip `ders-takip` adında boş bir repo açın.
2. **Kodu GitHub'a Gönderin:**
   ```bash
   git remote add origin https://github.com/KULLANICI_ADINIZ/ders-takip.git
   git branch -M main
   git push -u origin main
   ```
3. **GitHub Pages'i Açın:**
   - GitHub reponuzda **Settings (Ayarlar)** ➡️ **Pages** sekmesine gidin.
   - **Branch** kısmından `main` ve `/ (root)` seçip **Save** deyin.
4. **Hazır!**
   - 1-2 dakika içinde `https://KULLANICI_ADINIZ.github.io/ders-takip/` adresinde uygulamanız canlıya geçer!
   - Telefonunuzdan bu adresi açıp **"Ana Ekrana Ekle"** diyerek gerçek bir mobil uygulama gibi kullanabilirsiniz.
