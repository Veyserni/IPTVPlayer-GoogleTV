# TCL Google TV kurulum

## 1. APK üret

### GitHub Actions ile (önerilen)
1. Bu klasörün içeriğini bir GitHub reposuna yükle.
2. GitHub'da **Actions** sekmesine gir.
3. **Build Google TV APK** iş akışını aç.
4. **Run workflow** düğmesine bas.
5. İşlem başarılı olunca çalışmanın altındaki **Artifacts** bölümünden `IPTVPlayer-GoogleTV-debug` dosyasını indir.
6. ZIP'in içinden `app-debug.apk` dosyasını çıkar.

### Android Studio ile
1. Android Studio'da bu klasörü aç.
2. Gradle Sync'in tamamlanmasını bekle.
3. **Build > Build APK(s)** seç.
4. APK: `app/build/outputs/apk/debug/app-debug.apk`

## 2. TCL Google TV'ye yükle

1. `app-debug.apk` dosyasını USB belleğe kopyala.
2. USB belleği TCL televizyona tak.
3. TV'de bir dosya yöneticisi aç.
4. APK'ya tıkla.
5. Google TV engellerse, ekrandaki yönlendirmeden ilgili dosya yöneticisi için **Bilinmeyen uygulamaları yükle** iznini aç.
6. Geri dönüp APK'yı tekrar aç ve **Yükle** seç.
7. Uygulama, uygulamalar listesinde **IPTV Player** olarak görünür.

## 3. İlk test

- M3U kullanıyorsan tam M3U URL'sini gir.
- Xtream Codes kullanıyorsan sunucu adresi, kullanıcı adı ve şifreyi gir.
- Sunucu adresini `http://...:port` veya `https://...:port` biçiminde gir.

> Bu debug APK test amaçlıdır. Daha sonra release signing eklenebilir.
