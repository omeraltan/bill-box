# Bill Box

Faturaların profesyonel kasası: arşiv, vade takibi, istatistik ve uyarılar.

## Teknoloji

- Java 25, Spring Boot 4.1.1, PostgreSQL, Flyway
- React 19, PrimeReact 10, TypeScript, Vite
- Arayüz dili: Türkçe

PrimeReact 11 lisans anahtarı istediği için MIT lisanslı PrimeReact 10 kullanıldı.

## Çalıştırma

```bash
docker compose up -d

export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home"
cd backend && mvn spring-boot:run

cd frontend && npm install && npm run dev
```

- API: http://localhost:8080
- Arayüz: http://localhost:5173

İlk kullanımda `/kayit` üzerinden e-posta ve şifre ile kasa oluşturun.

## Kapsam

- Gelir ve gider faturaları
- Cari, kategori, kalem, belge eki
- PDF/XML metin okuma (OCR benzeri alan doldurma)
- UBL-TR içe aktarma
- Dashboard, rapor, CSV
- Vade / gecikme / yüksek tutar uyarıları
- Tekrarlayan faturalar ve bütçeler
- Organizasyon + üyelik altyapısı (ev/ekip için hazır)
