# IPTVPlayer Google TV — Streaming Catalog

Google TV / Android TV için M3U ve Xtream Codes destekli IPTV istemcisi.

## Özellikler
- M3U / M3U Plus URL desteği.
- Xtream Codes ile giriş.
- Başarılı Xtream girişinden sonra hesabı otomatik kaydetme.
- Kayıtlı hesaba tek OK basışıyla hızlı giriş.
- Spor kategorileri ve sağlayıcı alt grupları.
- Liste tamamen bitmeden ilk kanallar geldikçe kullanıma açılan streaming katalog.
- TV kumandasına uygun neon mor, yüksek kontrastlı arayüz.
- İki sütun okunaklı kanal kartları ve kanal arama.
- Kanal adlarında iki satır + odakta kayan yazı.
- Oynatıcıda 10 sn geri/ileri, oynat/duraklat, ses ve ayarlar.
- Oynatıcı kontrolleri 2 saniye sonra otomatik gizlenir.

---

# 📺 Google TV / Android TV'ye Kurulum

Uygulamayı televizyona kurmanın en kolay yolu **Downloader** uygulamasını kullanmaktır.

## 1. Downloader uygulamasını yükleyin

1. Televizyonda **Google Play Store**'u açın.
2. Arama bölümüne **Downloader** yazın.
3. Downloader uygulamasını yükleyin ve açın.

> Menü isimleri TV markasına ve Android/Google TV sürümüne göre biraz değişebilir.

## 2. Downloader'a APK bağlantısını yazın

Downloader ana ekranındaki URL kutusuna aşağıdaki bağlantıyı yazın:

```text
https://github.com/Veyserni/IPTVPlayer-GoogleTV/releases/latest/download/app-debug.apk
```

Bu adres sabittir. GitHub'da yeni bir sürüm **Latest** olarak yayınlandığında aynı bağlantı en son APK dosyasını indirir.

Uzun bağlantıyı TV kumandasıyla yazmak istemiyorsanız bağlantıyı bir kısa-link servisiyle kısaltıp Downloader'a kısa adresi de yazabilirsiniz. Ancak kısa-link servisleri kapanabildiği veya bağlantıyı devre dışı bırakabildiği için en güvenilir adres yukarıdaki doğrudan GitHub bağlantısıdır.

## 3. APK'yı kurun

1. Downloader indirmeyi tamamlayınca **Install / Yükle** seçeneğine basın.
2. TV ilk kurulumda Downloader'a APK yükleme izni isteyebilir.
3. İzin verilmediyse genellikle şu bölüme gidilir:

```text
Ayarlar → Uygulamalar → Özel uygulama erişimi → Bilinmeyen uygulamaları yükle → Downloader → İzin ver
```

4. Downloader'a dönüp **Yükle** seçeneğine tekrar basın.
5. Kurulum tamamlanınca **Aç** seçeneğiyle IPTV Player'ı başlatın.

---

# 🔗 APK İndirme Linkini Kendiniz Nasıl Bulabilirsiniz?

GitHub'dan doğrudan APK bağlantısını almak için:

1. Bu GitHub deposunun ana sayfasına girin.
2. Sağ taraftaki **Releases** bölümüne tıklayın.
3. En üstte **Latest** yazan sürümü açın.
4. **Assets** bölümünü açın.
5. `app-debug.apk` dosyasını bulun.
6. Bilgisayarda dosyaya sağ tıklayıp **Bağlantı adresini kopyala** seçeneğini kullanabilirsiniz.

Belirli bir sürümün bağlantısı şu yapıda olur:

```text
https://github.com/Veyserni/IPTVPlayer-GoogleTV/releases/download/vX.X.X/app-debug.apk
```

Örneğin sürüm `v0.6.11` ise:

```text
https://github.com/Veyserni/IPTVPlayer-GoogleTV/releases/download/v0.6.11/app-debug.apk
```

Her zaman son sürümü indirmek için ise sürüm numarası yazmadan şu sabit adresi kullanın:

```text
https://github.com/Veyserni/IPTVPlayer-GoogleTV/releases/latest/download/app-debug.apk
```

---

# 🚀 İlk Açılış ve Giriş

Uygulama iki farklı giriş yöntemi sunar.

## Xtream Codes

1. **Yeni hesapla giriş** seçeneğini açın.
2. IPTV sağlayıcınızın verdiği bilgileri girin:
   - **Sunucu** — örnek: `http://sunucuadresi:8080`
   - **Kullanıcı adı**
   - **Şifre**
3. **Giriş Yap** seçeneğine basın.
4. Başarılı girişten sonra hesap otomatik kaydedilir.
5. Uygulamayı sonraki açışınızda kayıtlı hesabınız Xtream Codes bölümünün üstünde görünür.
6. Kayıtlı hesabın üzerine gelip kumandadaki **OK** tuşuna bir kez basmanız yeterlidir; doğrudan giriş yapılır.

## M3U

1. IPTV sağlayıcınızdan aldığınız M3U / M3U Plus bağlantısını **M3U URL** alanına yazın.
2. **Listeyi Aç** seçeneğine basın.

---

# ⬆️ Yeni Sürüme Güncelleme

Yeni sürüm yayınlandığında uygulamayı güncellemek için tekrar Downloader'ı açıp aynı sabit bağlantıyı kullanabilirsiniz:

```text
https://github.com/Veyserni/IPTVPlayer-GoogleTV/releases/latest/download/app-debug.apk
```

Yeni APK'yı indirin ve mevcut uygulamanın üzerine kurun. Android TV normalde uygulama verilerini ve kayıtlı hesapları koruyarak güncelleme yapar.

> Güncelleme sırasında Android “Bu uygulamayı güncellemek istiyor musunuz?” benzeri bir onay gösterebilir; **Güncelle / Update** seçeneğini seçin.

---

# Build

GitHub Actions → **Build Google TV APK**.

---

## Sürüm Notları

### v0.6.0
- Windows sürümüne daha yakın streaming playlist yükleme davranışı.
- M3U satır satır işlenir; ilk öğeler geldikçe katalog açılır.
- VLC/LibVLC User-Agent kullanılır.

### v0.6.1
- Streaming katalog yüklenirken seçili ana kategori ve Spor alt kategorisi korunur.

### v0.6.4 — Neon Mor Okunaklı Arayüz
- Giriş ve kategori ekranları TV için yüksek kontrastlı neon mor tasarıma geçirildi.
- İki sütun kanal kartları ve kanal arama eklendi.
- Oynatıcıya özel kontrol katmanı eklendi.

### v0.6.6
- TV kumandasında kısa OK basışı iyileştirildi.
- Kanal adları için iki satır + odakta kayan yazı eklendi.
- Oynatıcı kontrolleri küçültüldü.

### v0.6.7+
- Başarılı Xtream girişleri otomatik ve kalıcı kaydedilir.
- Kayıtlı hesapla hızlı giriş eklendi.
- Giriş ekranında kayıtlı hesaplar öncelikli hale getirildi.
- Liste yüklenmeye devam ederken kategori ve kanal etkileşimleri kullanılabilir hale getirildi.
- Android TV klavyesinin girişten sonra açık kalmaması için focus/IME davranışı iyileştirildi.

