# Hesap v1.0.4 - Güncelleme Notları

## 📅 Yayın Tarihi: 19 Mart 2026

---

## 🇹🇷 Türkçe

### Yenilikler
• Reklam desteği eklendi - Ücretsiz kullanımın devamı için minimal banner reklamlar

### İyileştirmeler
• Android 15 tam uyumluluk sağlandı (Edge-to-Edge ekran desteği)
• Deprecated API'ler kaldırıldı ve modern API'lere geçiş yapıldı
• Uygulama performansı optimize edildi
• Genel kararlılık iyileştirmeleri yapıldı

### Hata Düzeltmeleri
• Sesli komut kısmi sonuç hatası düzeltildi - Konuşma sırasında erken "başarılı" durumu gösterilmesi sorunu giderildi
• Sesli komut sırasında gereksiz uyarı logları (TurkishParser spam) temizlendi
• Mikrofon butonu artık kısmi sonuç sırasında da aktif animasyonu gösteriyor

### Notlar
Hesap uygulamasını kullandığınız için teşekkürler! Geri bildirimleriniz için uygulama içi ayarlardan bize ulaşabilirsiniz.

---

## 🇬🇧 English

### What's New
• Ad support added - Minimal banner ads to support free usage

### Improvements
• Full Android 15 compatibility (Edge-to-Edge display support)
• Deprecated APIs removed and migrated to modern APIs
• App performance optimized
• General stability improvements

### Bug Fixes
• Fixed speech recognition partial result bug - Premature "success" state during speech input resolved
• Removed unnecessary warning log spam (TurkishParser) during voice commands
• Microphone button now shows active animation during partial speech results

### Notes
Thank you for using Hesap calculator! You can reach us through in-app settings for feedback.

---

## Play Console için Kısa Notlar (500 karakter limiti)

### Türkçe:
```
🆕 Yenilikler:
• Reklam desteği eklendi
• Android 15 tam uyumluluk
• Edge-to-Edge ekran desteği
• Sesli komut hata düzeltmeleri
• Performans iyileştirmeleri

Hesap'ı kullandığınız için teşekkürler!
```

### English:
```
🆕 What's New:
• Ad support added
• Full Android 15 compatibility
• Edge-to-Edge display support
• Voice command bug fixes
• Performance improvements

Thank you for using Hesap!
```

---

## Teknik Değişiklikler

### Düzeltilen Play Console Uyarıları:
1. ✅ `android.view.Window.setStatusBarColor` - Kaldırıldı
2. ✅ `android.view.Window.setNavigationBarColor` - Kaldırıldı
3. ✅ `android:statusBarColor` XML attribute - Kaldırıldı
4. ✅ `android:navigationBarColor` XML attribute - Kaldırıldı
5. ✅ Edge-to-Edge desteği - `enableEdgeToEdge()` + `WindowCompat` kullanılıyor

### Düzeltilen Sesli Komut Hataları:
6. ✅ `onPartialResults` → `SpeechState.PartialResult` kullanıyor (önceden yanlışlıkla `Success` atanıyordu)
7. ✅ `processSpokenText` içindeki gereksiz ikinci `parseExpression` çağrısı kaldırıldı
8. ✅ Yeni `SpeechState.PartialResult` durumu eklendi (konuşma sırasında kısmi metni gösterir)

---

## AAB Dosyası
**Dosya:** `hesap-v1.0.4-release.aab`
**Boyut:** ~12 MB
**Version Code:** 5
**Version Name:** 1.0.4
**Min SDK:** 26
**Target SDK:** 36
