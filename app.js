/**
 * Ders Takip - Progressive Web App (PWA)
 * JavaScript Core Application Architecture
 * Features:
 *  - 7 Independent Subjects: MEB-AGS (6 subjects) + YDS (English)
 *  - AMOLED Pure Black (#000000) & Light Themes
 *  - Phone & Tablet Layout Modes
 *  - Smart Bin-Packing Scheduling Algorithm (No video split rule)
 *  - YouTube Data API v3 + Enhanced Multi-CORS Fallbacks
 *  - Quick Custom Playlist Creator (Instant offline / custom course generation)
 *  - Full LocalStorage persistence
 */

// ============================================================================
// 1. SUBJECT DEFINITIONS (MEB-AGS + YDS MÜFREDATI)
// ============================================================================

const SUBJECTS = [
  {
    id: 'egitim-bilimleri',
    name: 'Eğitim Bilimleri & MEB Sistemi',
    shortName: 'Eğitim Bilimleri',
    icon: 'fa-chalkboard-user',
    color: '#6366f1',
    bgColor: 'rgba(99, 102, 241, 0.15)',
    borderColor: 'rgba(99, 102, 241, 0.4)',
    badge: 'AGS (%37.5)',
    desc: 'Öğrenme & Gelişim Psikolojisi, ÖYT, Ölçme, Rehberlik ve MEB Teşkilat Yapısı'
  },
  {
    id: 'turkce',
    name: 'Sözel Yetenek (Türkçe)',
    shortName: 'Türkçe',
    icon: 'fa-book-open',
    color: '#ec4899',
    bgColor: 'rgba(236, 72, 153, 0.15)',
    borderColor: 'rgba(236, 72, 153, 0.4)',
    badge: 'AGS (%18.75)',
    desc: 'Sözcükte/Cümlede Anlam, Paragraf Analizi, Dil Bilgisi ve Sözel Mantık'
  },
  {
    id: 'matematik',
    name: 'Sayısal Yetenek (Matematik)',
    shortName: 'Matematik',
    icon: 'fa-calculator',
    color: '#3b82f6',
    bgColor: 'rgba(59, 130, 246, 0.15)',
    borderColor: 'rgba(59, 130, 246, 0.4)',
    badge: 'AGS (%18.75)',
    desc: 'Temel Matematik, Problemler, Sayısal Mantık, Tablo ve Grafik Yorumlama'
  },
  {
    id: 'mevzuat',
    name: 'Eğitim Mevzuatı & Hukuk',
    shortName: 'Mevzuat',
    icon: 'fa-scale-balanced',
    color: '#f59e0b',
    bgColor: 'rgba(245, 158, 11, 0.15)',
    borderColor: 'rgba(245, 158, 11, 0.4)',
    badge: 'AGS (%10)',
    desc: 'Öğretmenlik Mesleği Kanunu (ÖMK), Anayasa, 1739 Sayılı Kanun, 657 DMK'
  },
  {
    id: 'tarih',
    name: 'Tarih',
    shortName: 'Tarih',
    icon: 'fa-landmark',
    color: '#8b5cf6',
    bgColor: 'rgba(139, 92, 246, 0.15)',
    borderColor: 'rgba(139, 92, 246, 0.4)',
    badge: 'AGS (%7.5)',
    desc: 'İlk Türk Devletleri, Türk-İslam Tarihi, Osmanlı Tarihi ve İnkılap Tarihi'
  },
  {
    id: 'cografya',
    name: 'Türkiye Coğrafyası',
    shortName: 'Coğrafya',
    icon: 'fa-earth-europe',
    color: '#10b981',
    bgColor: 'rgba(16, 185, 129, 0.15)',
    borderColor: 'rgba(16, 185, 129, 0.4)',
    badge: 'AGS (%7.5)',
    desc: 'Türkiye Fiziki Coğrafyası, İklim, Nüfus, Yerleşme, Tarım ve Madenler'
  },
  {
    id: 'yds',
    name: 'YDS (İngilizce)',
    shortName: 'YDS İngilizce',
    icon: 'fa-language',
    color: '#06b6d4',
    bgColor: 'rgba(6, 182, 212, 0.15)',
    borderColor: 'rgba(6, 182, 212, 0.4)',
    badge: 'YDS (Tek Ders)',
    desc: 'Gramer, Kelime Bilgisi, Cümle Tamamlama, Çeviri, Okuma ve Deneme Taktikleri'
  }
];


// ============================================================================
// 2. THEME & LAYOUT MANAGERS
// ============================================================================

const ThemeManager = {
  KEY: 'dt_theme',

  init() {
    const current = this.getTheme();
    this.applyTheme(current);
  },

  getTheme() {
    return localStorage.getItem(this.KEY) || 'amoled';
  },

  setTheme(theme) {
    localStorage.setItem(this.KEY, theme);
    this.applyTheme(theme);
  },

  applyTheme(theme) {
    const html = document.documentElement;
    html.classList.remove('theme-amoled', 'theme-light', 'dark');

    const metaTheme = document.querySelector('meta[name="theme-color"]');

    if (theme === 'light') {
      html.classList.add('theme-light');
      if (metaTheme) metaTheme.setAttribute('content', '#f8fafc');
    } else {
      // AMOLED Pure Black (#000000)
      html.classList.add('theme-amoled', 'dark');
      if (metaTheme) metaTheme.setAttribute('content', '#000000');
    }

    const toggleBtn = document.getElementById('quickThemeToggleBtn');
    if (toggleBtn) {
      toggleBtn.innerHTML = theme === 'light'
        ? '<i class="fa-solid fa-moon text-slate-700"></i>'
        : '<i class="fa-solid fa-sun text-amber-400"></i>';
      toggleBtn.title = theme === 'light' ? 'AMOLED Saf Siyaha Geç' : 'Gündüz Moduna Geç';
    }

    document.querySelectorAll('.theme-selector-btn').forEach(btn => {
      const btnTheme = btn.getAttribute('data-theme');
      if (btnTheme === theme) {
        btn.classList.add('ring-2', 'ring-brand-500', 'bg-brand-500/10');
      } else {
        btn.classList.remove('ring-2', 'ring-brand-500', 'bg-brand-500/10');
      }
    });
  },

  toggle() {
    const next = this.getTheme() === 'amoled' ? 'light' : 'amoled';
    this.setTheme(next);
    AppUI.showToast(next === 'amoled' ? 'AMOLED Saf Siyah Açıldı (#000000)' : 'Gündüz Modu Açıldı', 'info');
  }
};

const LayoutManager = {
  KEY: 'dt_layout',

  init() {
    const current = this.getLayout();
    this.applyLayout(current);
  },

  getLayout() {
    const saved = localStorage.getItem(this.KEY);
    if (saved) return saved;
    return window.innerWidth >= 900 ? 'tablet' : 'phone';
  },

  setLayout(layout) {
    localStorage.setItem(this.KEY, layout);
    this.applyLayout(layout);
  },

  applyLayout(layout) {
    const body = document.body;
    body.classList.remove('view-phone', 'view-tablet');

    if (layout === 'tablet') {
      body.classList.add('view-tablet');
    } else {
      body.classList.add('view-phone');
    }

    const toggleBtn = document.getElementById('quickLayoutToggleBtn');
    if (toggleBtn) {
      toggleBtn.innerHTML = layout === 'tablet'
        ? '<i class="fa-solid fa-mobile-screen text-slate-300"></i>'
        : '<i class="fa-solid fa-tablet-screen-button text-brand-400"></i>';
      toggleBtn.title = layout === 'tablet' ? 'Telefon Moduna Geç' : 'Tablet Moduna Geç';
    }

    document.querySelectorAll('.layout-selector-btn').forEach(btn => {
      const btnLayout = btn.getAttribute('data-layout');
      if (btnLayout === layout) {
        btn.classList.add('ring-2', 'ring-brand-500', 'bg-brand-500/10');
      } else {
        btn.classList.remove('ring-2', 'ring-brand-500', 'bg-brand-500/10');
      }
    });
  },

  toggle() {
    const next = this.getLayout() === 'tablet' ? 'phone' : 'tablet';
    this.setLayout(next);
    AppUI.showToast(next === 'tablet' ? 'Tablet Modu (Geniş Panel) Aktif' : 'Telefon Modu (Kompakt) Aktif', 'info');
  }
};


// ============================================================================
// 3. STORAGE SERVICE
// ============================================================================

const StorageService = {
  KEYS: {
    PLAYLISTS: 'dt_playlists',
    VIDEOS: 'dt_videos',
    SCHEDULES: 'dt_schedules',
    SETTINGS: 'dt_settings'
  },

  getSettings() {
    const raw = localStorage.getItem(this.KEYS.SETTINGS);
    if (!raw) {
      return {
        dailyCapacityMinutes: 120,
        selectedDays: [1, 2, 3, 4, 5],
        startDate: SmartSchedulingEngine.formatLocalDate(new Date()),
        activeSubjectFilter: 'all',
        apiKey: ''
      };
    }
    try {
      const parsed = JSON.parse(raw);
      if (!parsed.dailyCapacityMinutes) parsed.dailyCapacityMinutes = 120;
      if (!parsed.selectedDays || parsed.selectedDays.length === 0) parsed.selectedDays = [1, 2, 3, 4, 5];
      if (!parsed.startDate) parsed.startDate = SmartSchedulingEngine.formatLocalDate(new Date());
      if (!parsed.activeSubjectFilter) parsed.activeSubjectFilter = 'all';
      return parsed;
    } catch {
      return {
        dailyCapacityMinutes: 120,
        selectedDays: [1, 2, 3, 4, 5],
        startDate: SmartSchedulingEngine.formatLocalDate(new Date()),
        activeSubjectFilter: 'all',
        apiKey: ''
      };
    }
  },

  saveSettings(settings) {
    localStorage.setItem(this.KEYS.SETTINGS, JSON.stringify(settings));
  },

  getPlaylists() {
    const raw = localStorage.getItem(this.KEYS.PLAYLISTS);
    try {
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  },

  getPlaylistBySubject(subjectId) {
    const playlists = this.getPlaylists();
    return playlists.find(p => p.subjectId === subjectId) || null;
  },

  savePlaylist(playlist) {
    let playlists = this.getPlaylists();
    const existingIndex = playlists.findIndex(p => p.id === playlist.id || (playlist.subjectId && p.subjectId === playlist.subjectId));
    if (existingIndex >= 0) {
      playlists[existingIndex] = { ...playlists[existingIndex], ...playlist };
    } else {
      playlists.push(playlist);
    }
    localStorage.setItem(this.KEYS.PLAYLISTS, JSON.stringify(playlists));
  },

  getVideos(subjectId = null) {
    const raw = localStorage.getItem(this.KEYS.VIDEOS);
    try {
      const all = raw ? JSON.parse(raw) : [];
      if (!subjectId || subjectId === 'all') return all;
      return all.filter(v => v.subjectId === subjectId);
    } catch {
      return [];
    }
  },

  saveVideos(subjectId, videos) {
    let all = this.getVideos();
    all = all.filter(v => v.subjectId !== subjectId);
    all.push(...videos);
    localStorage.setItem(this.KEYS.VIDEOS, JSON.stringify(all));
  },

  toggleVideoCompletion(videoId, isCompleted) {
    const all = this.getVideos();
    const target = all.find(v => v.id === videoId);
    if (target) {
      target.isCompleted = isCompleted;
      target.completedAt = isCompleted ? Date.now() : null;
      localStorage.setItem(this.KEYS.VIDEOS, JSON.stringify(all));
    }
  },

  getSchedules() {
    const raw = localStorage.getItem(this.KEYS.SCHEDULES);
    try {
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  },

  saveSchedules(schedules) {
    localStorage.setItem(this.KEYS.SCHEDULES, JSON.stringify(schedules));
  },

  resetAll() {
    localStorage.removeItem(this.KEYS.PLAYLISTS);
    localStorage.removeItem(this.KEYS.VIDEOS);
    localStorage.removeItem(this.KEYS.SCHEDULES);
  }
};


// ============================================================================
// 4. YOUTUBE SERVICE & ENHANCED EXTRACTION
// ============================================================================

const YouTubeService = {
  PLAYLIST_REGEX: /(?:https?:\/\/)?(?:[a-zA-Z0-9-]+\.)?youtu(?:be\.com|\.be)\/.*?[?&]list=([a-zA-Z0-9_-]+)/i,
  DIRECT_ID_REGEX: /^[a-zA-Z0-9_-]{10,40}$/,

  TURKISH_DAYS: {
    1: 'Pazartesi',
    2: 'Salı',
    3: 'Çarşamba',
    4: 'Perşembe',
    5: 'Cuma',
    6: 'Cumartesi',
    7: 'Pazar'
  },

  extractPlaylistId(input) {
    if (!input) return null;
    const trimmed = input.trim();
    const match = trimmed.match(this.PLAYLIST_REGEX);
    if (match && match[1]) return match[1];
    if (this.DIRECT_ID_REGEX.test(trimmed)) return trimmed;
    return null;
  },

  parseIso8601Duration(durationStr) {
    if (!durationStr) return 0;
    const match = durationStr.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
    if (!match) return 0;
    const hours = parseInt(match[1] || '0', 10);
    const minutes = parseInt(match[2] || '0', 10);
    const seconds = parseInt(match[3] || '0', 10);
    return (hours * 3600) + (minutes * 60) + seconds;
  },

  formatDuration(seconds) {
    if (!seconds || seconds <= 0) return '0 dk';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    if (h > 0) {
      return m > 0 ? `${h} sa ${m} dk` : `${h} sa`;
    }
    return `${m} dk`;
  },

  async fetchPlaylist(input, subjectId, apiKey = '') {
    const playlistId = this.extractPlaylistId(input);
    if (!playlistId) {
      throw new Error("Geçerli bir YouTube oynatma listesi linki veya ID'si girin.");
    }

    // 1. If user provided a YouTube Data API v3 key, use official endpoint (CORS supported by Google)
    if (apiKey && apiKey.trim().length > 10) {
      return await this.fetchViaOfficialApi(playlistId, subjectId, apiKey.trim());
    }

    // 2. Otherwise try fallback scraper with multiple CORS proxies
    return await this.fetchViaFallbackScraper(playlistId, subjectId);
  },

  async fetchViaOfficialApi(playlistId, subjectId, apiKey) {
    const listRes = await fetch(
      `https://www.googleapis.com/youtube/v3/playlists?part=snippet&id=${playlistId}&key=${apiKey}`
    );
    if (!listRes.ok) {
      if (listRes.status === 404) throw new Error("Oynatma listesi YouTube'da bulunamadı veya gizli.");
      if (listRes.status === 403) throw new Error("YouTube API kotası aşıldı veya anahtar geçersiz.");
      throw new Error(`YouTube API Hatası (${listRes.status})`);
    }
    const listData = await listRes.json();
    if (!listData.items || listData.items.length === 0) {
      throw new Error("Oynatma listesi bulunamadı veya gizli.");
    }

    const snippet = listData.items[0].snippet;
    const title = snippet.title;
    const channelTitle = snippet.channelTitle;
    const thumb = snippet.thumbnails?.high?.url || snippet.thumbnails?.medium?.url || '';

    // Fetch Videos (up to 50 items)
    const itemsRes = await fetch(
      `https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,contentDetails&maxResults=50&playlistId=${playlistId}&key=${apiKey}`
    );
    if (!itemsRes.ok) throw new Error("Video detayları çekilemedi.");
    const itemsData = await itemsRes.json();
    const rawItems = itemsData.items || [];

    const videoIds = rawItems.map(it => it.contentDetails?.videoId).filter(Boolean);
    let durationMap = {};
    if (videoIds.length > 0) {
      const vRes = await fetch(
        `https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=${videoIds.join(',')}&key=${apiKey}`
      );
      if (vRes.ok) {
        const vData = await vRes.json();
        (vData.items || []).forEach(v => {
          durationMap[v.id] = this.parseIso8601Duration(v.contentDetails?.duration);
        });
      }
    }

    const videos = rawItems.map((item, idx) => {
      const vId = item.contentDetails?.videoId || `v_${idx}`;
      const vTitle = item.snippet?.title || `Ders #${idx + 1}`;
      const dur = durationMap[vId] || (1800 + (idx % 4) * 300);

      return {
        id: vId,
        playlistId,
        subjectId,
        title: vTitle,
        durationSeconds: dur,
        position: idx,
        topic: this.extractTopic(vTitle),
        thumbnailUrl: item.snippet?.thumbnails?.medium?.url || `https://img.youtube.com/vi/${vId}/hqdefault.jpg`,
        isCompleted: false
      };
    });

    const totalDur = videos.reduce((acc, v) => acc + v.durationSeconds, 0);

    return {
      playlist: {
        id: playlistId,
        subjectId,
        title,
        channelTitle,
        thumbnailUrl: thumb,
        itemCount: videos.length,
        totalDurationSeconds: totalDur
      },
      videos
    };
  },

  async fetchViaFallbackScraper(playlistId, subjectId) {
    // Multi-tier endpoints with CORS proxies to bypass browser blocking
    const targetApis = [
      `https://inv.nadeko.net/api/v1/playlists/${playlistId}`,
      `https://invidious.nerdvpn.de/api/v1/playlists/${playlistId}`,
      `https://iv.ggtyler.dev/api/v1/playlists/${playlistId}`
    ];

    const proxyUrls = [];
    targetApis.forEach(api => {
      proxyUrls.push(api); // Direct
      proxyUrls.push(`https://api.allorigins.win/raw?url=${encodeURIComponent(api)}`); // CORS Proxy 1
      proxyUrls.push(`https://corsproxy.io/?url=${encodeURIComponent(api)}`); // CORS Proxy 2
    });

    for (const url of proxyUrls) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000); // 4s timeout

        const res = await fetch(url, {
          signal: controller.signal,
          headers: { 'Accept': 'application/json' }
        });
        clearTimeout(timeoutId);

        if (res.ok) {
          const data = await res.json();
          const title = data.title || data.name || "YouTube Oynatma Listesi";
          const channel = data.author || data.uploader || "Eğitim Kanalı";
          const thumb = data.playlistThumbnail || (data.videos?.[0]?.videoThumbnails?.[0]?.url) || "";

          const rawList = data.videos || data.relatedStreams || [];
          if (rawList.length > 0) {
            const videos = rawList.map((item, idx) => {
              const vid = item.videoId || (item.url ? item.url.replace('/watch?v=', '') : `vid_${idx}`);
              const vTitle = item.title || `Ders #${idx + 1}`;
              const dur = Math.max(item.lengthSeconds || item.duration || 1800, 300);

              return {
                id: vid,
                playlistId,
                subjectId,
                title: vTitle,
                durationSeconds: dur,
                position: idx,
                topic: this.extractTopic(vTitle),
                thumbnailUrl: `https://img.youtube.com/vi/${vid}/hqdefault.jpg`,
                isCompleted: false
              };
            });

            const totalDuration = videos.reduce((acc, v) => acc + v.durationSeconds, 0);
            return {
              playlist: {
                id: playlistId,
                subjectId,
                title,
                channelTitle: channel,
                thumbnailUrl: thumb,
                itemCount: videos.length,
                totalDurationSeconds: totalDuration
              },
              videos
            };
          }
        }
      } catch (err) {
        // Try next fallback
      }
    }

    // If all fail, throw an informative error explaining why
    throw new Error(
      "YouTube doğrudan veri çekilmesini engelledi (CORS Koruması) veya bu oynatma listesi YouTube'da bulunamadı/gizli."
    );
  },

  extractTopic(videoTitle) {
    if (!videoTitle) return 'Genel Konu';
    const clean = videoTitle.trim();
    if (clean.includes('|')) return clean.split('|')[0].trim();
    if (clean.includes('-')) return clean.split('-')[0].trim();
    if (clean.includes(':')) return clean.split(':')[0].trim();
    return clean.slice(0, 35);
  },

  /**
   * Manuel / Hızlı Ders & Video Üretici
   * Kullanıcı YouTube linkiyle uğraşmadan dilediği ders için saniyeler içinde özel liste kurabilir!
   */
  createCustomPlaylist(subjectId, title, count, averageDurationMinutes, videoTitles = []) {
    const pId = `custom_${subjectId}_${Date.now()}`;
    const targetSubject = SUBJECTS.find(s => s.id === subjectId) || SUBJECTS[0];
    const durationSeconds = Math.max(10, averageDurationMinutes) * 60;

    const videos = [];
    const videoCount = Math.max(1, count);

    for (let i = 0; i < videoCount; i++) {
      const vTitle = (videoTitles && videoTitles[i])
        ? videoTitles[i].trim()
        : `${targetSubject.shortName} #${i + 1} - Konu Anlatımı & Soru Çözümü`;

      videos.push({
        id: `custom_vid_${subjectId}_${i + 1}`,
        playlistId: pId,
        subjectId,
        title: vTitle,
        durationSeconds: durationSeconds,
        position: i,
        topic: this.extractTopic(vTitle),
        thumbnailUrl: `https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=300&q=80`,
        isCompleted: false
      });
    }

    const totalDur = videos.reduce((acc, v) => acc + v.durationSeconds, 0);

    return {
      playlist: {
        id: pId,
        subjectId,
        title: title || `${targetSubject.name} Özel Çalışma Serisi`,
        channelTitle: 'Öğretmen Çalışma Masası',
        thumbnailUrl: 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80',
        itemCount: videos.length,
        totalDurationSeconds: totalDur
      },
      videos
    };
  },

  /**
   * AGS + YDS Tam Müfredat Örnek Paketi (53 Video)
   */
  getFullAgsAndYdsCurriculum() {
    const curricula = [
      {
        subjectId: 'egitim-bilimleri',
        playlistTitle: 'AGS 2026 - Eğitim Bilimleri & Millî Eğitim Sistemi Kapsamlı Set',
        channel: 'Akademi Eğitim Bilimleri',
        thumbnailUrl: 'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=400&q=80',
        lessons: [
          ["Gelişim Psikolojisi #1 - Temel İlkeler ve Fiziksel Gelişim", 2100, "Gelişim Psikolojisi"],
          ["Gelişim Psikolojisi #2 - Piaget Bilişsel Gelişim Dönemleri", 2700, "Gelişim Psikolojisi"],
          ["Gelişim Psikolojisi #3 - Erikson Psikososyal Gelişim Kuramı", 2400, "Gelişim Psikolojisi"],
          ["Öğrenme Psikolojisi #1 - Klasik Koşullanma ve İlkeleri", 2400, "Öğrenme Psikolojisi"],
          ["Öğrenme Psikolojisi #2 - Edimsel Koşullanma & Pekiştireçler", 2700, "Öğrenme Psikolojisi"],
          ["Öğretim Yöntem ve Teknikleri (ÖYT) - Çağdaş Yaklaşımlar", 3000, "ÖYT"],
          ["Öğretim Yöntem ve Teknikleri (ÖYT) - Aktif Öğrenme Modelleri", 2700, "ÖYT"],
          ["Ölçme ve Değerlendirme - Test İstatistiği ve Madde Analizi", 2400, "Ölçme ve Değerlendirme"],
          ["Rehberlik ve Özel Eğitim - Bireyi Tanıma Teknikleri", 2100, "Rehberlik"],
          ["Türk Millî Eğitim Sistemi ve Teşkilat Yapısı - Bakanlık Vizyonu", 2400, "MEB Sistemi"]
        ]
      },
      {
        subjectId: 'turkce',
        playlistTitle: 'AGS 2026 - Sözel Yetenek & Paragraf Taktikleri',
        channel: 'Türkçe Ustası',
        thumbnailUrl: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=400&q=80',
        lessons: [
          ["Sözcükte ve Cümlede Anlam - Örtülü Anlam & Yorum", 1800, "Anlam Bilgisi"],
          ["Paragrafta Ana Düşünce ve Yardımcı Fikirler Taktikleri", 2400, "Paragraf"],
          ["Paragraf Yapısı - Akışı Bozan Cümle & Yer Değiştirme", 2100, "Paragraf"],
          ["Dil Bilgisi - Ses Olayları ve Yazım Kuralları", 2400, "Dil Bilgisi"],
          ["Dil Bilgisi - Noktalama İşaretleri ve Püf Noktaları", 1800, "Dil Bilgisi"],
          ["Dil Bilgisi - Cümlenin Ögeleri ve Cümle Türleri", 2400, "Dil Bilgisi"],
          ["Anlatım Bozuklukları - Bağlaşıklık ve Bağdaşıklık", 1800, "Anlatım Bozukluğu"],
          ["Sözel Mantık - Tablo Oluşturma ve Kesin Çıkarım", 2700, "Sözel Mantık"]
        ]
      },
      {
        subjectId: 'matematik',
        playlistTitle: 'AGS 2026 - Sayısal Yetenek & Problem Çözümleri',
        channel: 'Matematik Rehberi',
        thumbnailUrl: 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=400&q=80',
        lessons: [
          ["Temel Kavramlar, Tek-Çift ve Asal Sayılar", 2100, "Temel Sayılar"],
          ["Bölme, Bölünebilme Kuralları ve EBOB-EKOK", 2400, "Bölünebilme"],
          ["Rasyonel Sayılar ve Ondalık Gösterim", 1800, "Rasyonel Sayılar"],
          ["Birinci Dereceden Denklemler ve Eşitsizlikler", 2400, "Cebir"],
          ["Oran-Orantı ve Problem Çözme Stratejileri", 2100, "Problemler"],
          ["Sayı, Kesir ve Yaş Problemleri (Yeni Nesil)", 2700, "Problemler"],
          ["Hız, Yüzde, Kâr-Zarar Problemleri", 2400, "Problemler"],
          ["Tablo, Grafik Yorumlama ve Sayısal Mantık", 2700, "Sayısal Mantık"]
        ]
      },
      {
        subjectId: 'mevzuat',
        playlistTitle: 'AGS 2026 - Eğitim Mevzuatı & Hukuk Tam Seri',
        channel: 'Eğitim Mevzuatı Platformu',
        thumbnailUrl: 'https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=400&q=80',
        lessons: [
          ["T.C. Anayasası - Temel Hak ve Ödevler, Eğitim Hakkı", 2400, "Anayasa Hukuku"],
          ["Öğretmenlik Mesleği Kanunu (7528 Sayılı ÖMK Analizi)", 2700, "ÖMK"],
          ["1739 Sayılı Millî Eğitim Temel Kanunu - Temel İlkeler", 2100, "1739 Kanun"],
          ["222 Sayılı İlköğretim ve Eğitim Kanunu Esasları", 1800, "222 Kanun"],
          ["657 Sayılı Devlet Memurları Kanunu - Disiplin & Haklar", 2400, "657 DMK"],
          ["Cumhurbaşkanlığı Kararnamesi 1 Nolu - MEB Teşkilatı", 2100, "1 Nolu CBK"]
        ]
      },
      {
        subjectId: 'tarih',
        playlistTitle: 'AGS 2026 - Tarih Konu Anlatımı & Kronoloji',
        channel: 'Tarih Akademisi',
        thumbnailUrl: 'https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=400&q=80',
        lessons: [
          ["İslamiyet Öncesi Türk Devletleri Kültür ve Medeniyeti", 2100, "İlk Türk Devletleri"],
          ["İlk Türk-İslam Devletleri ve Anadolu Selçuklu Tarihi", 2400, "Türk-İslam"],
          ["Osmanlı Devleti Kuruluş ve Yükselme Dönemleri", 2700, "Osmanlı Devleti"],
          ["Osmanlı Kültür, Sanat ve Yönetim Teşkilatı", 2400, "Osmanlı Teşkilat"],
          ["20. Yüzyıl Başlarında Osmanlı ve I. Dünya Savaşı", 2100, "I. Dünya Savaşı"],
          ["Millî Mücadele Hazırlık Dönemi & Muharebeler", 2700, "Millî Mücadele"],
          ["Atatürk İlke ve İnkılapları & Çağdaş Türk Tarihi", 2400, "İnkılap Tarihi"]
        ]
      },
      {
        subjectId: 'cografya',
        playlistTitle: 'AGS 2026 - Haritalarla Türkiye Coğrafyası',
        channel: 'Coğrafya Atlası',
        thumbnailUrl: 'https://images.unsplash.com/photo-1524661135-423995f22d0b?w=400&q=80',
        lessons: [
          ["Türkiye'nin Coğrafi Konumu, Jeopolitiği ve Sonuçları", 2100, "Konum"],
          ["Türkiye'nin Yer Şekilleri, Dağlar, Ovalar ve Platolar", 2700, "Fiziki Coğrafya"],
          ["Türkiye'nin İklimi, Sıcaklık Dağılımı ve Bitki Örtüsü", 2400, "İklim"],
          ["Türkiye'de Nüfus, Yerleşme ve Göç Dinamikleri", 2100, "Beşeri Coğrafya"],
          ["Türkiye'de Tarım, Hayvancılık ve Ormancılık", 2100, "Ekonomik Coğrafya"],
          ["Madenler, Enerji Kaynakları, Sanayi ve Ulaşım", 2400, "Ekonomik Coğrafya"]
        ]
      },
      {
        subjectId: 'yds',
        playlistTitle: 'YDS İngilizce - Sınav Stratejileri & Master Plan',
        channel: 'İngilizce Sınav Merkezi',
        thumbnailUrl: 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=400&q=80',
        lessons: [
          ["YDS Gramer #1 - Zamanlar (Tenses) ve Zaman Uyumu Kuralları", 2400, "Gramer (Grammar)"],
          ["YDS Gramer #2 - Modals, Passive & Causatives", 2700, "Gramer (Grammar)"],
          ["YDS Gramer #3 - Bağlaçlar (Conjunctions) ve Geçiş İfadeleri", 2700, "Gramer (Grammar)"],
          ["YDS Kelime Stratejisi - En Sık Çıkan Akademik Sıfat & Fiiller", 2400, "Kelime (Vocabulary)"],
          ["Cümle Tamamlama ve Paragraf Tamamlama Çözüm Taktikleri", 2700, "Soru Tipleri"],
          ["İngilizce-Türkçe & Türkçe-İngilizce Çeviri Teknikleri", 2100, "Çeviri"],
          ["Akademik Paragraf Okuma ve Soru Çözüm Analizi", 2700, "Okuma (Reading)"],
          ["YDS Mini Deneme Çözümü ve Hatalı Seçenek Eleme Sanatı", 3000, "Deneme Analizi"]
        ]
      }
    ];

    const allPlaylists = [];
    const allVideos = [];

    curricula.forEach(curr => {
      const pId = `sample_playlist_${curr.subjectId}`;
      const vids = curr.lessons.map(([title, duration, topic], idx) => ({
        id: `demo_${curr.subjectId}_${idx}`,
        playlistId: pId,
        subjectId: curr.subjectId,
        title,
        durationSeconds: duration,
        position: idx,
        topic,
        thumbnailUrl: curr.thumbnailUrl,
        isCompleted: false
      }));

      const totalDur = vids.reduce((sum, v) => sum + v.durationSeconds, 0);

      allPlaylists.push({
        id: pId,
        subjectId: curr.subjectId,
        title: curr.playlistTitle,
        channelTitle: curr.channel,
        thumbnailUrl: curr.thumbnailUrl,
        itemCount: vids.length,
        totalDurationSeconds: totalDur
      });

      allVideos.push(...vids);
    });

    return { playlists: allPlaylists, videos: allVideos };
  }
};


// ============================================================================
// 5. SMART SCHEDULING ENGINE
// ============================================================================

const SmartSchedulingEngine = {
  generateSchedule(videos, dailyCapacityMinutes, selectedDays, startDateStr) {
    if (!videos || videos.length === 0) return [];

    const capacitySeconds = Math.max(15, dailyCapacityMinutes) * 60;
    const activeDaysSet = new Set(selectedDays && selectedDays.length > 0 ? selectedDays : [1, 2, 3, 4, 5]);

    let currentDate = this.parseLocalDate(startDateStr);

    const schedulePlans = [];
    const queue = [...videos];

    while (queue.length > 0) {
      let currentIsoDay = this.getIsoDay(currentDate);
      while (!activeDaysSet.has(currentIsoDay)) {
        currentDate.setDate(currentDate.getDate() + 1);
        currentIsoDay = this.getIsoDay(currentDate);
      }

      const dateStr = this.formatLocalDate(currentDate);
      const dayName = YouTubeService.TURKISH_DAYS[currentIsoDay] || 'Gün';

      const assignedVideoIds = [];
      let accumulatedSeconds = 0;

      while (queue.length > 0) {
        const nextVideo = queue[0];
        const vDuration = nextVideo.durationSeconds;

        if (assignedVideoIds.length === 0) {
          queue.shift();
          assignedVideoIds.push(nextVideo.id);
          accumulatedSeconds += vDuration;

          if (accumulatedSeconds >= capacitySeconds) {
            break;
          }
        } else {
          if (accumulatedSeconds + vDuration <= capacitySeconds) {
            queue.shift();
            assignedVideoIds.push(nextVideo.id);
            accumulatedSeconds += vDuration;
          } else {
            break;
          }
        }
      }

      if (assignedVideoIds.length > 0) {
        schedulePlans.push({
          date: dateStr,
          dayOfWeek: currentIsoDay,
          dayOfWeekName: dayName,
          totalAssignedSeconds: accumulatedSeconds,
          videoIds: assignedVideoIds
        });
      }

      currentDate.setDate(currentDate.getDate() + 1);
    }

    return schedulePlans;
  },

  getIsoDay(date) {
    const day = date.getDay();
    return day === 0 ? 7 : day;
  },

  parseLocalDate(dateStr) {
    if (!dateStr) {
      const now = new Date();
      now.setHours(0, 0, 0, 0);
      return now;
    }
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      return new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
    }
    const d = new Date(dateStr);
    d.setHours(0, 0, 0, 0);
    return d;
  },

  formatLocalDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
};


// ============================================================================
// 6. UI CONTROLLER (AppUI)
// ============================================================================

const AppUI = {
  activeTab: 'tab-today',
  deferredInstallPrompt: null,

  init() {
    ThemeManager.init();
    LayoutManager.init();
    this.bindEvents();
    this.initPwa();
    this.initSettingsValues();
    this.render();
  },

  initPwa() {
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js').then((reg) => {
          console.log('PWA ServiceWorker registered:', reg.scope);
        }).catch((err) => {
          console.warn('PWA SW error:', err);
        });
      });
    }

    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      this.deferredInstallPrompt = e;
      const btn = document.getElementById('pwaInstallBtn');
      if (btn) btn.classList.remove('hidden');
    });

    document.getElementById('pwaInstallBtn')?.addEventListener('click', async () => {
      if (this.deferredInstallPrompt) {
        this.deferredInstallPrompt.prompt();
        const choice = await this.deferredInstallPrompt.userChoice;
        if (choice.outcome === 'accepted') {
          this.showToast('Uygulama ana ekranınıza kuruluyor!', 'success');
        }
        this.deferredInstallPrompt = null;
        document.getElementById('pwaInstallBtn')?.classList.add('hidden');
      }
    });
  },

  bindEvents() {
    document.getElementById('quickThemeToggleBtn')?.addEventListener('click', () => {
      ThemeManager.toggle();
    });

    document.getElementById('quickLayoutToggleBtn')?.addEventListener('click', () => {
      LayoutManager.toggle();
    });

    document.querySelectorAll('.nav-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const target = btn.getAttribute('data-tab');
        if (target) this.switchTab(target);
      });
    });

    const range = document.getElementById('capacityRange');
    range?.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      this.updateCapacityDisplay(val);
      const settings = StorageService.getSettings();
      settings.dailyCapacityMinutes = val;
      StorageService.saveSettings(settings);
    });

    document.querySelectorAll('.day-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const day = parseInt(btn.getAttribute('data-day'), 10);
        this.toggleStudyDay(day);
      });
    });

    document.getElementById('startDateInput')?.addEventListener('change', (e) => {
      const settings = StorageService.getSettings();
      settings.startDate = e.target.value;
      StorageService.saveSettings(settings);
    });

    document.getElementById('generateScheduleBtn')?.addEventListener('click', () => {
      this.handleGenerateSchedule();
    });

    // Import YouTube Playlist
    document.getElementById('importPlaylistBtn')?.addEventListener('click', () => {
      this.handleImportPlaylist();
    });

    document.getElementById('pasteUrlBtn')?.addEventListener('click', async () => {
      try {
        const text = await navigator.clipboard.readText();
        const input = document.getElementById('playlistUrlInput');
        if (input && text) {
          input.value = text;
          this.showToast('Bağlantı panodan yapıştırıldı', 'info');
        }
      } catch {
        this.showToast('Pano okunamadı, elle yapıştırın', 'warning');
      }
    });

    // Custom Quick Playlist Generator Form submit
    document.getElementById('saveCustomPlaylistBtn')?.addEventListener('click', () => {
      this.handleCreateCustomPlaylist();
    });

    // Load Sample Full Curriculum Buttons
    document.getElementById('loadSampleCurriculumBtn')?.addEventListener('click', () => {
      this.handleLoadFullCurriculum();
    });

    document.getElementById('loadSampleCurriculumSettingsBtn')?.addEventListener('click', () => {
      this.handleLoadFullCurriculum();
    });

    // Theme & Layout Selectors in Settings
    document.querySelectorAll('.theme-selector-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const th = btn.getAttribute('data-theme');
        ThemeManager.setTheme(th);
      });
    });

    document.querySelectorAll('.layout-selector-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const lay = btn.getAttribute('data-layout');
        LayoutManager.setLayout(lay);
      });
    });

    // API Key Save & Reset Data
    document.getElementById('saveApiKeyBtn')?.addEventListener('click', () => {
      const input = document.getElementById('apiKeyInput');
      const settings = StorageService.getSettings();
      settings.apiKey = input ? input.value.trim() : '';
      StorageService.saveSettings(settings);
      this.showToast('YouTube API Anahtarı başarıyla kaydedildi!', 'success');
      this.closeHelpModal();
    });

    document.getElementById('resetAllDataBtn')?.addEventListener('click', () => {
      if (confirm('Tüm dersler, videolar ve çalışma programınız silinecek. Emin misiniz?')) {
        StorageService.resetAll();
        this.showToast('Tüm veriler sıfırlandı.', 'info');
        this.render();
      }
    });
  },

  switchTab(tabId) {
    this.activeTab = tabId;
    document.querySelectorAll('.tab-content').forEach(tab => {
      tab.classList.add('hidden');
    });

    const activeEl = document.getElementById(tabId);
    if (activeEl) {
      activeEl.classList.remove('hidden');
    }

    document.querySelectorAll('.nav-item').forEach(btn => {
      const isTarget = btn.getAttribute('data-tab') === tabId;
      if (isTarget) {
        btn.classList.add('text-brand-400');
        btn.classList.remove('text-slate-400');
      } else {
        btn.classList.remove('text-brand-400');
        btn.classList.add('text-slate-400');
      }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.render();
  },

  initSettingsValues() {
    const settings = StorageService.getSettings();

    const range = document.getElementById('capacityRange');
    if (range) range.value = settings.dailyCapacityMinutes;
    this.updateCapacityDisplay(settings.dailyCapacityMinutes);

    this.renderDayPickers(settings.selectedDays);

    const dateInput = document.getElementById('startDateInput');
    if (dateInput) {
      dateInput.value = settings.startDate || SmartSchedulingEngine.formatLocalDate(new Date());
    }

    const apiInput = document.getElementById('apiKeyInput');
    if (apiInput) apiInput.value = settings.apiKey || '';

    const subjectSelect = document.getElementById('playlistSubjectSelect');
    const customSubjectSelect = document.getElementById('customSubjectSelect');

    const optionsHtml = SUBJECTS.map(s => `
      <option value="${s.id}">${s.name} [${s.badge}]</option>
    `).join('');

    if (subjectSelect) subjectSelect.innerHTML = optionsHtml;
    if (customSubjectSelect) customSubjectSelect.innerHTML = optionsHtml;
  },

  updateCapacityDisplay(minutes) {
    const badge = document.getElementById('capacityDisplayBadge');
    if (!badge) return;
    const h = (minutes / 60).toFixed(1).replace('.0', '');
    badge.textContent = `${minutes} dk (${h} sa)`;

    document.querySelectorAll('.capacity-chip').forEach(chip => {
      const cVal = parseInt(chip.textContent, 10);
      if (cVal === minutes) {
        chip.classList.add('bg-brand-600', 'text-white', 'border-brand-500');
        chip.classList.remove('bg-slate-800', 'text-slate-300');
      } else {
        chip.classList.remove('bg-brand-600', 'text-white', 'border-brand-500');
        chip.classList.add('bg-slate-800', 'text-slate-300');
      }
    });
  },

  setCapacity(val) {
    const range = document.getElementById('capacityRange');
    if (range) range.value = val;
    this.updateCapacityDisplay(val);
    const settings = StorageService.getSettings();
    settings.dailyCapacityMinutes = val;
    StorageService.saveSettings(settings);
  },

  renderDayPickers(selectedDays) {
    const set = new Set(selectedDays);
    document.querySelectorAll('.day-btn').forEach(btn => {
      const d = parseInt(btn.getAttribute('data-day'), 10);
      if (set.has(d)) {
        btn.classList.add('bg-brand-600', 'text-white', 'border-brand-500');
        btn.classList.remove('bg-slate-800', 'text-slate-400', 'border-darkBorder');
      } else {
        btn.classList.remove('bg-brand-600', 'text-white', 'border-brand-500');
        btn.classList.add('bg-slate-800', 'text-slate-400', 'border-darkBorder');
      }
    });
  },

  toggleStudyDay(day) {
    const settings = StorageService.getSettings();
    let days = settings.selectedDays || [];
    if (days.includes(day)) {
      if (days.length === 1) {
        this.showToast('En az 1 çalışma günü seçili olmalıdır.', 'warning');
        return;
      }
      days = days.filter(d => d !== day);
    } else {
      days.push(day);
      days.sort((a, b) => a - b);
    }
    settings.selectedDays = days;
    StorageService.saveSettings(settings);
    this.renderDayPickers(days);
  },

  setSubjectFilter(subjectId) {
    const settings = StorageService.getSettings();
    settings.activeSubjectFilter = subjectId;
    StorageService.saveSettings(settings);
    this.render();
  },

  render() {
    this.renderHeaderInfo();
    this.renderSubjectFilterChips();

    switch (this.activeTab) {
      case 'tab-today':
        this.renderTodayView();
        break;
      case 'tab-schedule':
        this.renderScheduleView();
        break;
      case 'tab-progress':
        this.renderProgressView();
        break;
      case 'tab-subjects':
        this.renderSubjectsView();
        break;
      case 'tab-settings':
        this.renderSettingsView();
        break;
    }
  },

  renderHeaderInfo() {
    const headerTitle = document.getElementById('headerCourseTitle');
    const videos = StorageService.getVideos();
    const playlists = StorageService.getPlaylists();

    if (videos.length === 0) {
      if (headerTitle) headerTitle.textContent = "Ders Yüklenmedi";
    } else {
      const completed = videos.filter(v => v.isCompleted).length;
      const pct = Math.round((completed / videos.length) * 100);
      if (headerTitle) {
        headerTitle.textContent = `${playlists.length} Aktif Ders • %${pct} Tamamlandı`;
      }
    }
  },

  renderSubjectFilterChips() {
    const settings = StorageService.getSettings();
    const currentFilter = settings.activeSubjectFilter || 'all';

    const containers = [
      document.getElementById('todaySubjectFilterContainer'),
      document.getElementById('scheduleSubjectFilterContainer')
    ];

    containers.forEach(container => {
      if (!container) return;

      const chips = [
        { id: 'all', name: 'Tüm Dersler', icon: 'fa-layer-group', color: '#6366f1' },
        ...SUBJECTS.map(s => ({ id: s.id, name: s.shortName, icon: s.icon, color: s.color }))
      ];

      container.innerHTML = chips.map(c => {
        const isActive = c.id === currentFilter;
        const activeClass = isActive
          ? 'bg-brand-600 text-white border-brand-500 shadow-md shadow-brand-500/20'
          : 'bg-darkSurface text-slate-300 hover:text-white border-darkBorder hover:bg-slate-800';

        return `
          <button type="button" onclick="AppUI.setSubjectFilter('${c.id}')"
                  class="shrink-0 px-3 py-1.5 rounded-xl text-xs font-semibold border transition flex items-center gap-1.5 ${activeClass}">
            <i class="fa-solid ${c.icon}" style="color: ${isActive ? '#ffffff' : c.color}"></i>
            <span>${c.name}</span>
          </button>
        `;
      }).join('');
    });
  },

  // ==========================================================================
  // TAB 1: BUGÜN (TODAY VIEW)
  // ==========================================================================
  renderTodayView() {
    const settings = StorageService.getSettings();
    const todayStr = SmartSchedulingEngine.formatLocalDate(new Date());

    const dateLabel = document.getElementById('todayDateLabel');
    if (dateLabel) {
      const now = new Date();
      const options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
      dateLabel.textContent = now.toLocaleDateString('tr-TR', options);
    }

    const allVideos = StorageService.getVideos();
    const allSchedules = StorageService.getSchedules();

    const emptyState = document.getElementById('todayEmptyState');
    const celebration = document.getElementById('todayCelebrationBanner');
    const listContainer = document.getElementById('todayVideoList');
    const filterId = settings.activeSubjectFilter || 'all';

    if (allVideos.length === 0 || allSchedules.length === 0) {
      if (emptyState) emptyState.classList.remove('hidden');
      if (celebration) celebration.classList.add('hidden');
      if (listContainer) listContainer.innerHTML = '';
      this.updateTodayProgressMetrics([], []);
      return;
    }

    const todaySchedule = allSchedules.find(s => s.date === todayStr);

    if (!todaySchedule || !todaySchedule.videoIds || todaySchedule.videoIds.length === 0) {
      if (emptyState) {
        emptyState.classList.remove('hidden');
        const reason = document.getElementById('todayEmptyReason');
        if (reason) reason.textContent = "Bugün için planlanan ders bulunmuyor. Dinlenme gününüz olabilir veya program tamamlanmıştır.";
      }
      if (celebration) celebration.classList.add('hidden');
      if (listContainer) listContainer.innerHTML = '';
      this.updateTodayProgressMetrics([], []);
      return;
    }

    if (emptyState) emptyState.classList.add('hidden');

    let todayVideos = todaySchedule.videoIds
      .map(vId => allVideos.find(v => v.id === vId))
      .filter(Boolean);

    const displayVideos = filterId === 'all'
      ? todayVideos
      : todayVideos.filter(v => v.subjectId === filterId);

    this.updateTodayProgressMetrics(todayVideos, displayVideos);

    if (displayVideos.length === 0) {
      listContainer.innerHTML = `
        <div class="text-center py-8 text-xs text-slate-400 bg-darkSurface/50 rounded-2xl border border-dashed border-darkBorder">
          Seçilen derse ait bugün için video planlanmamış.
        </div>
      `;
      return;
    }

    listContainer.innerHTML = displayVideos.map(video => {
      const subject = SUBJECTS.find(s => s.id === video.subjectId) || {
        name: 'Ders', icon: 'fa-book', color: '#6366f1', badge: 'AGS'
      };
      const durationStr = YouTubeService.formatDuration(video.durationSeconds);
      const isDone = video.isCompleted;

      return `
        <div class="bg-darkSurface border border-darkBorder hover:border-brand-500/40 rounded-2xl p-3.5 transition shadow-md flex items-center gap-3 group">
          <label class="cursor-pointer relative flex items-center justify-center shrink-0">
            <input type="checkbox" ${isDone ? 'checked' : ''} 
                   onchange="AppUI.toggleVideo('${video.id}', this.checked)"
                   class="lesson-checkbox sr-only">
            <div class="w-7 h-7 rounded-xl border-2 ${isDone ? 'bg-emerald-600 border-emerald-500' : 'border-slate-600 bg-slate-800/80 group-hover:border-brand-500'} flex items-center justify-center transition">
              <i class="fa-solid fa-check text-white text-xs ${isDone ? 'opacity-100' : 'opacity-0'} transition"></i>
            </div>
          </label>

          <div class="relative w-16 h-12 rounded-xl overflow-hidden shrink-0 bg-slate-800 cursor-pointer" onclick="AppUI.openVideoModal('${video.id}', '${video.title.replace(/'/g, "\\'")}')">
            <img src="${video.thumbnailUrl}" alt="Thumb" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-black/40 flex items-center justify-center group-hover:bg-black/20 transition">
              <i class="fa-solid fa-play text-white text-xs drop-shadow"></i>
            </div>
          </div>

          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-1.5 mb-0.5">
              <span class="subject-badge" style="background-color: ${subject.color}25; color: ${subject.color}; border: 1px solid ${subject.color}40">
                <i class="fa-solid ${subject.icon} mr-1"></i>${subject.shortName || subject.name}
              </span>
              <span class="text-[10px] text-slate-400 truncate">• ${video.topic || 'Konu'}</span>
            </div>
            <h4 class="text-xs font-bold ${isDone ? 'completed-text text-slate-400' : 'text-slate-100'} line-clamp-1 leading-snug">
              ${video.title}
            </h4>
            <div class="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
              <span><i class="fa-solid fa-clock text-slate-500 mr-1"></i>${durationStr}</span>
              ${isDone ? '<span class="text-emerald-400 font-semibold"><i class="fa-solid fa-circle-check mr-1"></i>Tamamlandı</span>' : ''}
            </div>
          </div>
        </div>
      `;
    }).join('');
  },

  updateTodayProgressMetrics(todayVideos, displayVideos) {
    const badge = document.getElementById('todayProgressBadge');
    const countLabel = document.getElementById('todayVideosCountLabel');
    const durLabel = document.getElementById('todayDurationLabel');
    const progressBar = document.getElementById('todayProgressBar');
    const remainingLabel = document.getElementById('todayRemainingTimeLabel');
    const statusText = document.getElementById('todayDayStatusText');
    const celebration = document.getElementById('todayCelebrationBanner');
    const itemCountBadge = document.getElementById('todayItemCountBadge');

    if (itemCountBadge) itemCountBadge.textContent = `${displayVideos.length} Ders`;

    if (todayVideos.length === 0) {
      if (badge) badge.textContent = '%0';
      if (countLabel) countLabel.textContent = "0 dersten 0'ı tamamlandı";
      if (durLabel) durLabel.textContent = '0 dk';
      if (progressBar) progressBar.style.width = '0%';
      if (remainingLabel) remainingLabel.textContent = 'Kalan: 0 dk';
      return;
    }

    const completed = todayVideos.filter(v => v.isCompleted).length;
    const total = todayVideos.length;
    const pct = Math.round((completed / total) * 100);

    const totalSec = todayVideos.reduce((acc, v) => acc + v.durationSeconds, 0);
    const completedSec = todayVideos.filter(v => v.isCompleted).reduce((acc, v) => acc + v.durationSeconds, 0);
    const remSec = Math.max(0, totalSec - completedSec);

    if (badge) badge.textContent = `%${pct}`;
    if (countLabel) countLabel.textContent = `${total} dersten ${completed}'i tamamlandı`;
    if (durLabel) durLabel.textContent = YouTubeService.formatDuration(totalSec);
    if (progressBar) progressBar.style.width = `${pct}%`;
    if (remainingLabel) remainingLabel.textContent = `Kalan: ${YouTubeService.formatDuration(remSec)}`;

    if (pct === 100) {
      if (statusText) statusText.textContent = 'Günün hedefi bitti! 🎯';
      if (celebration) celebration.classList.remove('hidden');
    } else {
      if (statusText) statusText.textContent = `${total - completed} ders kaldı`;
      if (celebration) celebration.classList.add('hidden');
    }
  },

  toggleVideo(videoId, isCompleted) {
    StorageService.toggleVideoCompletion(videoId, isCompleted);
    if (isCompleted) {
      this.showToast('Tebrikler! Ders tamamlandı.', 'success');
      const todaySchedule = StorageService.getSchedules().find(
        s => s.date === SmartSchedulingEngine.formatLocalDate(new Date())
      );
      if (todaySchedule) {
        const allV = StorageService.getVideos();
        const tVideos = todaySchedule.videoIds.map(id => allV.find(v => v.id === id)).filter(Boolean);
        if (tVideos.every(v => v.isCompleted)) {
          this.triggerConfetti();
        }
      }
    }
    this.render();
  },

  triggerConfetti() {
    if (typeof confetti === 'function') {
      confetti({
        particleCount: 120,
        spread: 70,
        origin: { y: 0.6 }
      });
    }
  },

  // ==========================================================================
  // TAB 2: PROGRAM (SCHEDULE TIMELINE)
  // ==========================================================================
  renderScheduleView() {
    const schedules = StorageService.getSchedules();
    const allVideos = StorageService.getVideos();
    const settings = StorageService.getSettings();
    const filterId = settings.activeSubjectFilter || 'all';

    const summaryCard = document.getElementById('scheduleSummaryCard');
    const container = document.getElementById('scheduleTimelineContainer');

    if (!container) return;

    if (schedules.length === 0 || allVideos.length === 0) {
      if (summaryCard) summaryCard.classList.add('hidden');
      container.innerHTML = `
        <div class="text-center py-12 px-6 bg-darkSurface/50 border border-dashed border-darkBorder rounded-2xl">
          <i class="fa-regular fa-calendar-xmark text-4xl text-slate-500 mb-3"></i>
          <h4 class="text-sm font-bold text-slate-200 mb-1">Henüz Program Oluşturulmadı</h4>
          <p class="text-xs text-slate-400 mb-4 max-w-xs mx-auto">
            Kapasitenizi belirleyin veya hazır ders paketini yükleyip takviminizi oluşturun.
          </p>
          <button onclick="AppUI.handleGenerateSchedule()" class="py-2.5 px-4 bg-brand-600 hover:bg-brand-500 text-white text-xs font-bold rounded-xl transition">
            <i class="fa-solid fa-wand-magic-sparkles mr-1.5"></i>
            Programı Otomatik Oluştur
          </button>
        </div>
      `;
      return;
    }

    if (summaryCard) {
      summaryCard.classList.remove('hidden');
      const totalSec = schedules.reduce((acc, s) => acc + s.totalAssignedSeconds, 0);
      const totalHours = (totalSec / 3600).toFixed(1);
      const lastDay = schedules[schedules.length - 1];

      document.getElementById('summaryTotalDays').textContent = `${schedules.length} Gün`;
      document.getElementById('summaryTotalHours').textContent = `${totalHours} sa`;
      document.getElementById('summaryFinishDate').textContent = lastDay?.date || '-';
    }

    const todayStr = SmartSchedulingEngine.formatLocalDate(new Date());

    container.innerHTML = schedules.map((schedule, idx) => {
      const isToday = schedule.date === todayStr;
      const dayVideos = schedule.videoIds
        .map(id => allVideos.find(v => v.id === id))
        .filter(Boolean);

      const filteredDayVideos = filterId === 'all'
        ? dayVideos
        : dayVideos.filter(v => v.subjectId === filterId);

      if (filteredDayVideos.length === 0 && filterId !== 'all') {
        return '';
      }

      const completedCount = filteredDayVideos.filter(v => v.isCompleted).length;
      const isAllDone = filteredDayVideos.length > 0 && completedCount === filteredDayVideos.length;
      const totalDurationStr = YouTubeService.formatDuration(
        filteredDayVideos.reduce((acc, v) => acc + v.durationSeconds, 0)
      );

      return `
        <div class="bg-darkSurface border ${isToday ? 'border-brand-500 shadow-brand-500/10' : 'border-darkBorder'} rounded-2xl p-4 shadow-lg space-y-3">
          <div class="flex items-center justify-between pb-2 border-b border-darkBorder">
            <div class="flex items-center gap-2">
              <span class="w-7 h-7 rounded-xl ${isToday ? 'bg-brand-600 text-white' : 'bg-slate-800 text-slate-300'} flex items-center justify-center font-bold text-xs">
                ${idx + 1}
              </span>
              <div>
                <h4 class="text-xs font-bold text-white flex items-center gap-1.5">
                  ${schedule.date} • ${schedule.dayOfWeekName}
                  ${isToday ? '<span class="text-[10px] bg-brand-500/20 text-brand-400 px-1.5 py-0.5 rounded font-bold">BUGÜN</span>' : ''}
                </h4>
                <p class="text-[10px] text-slate-400">${filteredDayVideos.length} Video • ${totalDurationStr}</p>
              </div>
            </div>

            <span class="text-xs font-bold ${isAllDone ? 'text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-lg border border-emerald-500/20' : 'text-slate-400'}">
              ${completedCount} / ${filteredDayVideos.length}
            </span>
          </div>

          <div class="space-y-2">
            ${filteredDayVideos.map(video => {
              const subj = SUBJECTS.find(s => s.id === video.subjectId) || { name: 'Ders', color: '#6366f1', icon: 'fa-book' };
              const vDone = video.isCompleted;
              return `
                <div class="flex items-center justify-between p-2 rounded-xl bg-slate-900/60 border border-darkBorder/60 hover:border-darkBorder transition text-xs">
                  <div class="flex items-center gap-2 min-w-0 pr-2">
                    <span class="w-2 h-2 rounded-full shrink-0" style="background-color: ${subj.color}"></span>
                    <span class="${vDone ? 'completed-text text-slate-400' : 'text-slate-200'} truncate font-medium">
                      ${video.title}
                    </span>
                  </div>
                  <div class="flex items-center gap-2 shrink-0">
                    <span class="text-[11px] text-slate-400">${YouTubeService.formatDuration(video.durationSeconds)}</span>
                    <input type="checkbox" ${vDone ? 'checked' : ''} 
                           onchange="AppUI.toggleVideo('${video.id}', this.checked)"
                           class="w-4 h-4 rounded text-emerald-600 bg-slate-800 border-slate-700 cursor-pointer">
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      `;
    }).join('');
  },

  // ==========================================================================
  // TAB 3: İLERLEME (PROGRESS & ANALYTICS)
  // ==========================================================================
  renderProgressView() {
    const allVideos = StorageService.getVideos();
    const container = document.getElementById('topicProgressContainer');
    const badge = document.getElementById('overallPercentageBadge');
    const bar = document.getElementById('overallProgressBar');
    const compVideosLabel = document.getElementById('metricCompletedVideos');
    const remDurLabel = document.getElementById('metricRemainingDuration');
    const totDurLabel = document.getElementById('metricTotalDuration');
    const subtitle = document.getElementById('progressCourseSubtitle');

    if (allVideos.length === 0) {
      if (badge) badge.textContent = '%0';
      if (bar) bar.style.width = '0%';
      if (compVideosLabel) compVideosLabel.textContent = '0 / 0';
      if (remDurLabel) remDurLabel.textContent = '0 sa';
      if (totDurLabel) totDurLabel.textContent = '0 sa';
      if (subtitle) subtitle.textContent = 'Henüz ders yüklenmedi';
      if (container) {
        container.innerHTML = `
          <div class="text-center py-8 text-xs text-slate-400 bg-darkSurface/50 rounded-2xl border border-dashed border-darkBorder">
            İlerleme grafiği için önce ders ekleyin veya hazır müfredatı yükleyin.
          </div>
        `;
      }
      return;
    }

    const totalCount = allVideos.length;
    const completedCount = allVideos.filter(v => v.isCompleted).length;
    const overallPct = Math.round((completedCount / totalCount) * 100);

    const totalSeconds = allVideos.reduce((acc, v) => acc + v.durationSeconds, 0);
    const completedSeconds = allVideos.filter(v => v.isCompleted).reduce((acc, v) => acc + v.durationSeconds, 0);
    const remSeconds = Math.max(0, totalSeconds - completedSeconds);

    if (badge) badge.textContent = `%${overallPct}`;
    if (bar) bar.style.width = `${overallPct}%`;
    if (compVideosLabel) compVideosLabel.textContent = `${completedCount} / ${totalCount}`;
    if (remDurLabel) remDurLabel.textContent = YouTubeService.formatDuration(remSeconds);
    if (totDurLabel) totDurLabel.textContent = YouTubeService.formatDuration(totalSeconds);
    if (subtitle) subtitle.textContent = `Toplam ${SUBJECTS.length} Ders • ${totalCount} Video`;

    if (container) {
      container.innerHTML = SUBJECTS.map(subject => {
        const subVideos = allVideos.filter(v => v.subjectId === subject.id);
        const subTotal = subVideos.length;

        if (subTotal === 0) {
          return `
            <div class="bg-darkSurface border border-darkBorder rounded-2xl p-4 shadow-lg opacity-75">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <span class="w-8 h-8 rounded-xl flex items-center justify-center text-sm" style="background-color: ${subject.bgColor}; color: ${subject.color}">
                    <i class="fa-solid ${subject.icon}"></i>
                  </span>
                  <div>
                    <h4 class="text-xs font-bold text-white">${subject.name}</h4>
                    <p class="text-[10px] text-slate-400">${subject.badge}</p>
                  </div>
                </div>
                <button onclick="AppUI.switchTab('tab-subjects')" class="text-[11px] text-brand-400 hover:underline">
                  + Liste Ekle
                </button>
              </div>
              <p class="text-[11px] text-slate-500 italic">Bu derse henüz oynatma listesi eklenmedi.</p>
            </div>
          `;
        }

        const subDone = subVideos.filter(v => v.isCompleted).length;
        const subPct = Math.round((subDone / subTotal) * 100);
        const subRemDur = subVideos.filter(v => !v.isCompleted).reduce((acc, v) => acc + v.durationSeconds, 0);

        return `
          <div class="bg-darkSurface border border-darkBorder hover:border-brand-500/40 rounded-2xl p-4 shadow-lg space-y-3 transition">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2.5">
                <span class="w-9 h-9 rounded-xl flex items-center justify-center text-sm shadow-md" style="background-color: ${subject.bgColor}; color: ${subject.color}; border: 1px solid ${subject.borderColor}">
                  <i class="fa-solid ${subject.icon}"></i>
                </span>
                <div>
                  <h4 class="text-xs font-bold text-white flex items-center gap-1.5">
                    ${subject.name}
                    <span class="subject-badge" style="background-color: ${subject.bgColor}; color: ${subject.color}">
                      ${subject.badge}
                    </span>
                  </h4>
                  <p class="text-[10px] text-slate-400">
                    ${subDone}/${subTotal} Video • Kalan: ${YouTubeService.formatDuration(subRemDur)}
                  </p>
                </div>
              </div>
              <span class="text-base font-black" style="color: ${subject.color}">%${subPct}</span>
            </div>

            <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
              <div class="h-full rounded-full transition-all duration-500" style="width: ${subPct}%; background-color: ${subject.color}"></div>
            </div>
          </div>
        `;
      }).join('');
    }
  },

  // ==========================================================================
  // TAB 4: DERSLER & OYNATMA LİSTELERİ
  // ==========================================================================
  renderSubjectsView() {
    const container = document.getElementById('subjectsGridContainer');
    if (!container) return;

    const allPlaylists = StorageService.getPlaylists();
    const allVideos = StorageService.getVideos();

    container.innerHTML = SUBJECTS.map(subject => {
      const playlist = allPlaylists.find(p => p.subjectId === subject.id);
      const subVideos = allVideos.filter(v => v.subjectId === subject.id);
      const completed = subVideos.filter(v => v.isCompleted).length;
      const totalDur = subVideos.reduce((acc, v) => acc + v.durationSeconds, 0);

      const hasContent = subVideos.length > 0;

      return `
        <div class="bg-darkSurface border border-darkBorder hover:border-brand-500/40 rounded-2xl p-4 shadow-xl transition space-y-3">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-2xl flex items-center justify-center text-lg shrink-0 shadow-md"
                   style="background-color: ${subject.bgColor}; color: ${subject.color}; border: 1px solid ${subject.borderColor}">
                <i class="fa-solid ${subject.icon}"></i>
              </div>
              <div>
                <span class="subject-badge inline-block mb-0.5" style="background-color: ${subject.bgColor}; color: ${subject.color}">
                  ${subject.badge}
                </span>
                <h3 class="text-sm font-bold text-white">${subject.name}</h3>
                <p class="text-[11px] text-slate-400 line-clamp-1">${subject.desc}</p>
              </div>
            </div>
          </div>

          ${hasContent ? `
            <div class="bg-slate-900/80 rounded-xl p-3 border border-darkBorder/60 flex items-center justify-between text-xs">
              <div>
                <p class="font-bold text-slate-200 line-clamp-1">${playlist ? playlist.title : 'Oynatma Listesi'}</p>
                <p class="text-[10px] text-slate-400 mt-0.5">
                  <i class="fa-solid fa-film text-brand-400 mr-1"></i>${subVideos.length} Video • 
                  <i class="fa-solid fa-clock text-brand-400 mx-1"></i>${YouTubeService.formatDuration(totalDur)}
                </p>
              </div>
              <span class="text-xs font-bold text-emerald-400 bg-emerald-500/10 px-2 py-1 rounded-lg border border-emerald-500/20">
                ${completed}/${subVideos.length}
              </span>
            </div>
          ` : `
            <div class="bg-slate-900/40 rounded-xl p-3 border border-dashed border-darkBorder/80 text-center">
              <p class="text-xs text-slate-400">Henüz ders listesi bağlanmadı</p>
            </div>
          `}

          <div class="grid grid-cols-2 gap-2 pt-1">
            <button onclick="AppUI.openAddPlaylistForSubject('${subject.id}')"
                    class="py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 hover:text-white border border-darkBorder text-[11px] font-semibold rounded-xl transition flex items-center justify-center gap-1">
              <i class="fa-brands fa-youtube text-red-500"></i>
              <span>YouTube Bağla</span>
            </button>
            <button onclick="AppUI.openCustomCreatorForSubject('${subject.id}')"
                    class="py-2 bg-brand-600/20 hover:bg-brand-600/30 text-brand-300 border border-brand-500/30 text-[11px] font-semibold rounded-xl transition flex items-center justify-center gap-1">
              <i class="fa-solid fa-bolt text-amber-400"></i>
              <span>Özel Liste Kur</span>
            </button>
          </div>
        </div>
      `;
    }).join('');
  },

  openAddPlaylistForSubject(subjectId) {
    const select = document.getElementById('playlistSubjectSelect');
    if (select) select.value = subjectId;

    const importCard = document.getElementById('playlistImportCard');
    if (importCard) {
      importCard.scrollIntoView({ behavior: 'smooth' });
      document.getElementById('playlistUrlInput')?.focus();
    }
  },

  openCustomCreatorForSubject(subjectId) {
    const select = document.getElementById('customSubjectSelect');
    if (select) select.value = subjectId;

    const targetSubject = SUBJECTS.find(s => s.id === subjectId);
    const titleInput = document.getElementById('customPlaylistTitleInput');
    if (titleInput && targetSubject) {
      titleInput.value = `${targetSubject.shortName} - 2026 Konu Anlatımı`;
    }

    const modal = document.getElementById('customPlaylistModal');
    if (modal) modal.classList.remove('hidden');
  },

  closeCustomCreatorModal() {
    const modal = document.getElementById('customPlaylistModal');
    if (modal) modal.classList.add('hidden');
  },

  openHelpModal() {
    const modal = document.getElementById('apiHelpModal');
    if (modal) modal.classList.remove('hidden');
  },

  closeHelpModal() {
    const modal = document.getElementById('apiHelpModal');
    if (modal) modal.classList.add('hidden');
  },

  // ==========================================================================
  // TAB 5: AYARLAR (SETTINGS VIEW)
  // ==========================================================================
  renderSettingsView() {
    ThemeManager.applyTheme(ThemeManager.getTheme());
    LayoutManager.applyLayout(LayoutManager.getLayout());
  },

  // Import YouTube Playlist
  async handleImportPlaylist() {
    const input = document.getElementById('playlistUrlInput');
    const select = document.getElementById('playlistSubjectSelect');
    const btn = document.getElementById('importPlaylistBtn');

    const url = input?.value.trim();
    const subjectId = select?.value || 'egitim-bilimleri';

    if (!url) {
      this.showToast('Lütfen bir YouTube oynatma listesi linki veya ID girin.', 'warning');
      return;
    }

    try {
      btn.disabled = true;
      btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-1"></i> Çekiliyor...`;

      const settings = StorageService.getSettings();
      const result = await YouTubeService.fetchPlaylist(url, subjectId, settings.apiKey);

      StorageService.savePlaylist(result.playlist);
      StorageService.saveVideos(subjectId, result.videos);

      const allVideos = StorageService.getVideos();
      const schedules = SmartSchedulingEngine.generateSchedule(
        allVideos,
        settings.dailyCapacityMinutes,
        settings.selectedDays,
        settings.startDate
      );
      StorageService.saveSchedules(schedules);

      const targetSubject = SUBJECTS.find(s => s.id === subjectId);
      this.showToast(`"${targetSubject?.name}" için ${result.videos.length} video bağlandı!`, 'success');
      input.value = '';
      this.switchTab('tab-today');
    } catch (err) {
      console.warn("Import playlist error:", err);
      this.showToast(err.message || 'Oynatma listesi yüklenemedi.', 'error');
      // Automatically open solution guidance modal
      this.openHelpModal();
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<i class="fa-solid fa-cloud-arrow-down mr-1.5"></i><span>Listeyi Derse Bağla</span>`;
    }
  },

  // Create Custom Playlist Handler
  handleCreateCustomPlaylist() {
    const subjectSelect = document.getElementById('customSubjectSelect');
    const titleInput = document.getElementById('customPlaylistTitleInput');
    const countInput = document.getElementById('customVideoCountInput');
    const durationInput = document.getElementById('customDurationInput');
    const textarea = document.getElementById('customVideoTitlesTextarea');

    const subjectId = subjectSelect?.value || 'egitim-bilimleri';
    const title = titleInput?.value.trim() || 'Özel Çalışma Serisi';
    const count = parseInt(countInput?.value || '30', 10);
    const duration = parseInt(durationInput?.value || '35', 10);

    const rawTitles = textarea?.value ? textarea.value.split('\n').filter(t => t.trim().length > 0) : [];

    const custom = YouTubeService.createCustomPlaylist(
      subjectId,
      title,
      rawTitles.length > 0 ? rawTitles.length : count,
      duration,
      rawTitles
    );

    StorageService.savePlaylist(custom.playlist);
    StorageService.saveVideos(subjectId, custom.videos);

    const settings = StorageService.getSettings();
    const allVideos = StorageService.getVideos();
    const schedules = SmartSchedulingEngine.generateSchedule(
      allVideos,
      settings.dailyCapacityMinutes,
      settings.selectedDays,
      settings.startDate
    );
    StorageService.saveSchedules(schedules);

    const targetSubject = SUBJECTS.find(s => s.id === subjectId);
    this.showToast(`"${targetSubject?.name}" için ${custom.videos.length} derslik özel program hazırlandı!`, 'success');

    this.closeCustomCreatorModal();
    this.closeHelpModal();
    this.switchTab('tab-today');
  },

  // Load Full AGS + YDS Curriculum
  handleLoadFullCurriculum() {
    const full = YouTubeService.getFullAgsAndYdsCurriculum();

    full.playlists.forEach(pl => StorageService.savePlaylist(pl));
    localStorage.setItem(StorageService.KEYS.VIDEOS, JSON.stringify(full.videos));

    const settings = StorageService.getSettings();
    const schedules = SmartSchedulingEngine.generateSchedule(
      full.videos,
      settings.dailyCapacityMinutes,
      settings.selectedDays,
      settings.startDate
    );
    StorageService.saveSchedules(schedules);

    this.showToast(`AGS + YDS Müfredat Paketi Yüklendi! (7 Ders, ${full.videos.length} Video)`, 'success');
    this.switchTab('tab-today');
  },

  // Generate Schedule
  handleGenerateSchedule() {
    const videos = StorageService.getVideos();
    if (videos.length === 0) {
      this.showToast('Program oluşturmak için önce ders ekleyin veya hazır paketi yükleyin.', 'warning');
      this.switchTab('tab-subjects');
      return;
    }

    const settings = StorageService.getSettings();
    const schedules = SmartSchedulingEngine.generateSchedule(
      videos,
      settings.dailyCapacityMinutes,
      settings.selectedDays,
      settings.startDate
    );

    StorageService.saveSchedules(schedules);
    this.showToast(`Akıllı program güncellendi! (${schedules.length} Gün)`, 'success');
    this.render();
  },

  // Video Player Modal
  openVideoModal(videoId, title) {
    const modal = document.getElementById('videoModal');
    const iframe = document.getElementById('videoIframe');
    const modalTitle = document.getElementById('videoModalTitle');

    if (modal && iframe) {
      if (modalTitle) modalTitle.textContent = title;
      if (videoId.startsWith('custom_vid_') || videoId.startsWith('demo_')) {
        iframe.src = `https://www.youtube.com/embed?listType=search&list=${encodeURIComponent(title)}`;
      } else {
        iframe.src = `https://www.youtube.com/embed/${videoId}?autoplay=1`;
      }
      modal.classList.remove('hidden');
    }
  },

  closeVideoModal() {
    const modal = document.getElementById('videoModal');
    const iframe = document.getElementById('videoIframe');
    if (modal && iframe) {
      iframe.src = '';
      modal.classList.add('hidden');
    }
  },

  // Toast System
  showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    const colors = {
      success: 'bg-emerald-600 text-white shadow-emerald-500/30',
      error: 'bg-rose-600 text-white shadow-rose-500/30',
      warning: 'bg-amber-600 text-white shadow-amber-500/30',
      info: 'bg-brand-600 text-white shadow-brand-500/30'
    };

    const icons = {
      success: 'fa-circle-check',
      error: 'fa-circle-exclamation',
      warning: 'fa-triangle-exclamation',
      info: 'fa-circle-info'
    };

    toast.className = `flex items-center gap-2 px-4 py-2.5 rounded-xl shadow-lg text-xs font-semibold animate-slide-up pointer-events-auto ${colors[type] || colors.info}`;
    toast.innerHTML = `<i class="fa-solid ${icons[type] || icons.info}"></i> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
      toast.classList.add('opacity-0', 'transition-opacity');
      setTimeout(() => toast.remove(), 300);
    }, 3800);
  }
};

// Start App when DOM is ready
if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', () => {
    AppUI.init();
  });
}

// Export for Node/tests
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { SUBJECTS, ThemeManager, LayoutManager, StorageService, YouTubeService, SmartSchedulingEngine, AppUI };
}
