# IPTVPlayer Google TV v0.6.0 — Streaming Catalog

Google TV / Android TV için IPTV istemcisi.

## v0.6.0
- Windows sürümüne daha yakın playlist yükleme davranışı.
- M3U artık tüm dosya bitene kadar bekletilmeden satır satır işlenir.
- İlk 250 öğe geldiğinde katalog açılır; liste arkada büyümeye devam eder.
- VLC/LibVLC User-Agent kullanılır.
- Xtream `player_api.php` doğrulaması girişin önünü kesmez; gerçek `get.php` playlist doğrudan açılır.
- Ağ bağlantısı listenin sonunda kesilirse alınmış kısım kullanılabilir kalır.
- Şifre alanı teşhis için görünürdür.
- Kayıtlı hesaplar, Spor > sağlayıcı alt menüsü ve oynatıcı overlay otomatik gizleme korunur.

## Build
GitHub Actions > Build Google TV APK.


## v0.6.1
- Streaming katalog yüklenirken ana sekme ve Spor alt kategori seçimi artık yeni batch geldiğinde sıfırlanmaz.
- 2 saniyelik otomatik gizleme yalnızca oynatıcıdaki Liste ve kanal adı overlay'i için geçerlidir.

## v0.6.3 — Neon Mor Okunaklı Arayüz
- Giriş ekranı neon mor, yüksek kontrastlı TV tasarımına geçirildi.
- Spor/kategori ekranı sol menü + üst alt-kategori şeritleri + iki sütun okunaklı kanal kartları şeklinde yenilendi.
- Kanal arama alanı eklendi.
- Oynatıcı ekranına neon mor özel kontrol katmanı eklendi: 10 sn geri/ileri, oynat/duraklat, ses ve ayarlar.
- Oynatıcı kontrolleri 2 saniye sonra otomatik gizlenir; kumanda etkileşiminde tekrar görünür.
