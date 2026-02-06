# Observer Watch

Android-приложение для автоматической детекции лиц через фронтальную камеру и отправки снимков в Telegram.

## Возможности

- Детекция лиц через ML Kit (standalone)
- Фоновая работа как foreground service
- Отправка фото в Telegram через Bot API
- Throttling: не чаще одного уведомления в 30 секунд
- Автоматическая конвертация YUV → JPEG

## Архитектура (DDD)

```
com.observerwatch/
├── MainActivity.kt                   — Permission gate, запуск сервиса
├── SettingsActivity.kt               — Ввод Telegram credentials
├── config/
│   └── AppConfig.kt                  — SharedPreferences (токен, chat ID, cooldown)
├── domain/
│   ├── camera/
│   │   ├── CameraFrameSource.kt      — Camera2 API lifecycle
│   │   └── ImageConverter.kt         — YUV → JPEG
│   ├── detection/
│   │   └── FaceDetector.kt           — ML Kit face detection
│   └── notification/
│       └── TelegramSender.kt         — Telegram Bot API (sendPhoto)
└── service/
    └── ObserverForegroundService.kt  — Foreground service, оркестрация
```

## Настройка

### 1. Telegram Bot

1. Создать бота через [@BotFather](https://t.me/BotFather)
2. Получить токен бота
3. Получить chat ID (отправить сообщение боту, затем проверить `https://api.telegram.org/bot<TOKEN>/getUpdates`)

### 2. Конфигурация

При первом запуске приложение откроет экран настроек, где нужно ввести Bot Token и Chat ID.

### 3. Сборка

```bash
./gradlew assembleDebug
```

### 4. Установка

```bash
./gradlew installDebug
```

## Тестирование

```bash
./gradlew test
```

## Требования

- Android 8.0+ (API 26)
- Фронтальная камера
- Доступ в интернет

## Как это работает

1. `MainActivity` проверяет наличие Telegram credentials → если нет, открывает `SettingsActivity`
2. Запрашивает разрешение на камеру, запускает `ObserverForegroundService`
3. `CameraFrameSource` захватывает кадры через Camera2 API
4. `FaceDetector` (ML Kit) анализирует каждый кадр
5. При обнаружении лица `ImageConverter` создаёт JPEG
6. `TelegramSender` отправляет фото в Telegram (не чаще раз в 30 сек)

## License

MIT
