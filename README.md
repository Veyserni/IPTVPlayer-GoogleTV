# IPTV Player — Google TV / Android TV

TCL Google TV ve diğer Android TV cihazları için native IPTV istemcisi prototipi.

## Özellikler
- M3U / M3U Plus URL yükleme
- Xtream Codes bilgileriyle M3U oluşturma
- Canlı / Film / Dizi sınıflandırması
- Google TV kumandasına uygun Compose for TV arayüzü
- Media3 / ExoPlayer ile oynatma
- HTTP ve HTTPS IPTV sunucuları
- Yeni yayın açılırken önceki oynatıcı oturumunu kapatma
- TV geri tuşuyla oynatıcıdan listeye dönüş

## Durum
Çekirdek M3U ve Xtream mantığı için smoke testleri mevcut. Gerçek APK derlemesi için Android SDK gerekir.
GitHub Actions workflow'u `.github/workflows/build-apk.yml` altında hazırdır.

## TCL kurulumu
Ayrıntılar: `INSTALL_TCL.md`

## Yerel test araçları
`tools/CoreSmokeTest.kt` ve `tools/ScaleSmokeTest.kt`

## v0.5.0
- Xtream hesapları giriş ekranında kullanıcı adıyla listelenir; kayıtlı hesaba tek tıkla giriş yapılır.
- Manuel Xtream giriş alanı yeni hesap eklemek için açık kalır.
- Birden fazla Xtream hesabı cihazda saklanabilir ve tek tek unutulabilir.
- Oynatıcıdaki “Liste” ve kanal adı katmanı 3 saniye sonra otomatik gizlenir.
