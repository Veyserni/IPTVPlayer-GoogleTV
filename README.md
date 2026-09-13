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
- 3 saniyelik otomatik gizleme yalnızca oynatıcıdaki Liste ve kanal adı overlay'i için geçerlidir.
