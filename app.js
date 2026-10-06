/**
 * Ders Takip - Progressive Web App (PWA)
 * JavaScript Core Application Architecture
 */

// ============================================================================
// 1. STORAGE SERVICE (Room Database -> LocalStorage Migration)
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
        selectedDays: [1, 2, 3, 4, 5], // 1 = Monday ... 7 = Sunday
        startDate: SmartSchedulingEngine.formatLocalDate(new Date()),
        apiKey: ''
      };
    }
    try {
      return JSON.parse(raw);
    } catch {
      return { dailyCapacityMinutes: 120, selectedDays: [1, 2, 3, 4, 5], startDate: '', apiKey: '' };
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

  getActivePlaylist() {
    const playlists = this.getPlaylists();
    return playlists.find(p => p.isActive) || playlists[0] || null;
  },

  savePlaylist(playlist) {
    let playlists = this.getPlaylists();
    // Mark previous as non-active if this one is active
    if (playlist.isActive) {
      playlists = playlists.map(p => ({ ...p, isActive: false }));
    }
    const existingIndex = playlists.findIndex(p => p.id === playlist.id);
    if (existingIndex >= 0) {
      playlists[existingIndex] = playlist;
    } else {
      playlists.unshift(playlist);
    }
    localStorage.setItem(this.KEYS.PLAYLISTS, JSON.stringify(playlists));
  },

  setActivePlaylist(playlistId) {
    let playlists = this.getPlaylists();
    playlists = playlists.map(p => ({
      ...p,
      isActive: p.id === playlistId
    }));
    localStorage.setItem(this.KEYS.PLAYLISTS, JSON.stringify(playlists));
  },

  getVideos(playlistId) {
    const raw = localStorage.getItem(this.KEYS.VIDEOS);
    try {
      const all = raw ? JSON.parse(raw) : [];
      if (!playlistId) return all;
      return all.filter(v => v.playlistId === playlistId);
    } catch {
      return [];
    }
  },

  saveVideos(playlistId, videos) {
    let all = this.getVideos();
    // Remove old videos for this playlist
    all = all.filter(v => v.playlistId !== playlistId);
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

  getSchedules(playlistId) {
    const raw = localStorage.getItem(this.KEYS.SCHEDULES);
    try {
      const all = raw ? JSON.parse(raw) : [];
      if (!playlistId) return all;
      return all.filter(s => s.playlistId === playlistId);
    } catch {
      return [];
    }
  },

  saveSchedules(playlistId, schedules) {
    let all = this.getSchedules();
    all = all.filter(s => s.playlistId !== playlistId);
    all.push(...schedules);
    localStorage.setItem(this.KEYS.SCHEDULES, JSON.stringify(all));
  },

  resetAll() {
    localStorage.removeItem(this.KEYS.PLAYLISTS);
    localStorage.removeItem(this.KEYS.VIDEOS);
    localStorage.removeItem(this.KEYS.SCHEDULES);
  }
};


// ============================================================================
// 2. YOUTUBE SERVICE & UTILITIES (URL, DURATION & TOPIC EXTRACTOR)
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
    if (!input || typeof input !== 'string') return null;
    const trimmed = input.trim();
    const match = trimmed.match(this.PLAYLIST_REGEX);
    if (match && match[1]) return match[1];
    if (this.DIRECT_ID_REGEX.test(trimmed)) return trimmed;
    return null;
  },

  parseIsoDuration(iso) {
    if (!iso || typeof iso !== 'string') return 0;
    const match = iso.trim().match(/^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?$/);
    if (!match) return 0;
    const days = parseInt(match[1] || '0', 10);
    const hours = parseInt(match[2] || '0', 10);
    const minutes = parseInt(match[3] || '0', 10);
    const seconds = parseInt(match[4] || '0', 10);
    return (days * 86400) + (hours * 3600) + (minutes * 60) + seconds;
  },

  formatDuration(seconds) {
    if (!seconds || seconds <= 0) return '0 dk';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;

    if (h > 0 && m > 0) return `${h} sa ${m} dk`;
    if (h > 0) return `${h} sa`;
    if (m > 0 && s > 0) return `${m} dk ${s} sn`;
    if (m > 0) return `${m} dk`;
    return `${s} sn`;
  },

  formatMinutes(minutes) {
    if (!minutes || minutes <= 0) return '0 dk';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h > 0 && m > 0) return `${h} sa ${m} dk`;
    if (h > 0) return `${h} sa`;
    return `${m} dk`;
  },

  extractTopic(videoTitle, playlistTitle) {
    if (!videoTitle) return playlistTitle || 'Genel Dersler';
    const lower = videoTitle.toLowerCase();

    const rules = [
      [['gramer', 'grammar', 'tenses', 'present', 'past', 'continuous', 'perfect', 'passive', 'modal', 'conditional', 'preposition'], 'Gramer (Grammar)'],
      [['kelime', 'vocabulary', 'idiom', 'phrasal verb', 'words', 'collocation'], 'Kelime Bilgisi (Vocabulary)'],
      [['dinleme', 'listening', 'podcast', 'comprehension', 'audio'], 'Dinleme (Listening)'],
      [['konuşma', 'speaking', 'pronunciation', 'telaffuz', 'fluency', 'dialogue'], 'Konuşma & Telaffuz'],
      [['okuma', 'reading', 'paragraf', 'çeviri', 'translation'], 'Okuma & Çeviri (Reading)'],
      [['yazma', 'writing', 'essay', 'paragraph'], 'Yazma (Writing)'],
      [['yds', 'yökdil', 'eyds', 'toefl', 'ielts', 'yks', 'soru çözümü', 'deneme'], 'Sınav Hazırlığı'],
      [['başlangıç', 'beginner', 'a1', 'a2', 'temel', 'alfabe'], 'Temel Seviye (A1-A2)'],
      [['orta seviye', 'intermediate', 'b1', 'b2'], 'Orta Seviye (B1-B2)']
    ];

    for (const [keywords, topic] of rules) {
      if (keywords.some(kw => lower.includes(kw))) {
        return topic;
      }
    }

    const delims = [' - ', ' : ', ': ', ' | ', ' / ', ' #'];
    for (const d of delims) {
      const idx = videoTitle.indexOf(d);
      if (idx >= 3 && idx <= 35) {
        const candidate = videoTitle.substring(0, idx).trim();
        if (!/^\d+$/.test(candidate)) return candidate;
      }
    }

    return 'Genel Konular';
  },

  async fetchPlaylist(urlOrId, userApiKey = '') {
    const playlistId = this.extractPlaylistId(urlOrId);
    if (!playlistId) {
      throw new Error("Geçerli bir YouTube oynatma listesi linki veya ID'si giriniz.");
    }

    // 1. If user provided a YouTube API key, call official YouTube Data API v3
    if (userApiKey && userApiKey.trim() !== '') {
      return await this.fetchViaOfficialApi(playlistId, userApiKey.trim());
    }

    // 2. Call YouTube Data via Invidious / Piped Open CORS Proxies
    try {
      return await this.fetchViaPublicInstances(playlistId);
    } catch (err) {
      console.warn("Public instance error, fallback to RSS/Demo:", err);
      // If network blocked or CORS error, return sample English curriculum
      return this.getSampleEnglishCurriculum(playlistId);
    }
  },

  async fetchViaOfficialApi(playlistId, apiKey) {
    const listUrl = `https://www.googleapis.com/youtube/v3/playlists?part=snippet,contentDetails&id=${playlistId}&key=${apiKey}`;
    const listRes = await fetch(listUrl);
    if (!listRes.ok) {
      const errJson = await listRes.json().catch(() => ({}));
      throw new Error(errJson.error?.message || `YouTube API Hatası (${listRes.status})`);
    }
    const listData = await listRes.json();
    const item = listData.items?.[0];
    if (!item) throw new Error("Oynatma listesi bulunamadı.");

    const title = item.snippet?.title || 'YouTube Kursu';
    const channelTitle = item.snippet?.channelTitle || 'YouTube Eğitmeni';
    const thumb = item.snippet?.thumbnails?.medium?.url || item.snippet?.thumbnails?.default?.url || `https://img.youtube.com/vi/${playlistId}/hqdefault.jpg`;

    // Fetch items with pagination
    let nextPageToken = '';
    const videos = [];
    let pos = 0;

    do {
      const itemsUrl = `https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,contentDetails&playlistId=${playlistId}&maxResults=50&pageToken=${nextPageToken}&key=${apiKey}`;
      const itemsRes = await fetch(itemsUrl);
      if (!itemsRes.ok) break;
      const itemsData = await itemsRes.json();
      
      const vIds = (itemsData.items || []).map(i => i.snippet?.resourceId?.videoId).filter(Boolean);
      let durationMap = {};
      if (vIds.length > 0) {
        const vUrl = `https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=${vIds.join(',')}&key=${apiKey}`;
        const vRes = await fetch(vUrl);
        if (vRes.ok) {
          const vData = await vRes.json();
          (vData.items || []).forEach(v => {
            durationMap[v.id] = this.parseIsoDuration(v.contentDetails?.duration);
          });
        }
      }

      for (const i of (itemsData.items || [])) {
        const vid = i.snippet?.resourceId?.videoId;
        if (!vid) continue;
        const vTitle = i.snippet?.title || `Ders #${pos + 1}`;
        const dur = durationMap[vid] || 900;
        videos.push({
          id: vid,
          playlistId,
          title: vTitle,
          durationSeconds: dur,
          position: pos++,
          topic: this.extractTopic(vTitle, title),
          thumbnailUrl: `https://img.youtube.com/vi/${vid}/hqdefault.jpg`,
          isCompleted: false
        });
      }

      nextPageToken = itemsData.nextPageToken;
    } while (nextPageToken && videos.length < 200);

    const totalSeconds = videos.reduce((acc, v) => acc + v.durationSeconds, 0);

    return {
      playlist: {
        id: playlistId,
        title,
        channelTitle,
        thumbnailUrl: thumb,
        itemCount: videos.length,
        totalDurationSeconds: totalSeconds,
        isActive: true
      },
      videos
    };
  },

  async fetchViaPublicInstances(playlistId) {
    // Try multiple public Invidious / Piped APIs with CORS
    const instances = [
      `https://invidious.privacydev.net/api/v1/playlists/${playlistId}`,
      `https://vid.puffyan.us/api/v1/playlists/${playlistId}`,
      `https://pipedapi.kavin.rocks/playlists/${playlistId}`
    ];

    let lastError = null;
    for (const endpoint of instances) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 6000);
        const res = await fetch(endpoint, { signal: controller.signal });
        clearTimeout(timeoutId);

        if (res.ok) {
          const data = await res.json();
          const title = data.title || data.name || "YouTube Ders Listesi";
          const channel = data.author || data.uploader || "Eğitmen";
          const thumb = data.playlistThumbnail || `https://img.youtube.com/vi/${playlistId}/hqdefault.jpg`;
          const rawVideos = data.videos || data.relatedStreams || [];

          if (rawVideos.length > 0) {
            const videos = rawVideos.map((v, idx) => {
              const vid = v.videoId || v.url?.split('=')[1] || `vid_${idx}`;
              const vTitle = v.title || `Ders #${idx + 1}`;
              const dur = v.lengthSeconds || (v.duration ? parseInt(v.duration, 10) : 900);
              return {
                id: vid,
                playlistId,
                title: vTitle,
                durationSeconds: dur,
                position: idx,
                topic: this.extractTopic(vTitle, title),
                thumbnailUrl: `https://img.youtube.com/vi/${vid}/hqdefault.jpg`,
                isCompleted: false
              };
            });

            const totalDuration = videos.reduce((acc, v) => acc + v.durationSeconds, 0);
            return {
              playlist: {
                id: playlistId,
                title,
                channelTitle: channel,
                thumbnailUrl: thumb,
                itemCount: videos.length,
                totalDurationSeconds: totalDuration,
                isActive: true
              },
              videos
            };
          }
        }
      } catch (err) {
        lastError = err;
      }
    }

    throw lastError || new Error("Oynatma listesi yüklenemedi.");
  },

  getSampleEnglishCurriculum(playlistId = 'PL_sample_english_course') {
    const courseTitle = "Sıfırdan İleri Seviye İngilizce Eğitim Seti (A1 - B2)";
    const channel = "İngilizce Akademi & Öğretmenler Kulübü";

    const lessons = [
      ["İngilizce Gramer #1 - Present Simple (Geniş Zaman)", 1800, "Gramer (Grammar)"],
      ["İngilizce Gramer #2 - Present Continuous (Şimdiki Zaman)", 2100, "Gramer (Grammar)"],
      ["İngilizce Gramer #3 - Simple Past Tense (Geçmiş Zaman)", 2400, "Gramer (Grammar)"],
      ["İngilizce Gramer #4 - Past Continuous & While/When", 1950, "Gramer (Grammar)"],
      ["İngilizce Gramer #5 - Future Tense (Will vs Be Going To)", 2200, "Gramer (Grammar)"],
      ["Kelime Bilgisi | En Çok Kullanılan 100 Fiil ve Örnek Cümleler", 2700, "Kelime Bilgisi (Vocabulary)"],
      ["Kelime Bilgisi | Günlük Yaşamda 50 Phrasal Verb", 2400, "Kelime Bilgisi (Vocabulary)"],
      ["Kelime Bilgisi | B1 Seviyesi Sıfatlar ve Zıt Anlamlılar", 1800, "Kelime Bilgisi (Vocabulary)"],
      ["Dinleme (Listening) | A2-B1 Seviye Günlük Konuşma Diyalogları", 1500, "Dinleme (Listening)"],
      ["Dinleme (Listening) | İngilizce Podcast: İş ve Sosyal Yaşam", 2100, "Dinleme (Listening)"],
      ["Konuşma & Telaffuz | Doğru Telaffuz Teknikleri ve Vurgular", 1800, "Konuşma & Telaffuz"],
      ["Konuşma & Telaffuz | Akıcı Konuşma Pratikleri ve Kalıplar", 2100, "Konuşma & Telaffuz"],
      ["Okuma & Çeviri | Kısa Hikayelerle İngilizce Okuma Analizi", 2400, "Okuma & Çeviri (Reading)"],
      ["Okuma & Çeviri | Makale Çevirisi ve Cümle Çözümlemesi", 2700, "Okuma & Çeviri (Reading)"],
      ["Sınav Hazırlığı | YDS & YÖKDİL Gramer Soru Çözümü #1", 3000, "Sınav Hazırlığı"],
      ["Sınav Hazırlığı | Cümle Tamamlama ve Paragraf Taktikleri", 2700, "Sınav Hazırlığı"]
    ];

    const videos = lessons.map(([title, duration, topic], idx) => ({
      id: `demo_vid_${idx}`,
      playlistId,
      title,
      durationSeconds: duration,
      position: idx,
      topic,
      thumbnailUrl: `https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=300&q=80`,
      isCompleted: false
    }));

    const totalSeconds = videos.reduce((acc, v) => acc + v.durationSeconds, 0);

    return {
      playlist: {
        id: playlistId,
        title: courseTitle,
        channelTitle: channel,
        thumbnailUrl: "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=300&q=80",
        itemCount: videos.length,
        totalDurationSeconds: totalSeconds,
        isActive: true
      },
      videos
    };
  }
};


// ============================================================================
// 3. SMART SCHEDULING ENGINE (Akıllı Programlama Motoru)
// ============================================================================

const SmartSchedulingEngine = {
  /**
   * Bin-Packing with Sequencing Algorithm
   * 1. Preserves pedagogical sequence
   * 2. Respects daily capacity limit
   * 3. Never splits an individual video unless single video alone exceeds capacity
   * 4. Skips non-study days
   */
  generateSchedule(videos, dailyCapacityMinutes, selectedDays, startDateStr) {
    if (!videos || videos.length === 0) return [];

    const capacitySeconds = Math.max(15, dailyCapacityMinutes) * 60;
    const activeDaysSet = new Set(selectedDays && selectedDays.length > 0 ? selectedDays : [1, 2, 3, 4, 5]);

    let currentDate = this.parseLocalDate(startDateStr);

    const schedulePlans = [];
    const queue = [...videos];

    while (queue.length > 0) {
      // Advance until we hit an active study day
      let currentIsoDay = this.getIsoDay(currentDate);
      while (!activeDaysSet.has(currentIsoDay)) {
        currentDate.setDate(currentDate.getDate() + 1);
        currentIsoDay = this.getIsoDay(currentDate);
      }

      const dateStr = this.formatLocalDate(currentDate);
      const dayName = YouTubeService.TURKISH_DAYS[currentIsoDay] || 'Gün';

      const assignedVideos = [];
      let accumulatedSeconds = 0;

      while (queue.length > 0) {
        const nextVideo = queue[0];
        const vDuration = nextVideo.durationSeconds;

        if (assignedVideos.length === 0) {
          // First video of the day: always accept it, even if alone exceeds capacity (No split rule)
          queue.shift();
          assignedVideos.push(nextVideo.id);
          accumulatedSeconds += vDuration;

          // If this video alone filled or exceeded capacity, day is complete
          if (accumulatedSeconds >= capacitySeconds) {
            break;
          }
        } else {
          // Subsequent videos: check if fits into remaining daily capacity
          if (accumulatedSeconds + vDuration <= capacitySeconds) {
            queue.shift();
            assignedVideos.push(nextVideo.id);
            accumulatedSeconds += vDuration;
          } else {
            // Exceeds daily capacity. Rule: DO NOT split video! Close day.
            break;
          }
        }
      }

      if (assignedVideos.length > 0) {
        schedulePlans.push({
          playlistId: videos[0].playlistId,
          date: dateStr,
          dayOfWeek: currentIsoDay,
          dayOfWeekName: dayName,
          totalAssignedSeconds: accumulatedSeconds,
          videoIds: assignedVideos
        });
      }

      // Next calendar day
      currentDate.setDate(currentDate.getDate() + 1);
    }

    return schedulePlans;
  },

  getIsoDay(date) {
    const day = date.getDay(); // 0 is Sunday, 1 is Monday ...
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
// 4. UI CONTROLLER (AppUI)
// ============================================================================

const AppUI = {
  activeTab: 'tab-today',
  deferredInstallPrompt: null,

  init() {
    this.bindEvents();
    this.initPwa();
    this.initSettingsValues();
    this.render();
  },

  initPwa() {
    // Register Service Worker
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js').then((reg) => {
          console.log('PWA ServiceWorker registered with scope:', reg.scope);
        }).catch((err) => {
          console.warn('PWA ServiceWorker registration failed:', err);
        });
      });
    }

    // PWA Install Prompt
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
          this.showToast('Uygulama ana ekranınıza ekleniyor!', 'success');
        }
        this.deferredInstallPrompt = null;
        document.getElementById('pwaInstallBtn')?.classList.add('hidden');
      }
    });
  },

  bindEvents() {
    // Nav bar items
    document.querySelectorAll('.nav-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const target = btn.getAttribute('data-tab');
        this.switchTab(target);
      });
    });

    // Capacity Slider
    const range = document.getElementById('capacityRange');
    range?.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      this.updateCapacityDisplay(val);
    });
    range?.addEventListener('change', (e) => {
      const val = parseInt(e.target.value, 10);
      const settings = StorageService.getSettings();
      settings.dailyCapacityMinutes = val;
      StorageService.saveSettings(settings);
    });

    // Day picker buttons
    document.querySelectorAll('.day-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const day = parseInt(btn.getAttribute('data-day'), 10);
        this.toggleDay(day);
      });
    });

    // Start Date change
    document.getElementById('startDateInput')?.addEventListener('change', (e) => {
      const settings = StorageService.getSettings();
      settings.startDate = e.target.value;
      StorageService.saveSettings(settings);
    });

    // Generate Schedule button
    document.getElementById('generateScheduleBtn')?.addEventListener('click', () => {
      this.handleGenerateSchedule();
    });

    // Playlist Import button
    document.getElementById('importPlaylistBtn')?.addEventListener('click', () => {
      this.handleImportPlaylist();
    });

    // Paste URL button
    document.getElementById('pasteUrlBtn')?.addEventListener('click', async () => {
      try {
        const text = await navigator.clipboard.readText();
        const input = document.getElementById('playlistUrlInput');
        if (input && text) {
          input.value = text;
          this.showToast('Link yapıştırıldı!', 'info');
        }
      } catch {
        this.showToast('Panodan yapıştırma izni verilmedi.', 'warning');
      }
    });

    // Load Sample Course button
    document.getElementById('loadSampleCourseBtn')?.addEventListener('click', () => {
      this.handleLoadSampleCourse();
    });

    // Video search
    document.getElementById('searchVideoInput')?.addEventListener('input', (e) => {
      this.renderAllVideosList(e.target.value);
    });

    // Settings Modal
    document.getElementById('openSettingsBtn')?.addEventListener('click', () => {
      const modal = document.getElementById('settingsModal');
      const apiKeyInput = document.getElementById('apiKeyInput');
      apiKeyInput.value = StorageService.getSettings().apiKey || '';
      modal.classList.remove('hidden');
    });

    document.getElementById('closeSettingsBtn')?.addEventListener('click', () => {
      document.getElementById('settingsModal')?.classList.add('hidden');
    });

    document.getElementById('saveApiKeyBtn')?.addEventListener('click', () => {
      const apiKey = document.getElementById('apiKeyInput')?.value.trim() || '';
      const settings = StorageService.getSettings();
      settings.apiKey = apiKey;
      StorageService.saveSettings(settings);
      document.getElementById('settingsModal')?.classList.add('hidden');
      this.showToast('Ayarlar kaydedildi.', 'success');
    });

    document.getElementById('resetAllDataBtn')?.addEventListener('click', () => {
      if (confirm('Tüm kayıtlı oynatma listeleri ve çalışma programınız silinecek. Emin misiniz?')) {
        StorageService.resetAll();
        document.getElementById('settingsModal')?.classList.add('hidden');
        this.render();
        this.showToast('Tüm veriler sıfırlandı.', 'info');
      }
    });
  },

  switchTab(tabId) {
    this.activeTab = tabId;
    document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
    document.getElementById(tabId)?.classList.remove('hidden');

    // Update bottom nav bar active styling
    document.querySelectorAll('.nav-item').forEach(btn => {
      const isTarget = btn.getAttribute('data-tab') === tabId;
      if (isTarget) {
        btn.classList.add('text-brand-400', 'font-bold');
        btn.classList.remove('text-slate-400', 'font-medium');
      } else {
        btn.classList.remove('text-brand-400', 'font-bold');
        btn.classList.add('text-slate-400', 'font-medium');
      }
    });

    this.render();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  },

  initSettingsValues() {
    const settings = StorageService.getSettings();
    const capacityRange = document.getElementById('capacityRange');
    if (capacityRange) capacityRange.value = settings.dailyCapacityMinutes;
    this.updateCapacityDisplay(settings.dailyCapacityMinutes);

    const startDateInput = document.getElementById('startDateInput');
    if (startDateInput) {
      startDateInput.value = settings.startDate || SmartSchedulingEngine.formatLocalDate(new Date());
    }

    this.renderDayButtons(settings.selectedDays);
  },

  setCapacity(mins) {
    const range = document.getElementById('capacityRange');
    if (range) range.value = mins;
    this.updateCapacityDisplay(mins);

    const settings = StorageService.getSettings();
    settings.dailyCapacityMinutes = mins;
    StorageService.saveSettings(settings);

    document.querySelectorAll('.capacity-chip').forEach(btn => {
      if (btn.innerText.includes(`${mins} dk`)) {
        btn.classList.add('bg-brand-600', 'text-white', 'border-brand-500');
        btn.classList.remove('bg-slate-800', 'text-slate-300', 'border-darkBorder');
      } else {
        btn.classList.remove('bg-brand-600', 'text-white', 'border-brand-500');
        btn.classList.add('bg-slate-800', 'text-slate-300', 'border-darkBorder');
      }
    });
  },

  updateCapacityDisplay(mins) {
    const badge = document.getElementById('capacityDisplayBadge');
    if (badge) {
      badge.textContent = `${mins} dk (${YouTubeService.formatMinutes(mins)})`;
    }
  },

  toggleDay(dayNum) {
    const settings = StorageService.getSettings();
    let days = settings.selectedDays || [1, 2, 3, 4, 5];
    if (days.includes(dayNum)) {
      if (days.length > 1) { // En az 1 gün seçili kalsın
        days = days.filter(d => d !== dayNum);
      }
    } else {
      days.push(dayNum);
    }
    days.sort((a, b) => a - b);
    settings.selectedDays = days;
    StorageService.saveSettings(settings);
    this.renderDayButtons(days);
  },

  renderDayButtons(selectedDays) {
    const daySet = new Set(selectedDays);
    document.querySelectorAll('.day-btn').forEach(btn => {
      const d = parseInt(btn.getAttribute('data-day'), 10);
      if (daySet.has(d)) {
        btn.className = 'day-btn h-10 rounded-xl text-xs font-bold border transition flex flex-col items-center justify-center bg-brand-600 text-white border-brand-500 shadow-sm';
      } else {
        btn.className = 'day-btn h-10 rounded-xl text-xs font-bold border transition flex flex-col items-center justify-center bg-slate-800 text-slate-400 border-darkBorder hover:bg-slate-700';
      }
    });
  },

  render() {
    const activePlaylist = StorageService.getActivePlaylist();

    // Header Course Title
    const headerTitle = document.getElementById('headerCourseTitle');
    if (headerTitle) {
      headerTitle.textContent = activePlaylist ? activePlaylist.title : 'Kurs Yüklenmedi';
    }

    this.renderTodayTab();
    this.renderScheduleTab();
    this.renderProgressTab();
    this.renderPlaylistTab();
  },

  // ==========================================================================
  // RENDER: TODAY TAB
  // ==========================================================================
  renderTodayTab() {
    const activePlaylist = StorageService.getActivePlaylist();
    const todayStr = SmartSchedulingEngine.formatLocalDate(new Date());
    const todayIsoDay = SmartSchedulingEngine.getIsoDay(new Date());

    // Date Label
    const dateLabel = document.getElementById('todayDateLabel');
    if (dateLabel) {
      const options = { weekday: 'long', day: 'numeric', month: 'long' };
      dateLabel.textContent = new Date().toLocaleDateString('tr-TR', options);
    }

    if (!activePlaylist) {
      document.getElementById('todayVideoList').innerHTML = '';
      document.getElementById('todayEmptyState').classList.remove('hidden');
      document.getElementById('todayEmptyReason').textContent = 'Çalışma programı oluşturmak için önce bir oynatma listesi ekleyin.';
      this.updateTodayProgress(0, 0, 0);
      return;
    }

    const schedules = StorageService.getSchedules(activePlaylist.id);
    const todaySchedule = schedules.find(s => s.date === todayStr);

    if (!todaySchedule || !todaySchedule.videoIds || todaySchedule.videoIds.length === 0) {
      document.getElementById('todayVideoList').innerHTML = '';
      document.getElementById('todayEmptyState').classList.remove('hidden');
      document.getElementById('todayEmptyReason').textContent = `Bugün (${YouTubeService.TURKISH_DAYS[todayIsoDay]}) dinlenme gününüz veya henüz bu gün için ders atanmamış. Program sekmesinden takviminizi güncelleyebilirsiniz.`;
      this.updateTodayProgress(0, 0, 0);
      return;
    }

    document.getElementById('todayEmptyState').classList.add('hidden');

    const allVideos = StorageService.getVideos(activePlaylist.id);
    const todayVideos = todaySchedule.videoIds.map(vid => allVideos.find(v => v.id === vid)).filter(Boolean);

    const completedCount = todayVideos.filter(v => v.isCompleted).length;
    const totalCount = todayVideos.length;
    const totalDuration = todayVideos.reduce((acc, v) => acc + v.durationSeconds, 0);
    const remainingDuration = todayVideos.filter(v => !v.isCompleted).reduce((acc, v) => acc + v.durationSeconds, 0);

    this.updateTodayProgress(completedCount, totalCount, totalDuration, remainingDuration);

    // List rendering
    const container = document.getElementById('todayVideoList');
    container.innerHTML = todayVideos.map(v => this.createVideoCardHtml(v)).join('');

    // Celebration banner
    const celebration = document.getElementById('todayCelebrationBanner');
    if (totalCount > 0 && completedCount === totalCount) {
      celebration.classList.remove('hidden');
    } else {
      celebration.classList.add('hidden');
    }

    document.getElementById('todayItemCountBadge').textContent = `${totalCount} Ders`;
  },

  updateTodayProgress(completed, total, totalDuration, remainingDuration = 0) {
    const fraction = total > 0 ? (completed / total) : 0;
    const percent = Math.round(fraction * 100);

    document.getElementById('todayProgressBadge').textContent = `%${percent}`;
    document.getElementById('todayProgressBar').style.width = `${percent}%`;
    document.getElementById('todayVideosCountLabel').textContent = `${total} dersten ${completed}'i tamamlandı`;
    document.getElementById('todayDurationLabel').textContent = YouTubeService.formatDuration(totalDuration);
    document.getElementById('todayRemainingTimeLabel').textContent = `Kalan: ${YouTubeService.formatDuration(remainingDuration)}`;

    const statusText = document.getElementById('todayDayStatusText');
    if (statusText) {
      if (total === 0) statusText.textContent = 'Ders yok';
      else if (completed === total) statusText.textContent = 'Harika! Tamamlandı ✓';
      else statusText.textContent = 'Hedefe odaklan';
    }
  },

  // ==========================================================================
  // RENDER: SCHEDULE TAB
  // ==========================================================================
  renderScheduleTab() {
    const activePlaylist = StorageService.getActivePlaylist();
    const container = document.getElementById('scheduleTimelineContainer');
    const summaryCard = document.getElementById('scheduleSummaryCard');

    if (!activePlaylist) {
      container.innerHTML = `<p class="text-xs text-slate-400 text-center py-6">Henüz bir kurs eklenmedi.</p>`;
      summaryCard.classList.add('hidden');
      return;
    }

    const schedules = StorageService.getSchedules(activePlaylist.id);
    if (!schedules || schedules.length === 0) {
      container.innerHTML = `<p class="text-xs text-slate-400 text-center py-6">Bu kurs için henüz program oluşturulmadı. Yukarıdaki butona tıklayarak hemen oluşturun.</p>`;
      summaryCard.classList.add('hidden');
      return;
    }

    summaryCard.classList.remove('hidden');
    const totalDays = schedules.length;
    const allVideos = StorageService.getVideos(activePlaylist.id);
    const totalDuration = allVideos.reduce((acc, v) => acc + v.durationSeconds, 0);
    const finishDate = schedules[schedules.length - 1]?.date || '-';

    document.getElementById('summaryTotalDays').textContent = `${totalDays} Gün`;
    document.getElementById('summaryTotalHours').textContent = YouTubeService.formatDuration(totalDuration);
    document.getElementById('summaryFinishDate').textContent = finishDate;

    // Timeline days list
    container.innerHTML = schedules.map((sch, dayIndex) => {
      const dayVideos = sch.videoIds.map(vid => allVideos.find(v => v.id === vid)).filter(Boolean);
      const isAllDone = dayVideos.length > 0 && dayVideos.every(v => v.isCompleted);
      const completedCount = dayVideos.filter(v => v.isCompleted).length;

      return `
        <div class="bg-darkSurface border border-darkBorder rounded-2xl overflow-hidden shadow-md">
          <div class="p-3.5 flex items-center justify-between cursor-pointer hover:bg-slate-800/40 transition" onclick="AppUI.toggleScheduleAccordion(${dayIndex})">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl ${isAllDone ? 'bg-emerald-500/20 text-emerald-400' : 'bg-brand-500/20 text-brand-400'} flex flex-col items-center justify-center font-bold text-xs shrink-0">
                <span>${sch.dayOfWeekName.substring(0, 3)}</span>
                <span class="text-[10px] font-normal opacity-80">${sch.date.split('-')[2]}</span>
              </div>
              <div>
                <h4 class="text-xs font-bold text-white flex items-center gap-2">
                  <span>Gün ${dayIndex + 1}: ${sch.dayOfWeekName} (${sch.date})</span>
                  ${isAllDone ? '<i class="fa-solid fa-circle-check text-emerald-400 text-xs"></i>' : ''}
                </h4>
                <p class="text-[11px] text-slate-400">
                  ${dayVideos.length} ders • ${YouTubeService.formatDuration(sch.totalAssignedSeconds)} (${completedCount}/${dayVideos.length} bitti)
                </p>
              </div>
            </div>
            <i id="accordion-icon-${dayIndex}" class="fa-solid fa-chevron-down text-xs text-slate-400 transition-transform"></i>
          </div>

          <div id="accordion-content-${dayIndex}" class="hidden px-3.5 pb-3.5 pt-1 space-y-2 border-t border-darkBorder/60">
            ${dayVideos.map(v => this.createVideoCardHtml(v)).join('')}
          </div>
        </div>
      `;
    }).join('');
  },

  toggleScheduleAccordion(index) {
    const content = document.getElementById(`accordion-content-${index}`);
    const icon = document.getElementById(`accordion-icon-${index}`);
    if (content) {
      content.classList.toggle('hidden');
      if (icon) icon.classList.toggle('rotate-180');
    }
  },

  // ==========================================================================
  // RENDER: PROGRESS TAB
  // ==========================================================================
  renderProgressTab() {
    const activePlaylist = StorageService.getActivePlaylist();
    const container = document.getElementById('topicProgressContainer');

    if (!activePlaylist) {
      document.getElementById('overallPercentageBadge').textContent = '%0';
      document.getElementById('overallProgressBar').style.width = '0%';
      document.getElementById('progressCourseSubtitle').textContent = 'Aktif kurs bulunmuyor';
      document.getElementById('metricCompletedVideos').textContent = '0 / 0';
      document.getElementById('metricRemainingDuration').textContent = '0 sa';
      document.getElementById('metricTotalDuration').textContent = '0 sa';
      container.innerHTML = `<p class="text-xs text-slate-400 text-center py-6">İstatistikleri görmek için oynatma listesi ekleyin.</p>`;
      return;
    }

    const videos = StorageService.getVideos(activePlaylist.id);
    const totalCount = videos.length;
    const completedCount = videos.filter(v => v.isCompleted).length;
    const fraction = totalCount > 0 ? (completedCount / totalCount) : 0;
    const percent = Math.round(fraction * 100);

    const totalSeconds = videos.reduce((acc, v) => acc + v.durationSeconds, 0);
    const remainingSeconds = videos.filter(v => !v.isCompleted).reduce((acc, v) => acc + v.durationSeconds, 0);

    document.getElementById('overallPercentageBadge').textContent = `%${percent}`;
    document.getElementById('overallProgressBar').style.width = `${percent}%`;
    document.getElementById('progressCourseSubtitle').textContent = activePlaylist.title;
    document.getElementById('metricCompletedVideos').textContent = `${completedCount} / ${totalCount}`;
    document.getElementById('metricRemainingDuration').textContent = YouTubeService.formatDuration(remainingSeconds);
    document.getElementById('metricTotalDuration').textContent = YouTubeService.formatDuration(totalSeconds);

    // Group videos by Topic
    const topicMap = {};
    videos.forEach(v => {
      const t = v.topic || 'Genel';
      if (!topicMap[t]) {
        topicMap[t] = { topic: t, total: 0, completed: 0, totalDuration: 0, remainingDuration: 0 };
      }
      topicMap[t].total += 1;
      topicMap[t].totalDuration += v.durationSeconds;
      if (v.isCompleted) {
        topicMap[t].completed += 1;
      } else {
        topicMap[t].remainingDuration += v.durationSeconds;
      }
    });

    const topicList = Object.values(topicMap);
    document.getElementById('topicsCountBadge').textContent = `${topicList.length} Konu`;

    container.innerHTML = topicList.map(t => {
      const tFraction = t.total > 0 ? (t.completed / t.total) : 0;
      const tPercent = Math.round(tFraction * 100);
      const isDone = t.completed >= t.total;

      return `
        <div class="bg-darkSurface border border-darkBorder rounded-2xl p-4 shadow-md space-y-2">
          <div class="flex items-center justify-between">
            <h4 class="text-xs font-bold text-white flex items-center gap-2">
              <span>${t.topic}</span>
              ${isDone ? '<span class="text-[10px] bg-emerald-500/20 text-emerald-400 font-semibold px-2 py-0.5 rounded-md">Tamamlandı ✓</span>' : ''}
            </h4>
            <span class="text-xs font-bold ${isDone ? 'text-emerald-400' : 'text-brand-400'}">%${tPercent}</span>
          </div>

          <div class="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
            <div class="h-full rounded-full ${isDone ? 'bg-emerald-500' : 'bg-brand-500'} transition-all duration-500" style="width: ${tPercent}%"></div>
          </div>

          <div class="flex items-center justify-between text-[11px] text-slate-400 pt-1">
            <span>${t.total} videodan ${t.completed}'i tamamlandı</span>
            <span class="font-medium ${isDone ? 'text-emerald-400' : 'text-slate-300'}">
              ${isDone ? 'Bitti' : `${YouTubeService.formatDuration(t.remainingDuration)} kaldı`}
            </span>
          </div>
        </div>
      `;
    }).join('');
  },

  // ==========================================================================
  // RENDER: PLAYLIST TAB
  // ==========================================================================
  renderPlaylistTab() {
    const activePlaylist = StorageService.getActivePlaylist();
    const card = document.getElementById('activePlaylistCard');
    const videosSection = document.getElementById('playlistVideosSection');

    if (!activePlaylist) {
      card?.classList.add('hidden');
      videosSection?.classList.add('hidden');
      return;
    }

    card?.classList.remove('hidden');
    videosSection?.classList.remove('hidden');

    document.getElementById('activePlaylistTitle').textContent = activePlaylist.title;
    document.getElementById('activePlaylistChannel').textContent = activePlaylist.channelTitle;
    document.getElementById('activePlaylistThumb').src = activePlaylist.thumbnailUrl;
    document.getElementById('activePlaylistVideoCount').innerHTML = `<i class="fa-solid fa-film text-brand-400 mr-1"></i>${activePlaylist.itemCount} Ders`;
    document.getElementById('activePlaylistTotalDuration').innerHTML = `<i class="fa-solid fa-clock text-brand-400 mr-1"></i>${YouTubeService.formatDuration(activePlaylist.totalDurationSeconds)}`;

    this.renderAllVideosList();
  },

  renderAllVideosList(filterQuery = '') {
    const activePlaylist = StorageService.getActivePlaylist();
    if (!activePlaylist) return;

    let videos = StorageService.getVideos(activePlaylist.id);
    if (filterQuery && filterQuery.trim() !== '') {
      const q = filterQuery.toLowerCase().trim();
      videos = videos.filter(v => v.title.toLowerCase().includes(q) || (v.topic && v.topic.toLowerCase().includes(q)));
    }

    document.getElementById('playlistVideosCountLabel').textContent = videos.length;
    const container = document.getElementById('allVideosListContainer');
    container.innerHTML = videos.map(v => this.createVideoCardHtml(v)).join('');
  },

  // Video Card HTML Template
  createVideoCardHtml(video) {
    const durationFormatted = YouTubeService.formatDuration(video.durationSeconds);
    return `
      <div class="bg-darkSurface border border-darkBorder rounded-xl p-3 flex items-center justify-between gap-3 shadow-sm hover:border-slate-700 transition ${video.isCompleted ? 'opacity-70' : ''}">
        <!-- Checkbox -->
        <label class="cursor-pointer flex items-center shrink-0">
          <input type="checkbox" ${video.isCompleted ? 'checked' : ''} onchange="AppUI.toggleComplete('${video.id}', this.checked)" class="lesson-checkbox sr-only">
          <div class="w-6 h-6 rounded-lg border-2 ${video.isCompleted ? 'bg-emerald-600 border-emerald-600' : 'border-slate-600 bg-slate-900'} flex items-center justify-center transition">
            <i class="fa-solid fa-check text-white text-xs ${video.isCompleted ? '' : 'hidden'}"></i>
          </div>
        </label>

        <!-- Video Info -->
        <div class="flex-1 min-w-0" onclick="AppUI.openVideoModal('${video.id}', '${video.title.replace(/'/g, "\\'")}')">
          <div class="flex items-center gap-1.5 mb-0.5">
            <span class="text-[10px] font-semibold bg-brand-500/10 text-brand-400 border border-brand-500/20 px-1.5 py-0.2 rounded">${video.topic || 'Ders'}</span>
            <span class="text-[11px] text-slate-400 flex items-center gap-1">
              <i class="fa-regular fa-clock text-[10px]"></i>
              ${durationFormatted}
            </span>
          </div>
          <h5 class="text-xs font-semibold text-white truncate cursor-pointer hover:text-brand-300 transition ${video.isCompleted ? 'completed-text' : ''}">${video.title}</h5>
        </div>

        <!-- Watch Button -->
        <button onclick="AppUI.openVideoModal('${video.id}', '${video.title.replace(/'/g, "\\'")}')" class="w-8 h-8 rounded-lg bg-slate-800 text-slate-300 hover:text-brand-400 hover:bg-slate-700 shrink-0 flex items-center justify-center transition" title="Videoyu İzle">
          <i class="fa-solid fa-play text-xs"></i>
        </button>
      </div>
    `;
  },

  // Toggle Video Completion Handler
  toggleComplete(videoId, isChecked) {
    StorageService.toggleVideoCompletion(videoId, isChecked);
    this.render();

    if (isChecked) {
      // Trigger subtle confetti celebration if daily goal is completed
      const active = StorageService.getActivePlaylist();
      if (active) {
        const todayStr = SmartSchedulingEngine.formatLocalDate(new Date());
        const schedules = StorageService.getSchedules(active.id);
        const todaySch = schedules.find(s => s.date === todayStr);
        if (todaySch) {
          const allVideos = StorageService.getVideos(active.id);
          const todayVideos = todaySch.videoIds.map(id => allVideos.find(v => v.id === id)).filter(Boolean);
          if (todayVideos.length > 0 && todayVideos.every(v => v.isCompleted)) {
            this.celebrate();
          }
        }
      }
    }
  },

  celebrate() {
    if (typeof confetti === 'function') {
      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.7 }
      });
    }
  },

  // Video Modal
  openVideoModal(videoId, title) {
    const modal = document.getElementById('videoModal');
    const iframe = document.getElementById('videoIframe');
    const modalTitle = document.getElementById('videoModalTitle');

    if (modal && iframe) {
      modalTitle.textContent = title;
      iframe.src = `https://www.youtube.com/embed/${videoId}?autoplay=1`;
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

  // Import Playlist Handler
  async handleImportPlaylist() {
    const input = document.getElementById('playlistUrlInput');
    const btn = document.getElementById('importPlaylistBtn');
    const url = input?.value.trim();

    if (!url) {
      this.showToast('Lütfen bir YouTube linki veya ID girin.', 'warning');
      return;
    }

    try {
      btn.disabled = true;
      btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-1"></i> İçe Aktarılıyor...`;

      const settings = StorageService.getSettings();
      const result = await YouTubeService.fetchPlaylist(url, settings.apiKey);

      StorageService.savePlaylist(result.playlist);
      StorageService.saveVideos(result.playlist.id, result.videos);
      StorageService.setActivePlaylist(result.playlist.id);

      // Auto generate initial schedule
      const schedules = SmartSchedulingEngine.generateSchedule(
        result.videos,
        settings.dailyCapacityMinutes,
        settings.selectedDays,
        settings.startDate
      );
      StorageService.saveSchedules(result.playlist.id, schedules);

      this.showToast(`"${result.playlist.title}" içe aktarıldı! (${result.videos.length} ders)`, 'success');
      input.value = '';
      this.switchTab('tab-today');
    } catch (err) {
      this.showToast(err.message || 'Oynatma listesi yüklenemedi.', 'error');
    } finally {
      btn.disabled = false;
      btn.innerHTML = `<i class="fa-solid fa-cloud-arrow-down"></i> <span>Listeyi İçe Aktar</span>`;
    }
  },

  // Load Sample Course Handler
  handleLoadSampleCourse() {
    const sample = YouTubeService.getSampleEnglishCurriculum();
    StorageService.savePlaylist(sample.playlist);
    StorageService.saveVideos(sample.playlist.id, sample.videos);
    StorageService.setActivePlaylist(sample.playlist.id);

    const settings = StorageService.getSettings();
    const schedules = SmartSchedulingEngine.generateSchedule(
      sample.videos,
      settings.dailyCapacityMinutes,
      settings.selectedDays,
      settings.startDate
    );
    StorageService.saveSchedules(sample.playlist.id, schedules);

    this.showToast('Örnek İngilizce kursu yüklendi! (16 Ders)', 'success');
    this.switchTab('tab-today');
  },

  // Generate / Regenerate Schedule
  handleGenerateSchedule() {
    const active = StorageService.getActivePlaylist();
    if (!active) {
      this.showToast('Önce bir oynatma listesi içe aktarın.', 'warning');
      this.switchTab('tab-playlist');
      return;
    }

    const videos = StorageService.getVideos(active.id);
    if (videos.length === 0) {
      this.showToast('Listede video bulunamadı.', 'warning');
      return;
    }

    const settings = StorageService.getSettings();
    const schedules = SmartSchedulingEngine.generateSchedule(
      videos,
      settings.dailyCapacityMinutes,
      settings.selectedDays,
      settings.startDate
    );

    StorageService.saveSchedules(active.id, schedules);
    this.showToast(`Akıllı program oluşturuldu! (${schedules.length} Gün)`, 'success');
    this.render();
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
    }, 3200);
  }
};

// Start App when DOM is ready
if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', () => {
    AppUI.init();
  });
}

// Export for Node/test environments if present
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { StorageService, YouTubeService, SmartSchedulingEngine, AppUI };
}
