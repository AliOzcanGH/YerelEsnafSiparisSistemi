# Yerel Esnaf Sipariş Sistemi

Spring Boot tabanlı yerel esnaflar için online sipariş yönetim sistemi.  
> ℹ️ Tüm bilgilendirme bu README içindedir; ekstra `.md` dosyası yoktur.

## 🚀 Hızlı Başlangıç (30 sn)

1. Proje klasöründe bir terminal açın.
2. Aşağıdaki komutu çalıştırın:
   ```bash
   mvnw.cmd spring-boot:run
   ```
3. Tarayıcıdan `http://localhost:8080` adresine gidin.

> SQLite otomatik kullanıldığı için veritabanı kurulumu yapmanıza gerek yoktur. `yerel_esnaf_db.db` dosyası ilk çalıştırmada oluşturulur.

## 📦 Proje Teslimi ve Paketleme

1. `target/`, `.idea/`, `.vscode/` gibi IDE/build klasörlerini sil.
2. Aşağıdaki öğelerin pakette olduğundan emin ol:
   - `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`
   - `yerel_esnaf_db.db` (opsiyonel; yoksa uygulama oluşturur)
3. Klasörü RAR/ZIP yap ve teslim et.

### Teslim Öncesi Kontrol Listesi
- [ ] Uygulama kendi bilgisayarında çalışıyor mu?
- [ ] `src/main/resources/static/style.css` ve `script.js` mevcut mu?
- [ ] Tüm Thymeleaf şablonları `templates/` altında mı?

### Sunumda Altını Çizebileceğin Başlıklar
- Yerel esnaf & müşteri rolleri olan online sipariş sistemi.
- Kullanılan teknolojiler: Spring Boot, Spring Security, SQLite, Thymeleaf, Maven.
- Özellikler: ürün listeleme/arama, sepet & sipariş yönetimi, kuponlar, dashboard, admin paneli.
- Roller: CUSTOMER (müşteri), SHOP_OWNER (esnaf), ADMIN (yönetici).

## ❓ Sık Sorulanlar

- **Projeyi nasıl çalıştırırız?**  
  `mvnw.cmd spring-boot:run` komutunu çalıştır ve `http://localhost:8080`'u aç.

- **Veritabanı nedir?**  
  SQLite kullanıyoruz. `yerel_esnaf_db.db` dosyası otomatik oluşur; istersen paketine ekleyebilirsin.

- **Hangi roller var?**  
  Müşteri, Esnaf ve Admin. Admin oluşturmak için `http://localhost:8080/create-admin` adresini bir kez ziyaret et.

## 📋 Gereksinimler

- **Java 17 veya üzeri** (JDK)
- **Maven 3.6+**
- **SQLite** (Otomatik olarak dahil edilir, ekstra kurulum gerekmez!)

## 🚀 Kurulum Adımları

### 1. Veritabanı Kurulumu

**Hiçbir şey yapmanıza gerek yok!** SQLite kullanıyoruz ve veritabanı dosyası (`yerel_esnaf_db.db`) uygulama ilk çalıştırıldığında otomatik olarak proje klasöründe oluşturulacaktır.

### 2. Veritabanı Ayarları (İsteğe Bağlı)

`src/main/resources/application.properties` dosyasındaki ayarlar hazırdır. Eğer veritabanı dosyasının konumunu değiştirmek isterseniz:

```properties
spring.datasource.url=jdbc:sqlite:yerel_esnaf_db.db
```

Dosya adını veya yolunu değiştirebilirsiniz (örn: `jdbc:sqlite:./data/yerel_esnaf_db.db`).

### 3. Projeyi Derle

Terminal/Command Prompt'ta proje klasörüne gidin ve:

```bash
mvn clean install
```

veya Windows'ta:

```cmd
mvnw.cmd clean install
```

### 4. Uygulamayı Çalıştır

```bash
mvn spring-boot:run
```

veya Windows'ta:

```cmd
mvnw.cmd spring-boot:run
```

### 5. Uygulamaya Erişim

Tarayıcıda şu adresi açın:
```
http://localhost:8080
```

## İlk Kullanım

### Admin Kullanıcısı Oluşturma

Tarayıcıda şu adresi açın:
```
http://localhost:8080/create-admin
```

Bu otomatik olarak bir admin kullanıcısı oluşturur:
- **Kullanıcı Adı:** `admin`
- **Şifre:** `password`

### Test Kullanıcıları

1. Ana sayfadan **Kayıt Ol** butonuna tıklayın
2. Yeni kullanıcı oluşturun (Müşteri veya Esnaf rolü seçebilirsiniz)

## 📁 Proje Yapısı

```
yerel-esnaf-siparis/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/esnaf/yerel_esnaf_siparis/
│   │   │       ├── controller/    # REST ve Web Controller'ları
│   │   │       ├── model/          # Veritabanı Modelleri
│   │   │       ├── repository/     # Veritabanı Repository'leri
│   │   │       ├── service/        # İş Mantığı Servisleri
│   │   │       └── config/         # Yapılandırma Dosyaları
│   │   └── resources/
│   │       ├── static/              # CSS ve JavaScript dosyaları
│   │       │   ├── style.css
│   │       │   └── script.js
│   │       ├── templates/          # Thymeleaf HTML şablonları
│   │       └── application.properties
│   └── test/
├── pom.xml                          # Maven bağımlılık dosyası
└── README.md
```

## ⚙️ Yapılandırma

### Port Değiştirme

`application.properties` dosyasında:

```properties
server.port=8081
```

### Veritabanı Modu

- `spring.jpa.hibernate.ddl-auto=update` - Otomatik tablo oluşturur/günceller
- `spring.jpa.show-sql=true` - SQL sorgularını konsola yazdırır

## 🐛 Sorun Giderme

### Port Zaten Kullanılıyor Hatası

`application.properties` dosyasında port numarasını değiştirin:
```properties
server.port=8081
```

### Veritabanı Bağlantı Hatası

1. Proje klasöründe yazma izninizin olduğundan emin olun (veritabanı dosyası buraya yazılacak)
2. Daha önce oluşturulmuş bir veritabanı dosyası varsa ve bozuksa, silebilirsiniz (yeniden oluşturulacak)
3. `application.properties` dosyasındaki veritabanı URL'inin doğru olduğundan emin olun

### CSS/JS Dosyaları Yüklenmiyor

1. Projeyi temizleyip yeniden derleyin: `mvn clean install`
2. Tarayıcı cache'ini temizleyin (Ctrl+F5)
3. Static dosyaların `src/main/resources/static/` klasöründe olduğundan emin olun

### Sayfalar Görünmüyor veya Hatalı

1. Tüm template dosyalarının `src/main/resources/templates/` klasöründe olduğunu kontrol edin
2. Projeyi yeniden build edin: `mvn clean install`
3. Uygulamayı yeniden başlatın

## 📝 Notlar

- İlk çalıştırmada veritabanı tabloları otomatik oluşturulur
- Admin panelinden kuponlar ve kategoriler yönetilebilir
- Esnaf hesapları ürün ekleyebilir ve siparişleri yönetebilir
- Müşteri hesapları ürün satın alabilir ve siparişlerini görüntüleyebilir

## 🆘 Yardım

Herhangi bir sorun yaşarsanız:
1. Terminal'deki hata mesajlarını kontrol edin
2. Veritabanı bağlantısını kontrol edin
3. Port numarasının başka bir uygulama tarafından kullanılmadığından emin olun

