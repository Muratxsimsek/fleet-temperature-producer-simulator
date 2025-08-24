# VHF Simulator - Fleet Temperature Dashboard

## Proje Açıklaması

VHF Simulator, Fleet Temperature Dashboard projesi için gerçekçi VHF mesajları üreten ve Kafka'ya gönderen bir simülasyon uygulamasıdır. Bu uygulama, uçak kabin sıcaklık sensörlerinden gelen verileri simüle eder ve yüksek performanslı Kafka producer kullanarak mesajları iletir.

## Özellikler

### 🚀 Simülasyon Modları
- **LOW**: 100 mesaj/saniye
- **MEDIUM**: 1000 mesaj/saniye  
- **HIGH**: 5000 mesaj/saniye
- **VARIABLE**: Değişken hız (100-5000 mesaj/saniye)

### ✈️ Gerçekçi Veri Üretimi
- 24 farklı Türk Hava Yolları uçak ID'si
- Boeing ve Airbus uçak modelleri
- 3 farklı kabin bölgesi (FWD, MID, AFT)
- Gerçekçi sıcaklık dağılımı (18-32°C normal, 28-35°C kritik)
- Sıcaklık trend analizi ve tutarlılık

### 📊 Kafka Producer Optimizasyonları
- Batch processing (16KB batch size)
- Snappy compression
- Idempotence desteği
- Retry mekanizması
- High throughput ayarları

### 🔧 REST API
- Simülasyon başlatma/durdurma
- Tek mesaj ve batch gönderimi
- İstatistik görüntüleme
- Durum takibi

## Teknik Gereksinimler

- Java 21
- Spring Boot 3.2.0
- Apache Kafka
- Maven 3.8+

## Kurulum

### 1. Proje Klonlama
```bash
git clone <repository-url>
cd vhf-simulator
```

### 2. Bağımlılıkları Yükleme
```bash
mvn clean install
```

### 3. Kafka Başlatma
```bash
# Docker Compose ile Kafka ve Zookeeper başlatma
docker-compose up -d zookeeper kafka
```

### 4. Uygulamayı Çalıştırma
```bash
mvn spring-boot:run
```

Uygulama `http://localhost:8081` adresinde çalışacaktır.

## Kullanım

### Simülasyon Kontrolü

#### Simülasyon Başlatma
```bash
# 1000 mesaj/saniye ile başlat
curl -X POST "http://localhost:8081/api/vhf-simulator/start?mode=MEDIUM"

# 5000 mesaj/saniye ile başlat
curl -X POST "http://localhost:8081/api/vhf-simulator/start?mode=HIGH"

# Değişken hız ile başlat
curl -X POST "http://localhost:8081/api/vhf-simulator/start?mode=VARIABLE"
```

#### Simülasyon Durdurma
```bash
curl -X POST "http://localhost:8081/api/vhf-simulator/stop"
```

#### Durum Kontrolü
```bash
curl "http://localhost:8081/api/vhf-simulator/status"
```

### Mesaj Gönderimi

#### Tek Mesaj
```bash
curl -X POST "http://localhost:8081/api/vhf-simulator/send-message"
```

#### Batch Mesaj
```bash
# 1000 mesaj gönder
curl -X POST "http://localhost:8081/api/vhf-simulator/send-batch?count=1000"
```

### İstatistikler

#### Producer İstatistikleri
```bash
curl "http://localhost:8081/api/vhf-simulator/stats"
```

#### İstatistikleri Sıfırlama
```bash
curl -X POST "http://localhost:8081/api/vhf-simulator/reset-stats"
```

#### Mevcut Modlar
```bash
curl "http://localhost:8081/api/vhf-simulator/modes"
```

## API Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|-----------|
| POST | `/api/vhf-simulator/start` | Simülasyon başlatma |
| POST | `/api/vhf-simulator/stop` | Simülasyon durdurma |
| GET | `/api/vhf-simulator/status` | Simülasyon durumu |
| POST | `/api/vhf-simulator/send-message` | Tek mesaj gönderimi |
| POST | `/api/vhf-simulator/send-batch` | Batch mesaj gönderimi |
| GET | `/api/vhf-simulator/stats` | Producer istatistikleri |
| POST | `/api/vhf-simulator/reset-stats` | İstatistikleri sıfırlama |
| GET | `/api/vhf-simulator/modes` | Mevcut simülasyon modları |
| GET | `/api/vhf-simulator/health` | Sağlık kontrolü |

## Konfigürasyon

### application.yml
```yaml
spring:
  application:
    name: vhf-simulator
  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8081

kafka:
  topic:
    vhf-messages: fleet.cabin_temperature.raw

simulator:
  default-mode: MEDIUM
  aircraft-count: 24
  temperature:
    normal-range:
      min: 18.0
      max: 32.0
    warning-threshold: 25.0
    critical-threshold: 28.0
```

### Kafka Producer Ayarları
- **Batch Size**: 16KB
- **Compression**: Snappy
- **Acks**: All
- **Retries**: 3
- **Idempotence**: Enabled

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Sağlık durumu
- `/actuator/info` - Uygulama bilgileri
- `/actuator/metrics` - Metrikler

### Logging
Uygulama detaylı loglama yapar:
- Simülasyon başlangıç/bitiş
- Mesaj gönderim durumları
- Hata durumları
- Performans metrikleri

## Performans

### Test Senaryoları
- **Düşük Yük**: 100 msg/s - ~1ms latency
- **Orta Yük**: 1000 msg/s - ~5ms latency  
- **Yüksek Yük**: 5000 msg/s - ~20ms latency
- **Değişken Yük**: 100-5000 msg/s - Dinamik latency

### Optimizasyonlar
- Asenkron mesaj işleme
- Batch processing
- Connection pooling
- Memory buffer optimization

## Geliştirme

### Proje Yapısı
```
src/main/java/com/fleet/temperature/
├── VHFSimulatorApplication.java
├── config/
│   └── KafkaConfig.java
├── controller/
│   └── VHFSimulatorController.java
├── dto/
│   └── VHFMessageSimDto.java
└── service/
    ├── AircraftDataGenerator.java
    ├── KafkaProducerService.java
    ├── VHFMessageGenerator.java
    └── VHFSimulatorService.java
```

### Yeni Özellik Ekleme
1. Service katmanında yeni metod ekleme
2. Controller'da endpoint tanımlama
3. DTO'da gerekli alanları ekleme
4. Test yazma

## Troubleshooting

### Yaygın Sorunlar

#### Kafka Bağlantı Hatası
```bash
# Kafka durumunu kontrol et
docker ps | grep kafka

# Topic'leri listele
docker exec -it fleet-temperature-kafka kafka-topics --list --bootstrap-server localhost:9092
```

#### Port Çakışması
```bash
# Port kullanımını kontrol et
lsof -i :8081

# Farklı port kullan
server.port: 8082
```

#### Memory Sorunları
```bash
# JVM heap size artır
export JAVA_OPTS="-Xmx2g -Xms1g"
```

## Gelecek Geliştirmeler

- [ ] Web UI dashboard
- [ ] Grafana metrik entegrasyonu
- [ ] Çoklu Kafka cluster desteği
- [ ] Mesaj şema validasyonu
- [ ] Otomatik test senaryoları
- [ ] Load balancing
- [ ] Kubernetes deployment
- [ ] Prometheus metrik export

## Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## İletişim

Proje ile ilgili sorularınız için:
- GitHub Issues
- Email: [email]
- Slack: [slack-channel]
