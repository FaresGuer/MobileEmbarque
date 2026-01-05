# شرح شامل بالعربية - Module 4: Gesture Control & Safety Interaction

## 📚 الفهرس
1. [GestureDetector.java - كشف الإيماءات](#1-gesturedetectorjava)
2. [GestureService.java - الخدمة في الخلفية](#2-gestureservicejava)
3. [ControlFragment.java - واجهة التحكم](#3-controlfragmentjava)
4. [SOSUtil.java - إرسال التنبيهات](#4-sosutiljava)
5. [FlashlightUtil.java - التحكم في الفلاش](#5-flashlightutiljava)

---

## 1. GestureDetector.java

### 📖 الوصف العام
هذا الكلاس هو **القلب النابض** لكشف الإيماءات. يستمع للمستشعرات (Sensors) ويكتشف الحركات.

### 🔧 المتغيرات (Variables)

```java
private static final float SHAKE_THRESHOLD = 8.0f;
```
**الشرح:** العتبة (Threshold) للهز. إذا كان التغيير في التسارع أكبر من 8.0، يعتبر هزة.

```java
private static final long SHAKE_TIME_WINDOW = 1000;
```
**الشرح:** النافذة الزمنية للهزتين (1000ms = ثانية واحدة). يجب أن تحدث الهزتان خلال هذه المدة.

```java
private long lastShakeTime = 0;
private int shakeCount = 0;
```
**الشرح:** 
- `lastShakeTime`: وقت آخر هزة
- `shakeCount`: عدد الهزات المكتشفة

### 📝 Methods الرئيسية

#### `public GestureDetector(Context ctx)`
**الوصف:** Constructor - يُنشئ الكائن ويجهز المستشعرات

**الكود:**
```java
sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
```

**الشرح:**
1. يحصل على `SensorManager` من النظام
2. يحصل على Gyroscope (للكشف عن الإمالة)
3. يحصل على Accelerometer (للكشف عن الهز)
4. يحصل على Proximity (للكشف عن موجة اليد)

---

#### `public void start()`
**الوصف:** يبدأ الاستماع للمستشعرات

**الكود:**
```java
sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
```

**الشرح:**
- `registerListener(this, ...)`: يسجل هذا الكلاس كـ listener للمستشعر
- `SENSOR_DELAY_UI`: سرعة التحديث (مناسبة للواجهة)

**ماذا يحدث:**
- عند تحريك الهاتف، يستدعي النظام `onSensorChanged()`

---

#### `public void onSensorChanged(SensorEvent event)`
**الوصف:** يتم استدعاؤه تلقائياً عند تغيير المستشعر

**الكود:**
```java
int type = event.sensor.getType();
if (type == Sensor.TYPE_ACCELEROMETER) {
    handleAccelerometerEvent(event);
} else if (type == Sensor.TYPE_GYROSCOPE) {
    handleGyroscopeEvent(event);
} else if (type == Sensor.TYPE_PROXIMITY) {
    handleProximityEvent(event);
}
```

**الشرح:**
1. يحدد نوع المستشعر
2. يوجه الحدث للدالة المناسبة

---

#### `private void handleAccelerometerEvent(SensorEvent event)`
**الوصف:** يعالج أحداث Accelerometer (للكشف عن الهز)

**الكود:**
```java
float x = event.values[0];  // التسارع في المحور X
float y = event.values[1];  // التسارع في المحور Y
float z = event.values[2];  // التسارع في المحور Z

// حساب التغيير من القراءة السابقة
float deltaX = Math.abs(x - lastAccelValues[0]);
float deltaY = Math.abs(y - lastAccelValues[1]);
float deltaZ = Math.abs(z - lastAccelValues[2]);

// حساب التغيير الكلي
float delta = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
```

**الشرح:**
1. **قراءة القيم:** يحصل على التسارع في المحاور الثلاثة
2. **حساب التغيير:** يطرح القيم الحالية من السابقة
3. **حساب المقدار:** يستخدم نظرية فيثاغورس لحساب التغيير الكلي

**كشف الهز:**
```java
if (delta > SHAKE_THRESHOLD) {
    // تم اكتشاف هزة!
    if (currentTime - lastShakeTime < SHAKE_TIME_WINDOW) {
        shakeCount++;
        if (shakeCount >= 2) {
            // هزتان متتاليتان! → SOS
            listener.onDoubleShakeDetected();
        }
    }
}
```

**المنطق:**
- إذا كان التغيير > 8.0 → هزة واحدة
- إذا حدثت هزة ثانية خلال ثانية → Double Shake → SOS

---

#### `private void handleGyroscopeEvent(SensorEvent event)`
**الوصف:** يعالج أحداث Gyroscope (للكشف عن الإمالة)

**الكود:**
```java
float x = event.values[0];  // السرعة الزاوية في المحور X
float y = event.values[1];  // السرعة الزاوية في المحور Y
float z = event.values[2];  // السرعة الزاوية في المحور Z

// حساب التغيير
float deltaX = Math.abs(x - lastGyroValues[0]);
float deltaY = Math.abs(y - lastGyroValues[1]);
float deltaZ = Math.abs(z - lastGyroValues[2]);

float maxDelta = Math.max(Math.max(deltaX, deltaY), deltaZ);
```

**الشرح:**
- Gyroscope يقيس **السرعة الزاوية** (سرعة الدوران)
- عند إمالة الهاتف، تتغير القيم
- إذا كان التغيير > 8.0 → إمالة واحدة
- إذا حدثت إمالة ثانية خلال ثانية → Double Tilt → إرسال GPS

---

#### `private void handleProximityEvent(SensorEvent event)`
**الوصف:** يعالج أحداث Proximity (للكشف عن موجة اليد)

**الكود:**
```java
float distance = event.values[0];  // المسافة بالـ cm

// التحقق من القرب
boolean isClose = (distance < PROXIMITY_WAVE_THRESHOLD);

if (isClose && !wasClose) {
    // اليد اقتربت للتو
    wasClose = true;
    lastProximityCloseTime = currentTime;
} else if (!isClose && wasClose) {
    // اليد ابتعدت - تحقق إذا كانت موجة سريعة
    if (currentTime - lastProximityCloseTime < PROXIMITY_WAVE_TIME) {
        // موجة سريعة! → تشغيل الفلاش
        listener.onHandWaveDetected();
    }
}
```

**الشرح:**
1. **الاقتراب:** إذا كانت المسافة < 2cm → اليد قريبة
2. **الابتعاد:** إذا ابتعدت اليد بسرعة (< 300ms) → موجة يد
3. **الإجراء:** تشغيل/إيقاف الفلاش

---

## 2. GestureService.java

### 📖 الوصف العام
خدمة تعمل في **الخلفية** حتى عند قفل الشاشة. تستمع للإيماءات وتنفذ الإجراءات.

### 📝 Methods الرئيسية

#### `public void onCreate()`
**الوصف:** يتم استدعاؤه عند إنشاء الخدمة

**الكود:**
```java
gestureDetector = new GestureDetector(this);
gestureDetector.setListener(this);
FlashlightUtil.initialize(this);
```

**الشرح:**
1. ينشئ `GestureDetector`
2. يربط نفسه كـ listener
3. يهيئ الفلاش

---

#### `public int onStartCommand(Intent intent, int flags, int startId)`
**الوصف:** يتم استدعاؤه عند بدء الخدمة

**الكود:**
```java
startForeground(NOTIFICATION_ID, createNotification());
gestureDetector.start();
isRunning = true;
return START_STICKY;
```

**الشرح:**
1. **startForeground():** يجعل الخدمة في المقدمة (Foreground Service)
   - **لماذا؟** لأن Android يقتل الخدمات في الخلفية
   - **الحل:** إشعار دائم في شريط الإشعارات
2. **gestureDetector.start():** يبدأ الاستماع للمستشعرات
3. **START_STICKY:** إذا قُتلت الخدمة، يعيد النظام تشغيلها تلقائياً

---

#### `public void onDoubleShakeDetected()`
**الوصف:** يتم استدعاؤه عند كشف الهز مرتين

**الكود:**
```java
SOSUtil.sendSOSAlert(this);
```

**الشرح:**
- يستدعي `SOSUtil` لإرسال تنبيه SOS

---

#### `public void onHandWaveDetected()`
**الوصف:** يتم استدعاؤه عند كشف موجة اليد

**الكود:**
```java
FlashlightUtil.toggleFlashlight(this);
```

**الشرح:**
- يستدعي `FlashlightUtil` لتشغيل/إيقاف الفلاش

---

#### `public void onDoubleTiltDetected()`
**الوصف:** يتم استدعاؤه عند كشف الإمالة مرتين

**الكود:**
```java
SOSUtil.sendGPSCoordinates(this);
```

**الشرح:**
- يستدعي `SOSUtil` لإرسال الإحداثيات فقط

---

## 3. ControlFragment.java

### 📖 الوصف العام
Fragment يعرض واجهة المستخدم للتحكم في Gesture Control.

### 📝 Methods الرئيسية

#### `public View onCreateView(...)`
**الوصف:** ينشئ الواجهة

**الكود:**
```java
switchGestureService.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (isUpdatingSwitch) return;  // تجاهل التحديثات البرمجية
    
    if (isChecked) {
        startGestureService();
    } else {
        stopGestureService();
    }
});
```

**الشرح:**
- عند تفعيل/إلغاء تفعيل الـ Switch:
  - إذا `isChecked = true` → يبدأ الخدمة
  - إذا `isChecked = false` → يوقف الخدمة
- `isUpdatingSwitch`: يمنع الحلقة اللانهائية عند التحديث البرمجي

---

#### `private void startGestureService()`
**الوصف:** يبدأ GestureService

**الكود:**
```java
// 1. التحقق من الصلاحيات
if (!checkPermissions()) {
    requestPermissions();
    return;
}

// 2. بدء الخدمة
Intent serviceIntent = new Intent(getActivity(), GestureService.class);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    getActivity().startForegroundService(serviceIntent);
} else {
    getActivity().startService(serviceIntent);
}
```

**الشرح:**
1. **التحقق من الصلاحيات:** إذا لم تكن ممنوحة، يطلبها
2. **إنشاء Intent:** يحدد الخدمة المطلوب بدؤها
3. **بدء الخدمة:**
   - Android 8.0+ → `startForegroundService()`
   - Android قديم → `startService()`

---

#### `private boolean checkPermissions()`
**الوصف:** يتحقق من الصلاحيات المطلوبة

**الكود:**
```java
boolean locationFine = ContextCompat.checkSelfPermission(...) == PERMISSION_GRANTED;
boolean locationCoarse = ContextCompat.checkSelfPermission(...) == PERMISSION_GRANTED;
boolean sms = ContextCompat.checkSelfPermission(...) == PERMISSION_GRANTED;

return locationFine && locationCoarse && sms;
```

**الشرح:**
- يتحقق من 3 صلاحيات:
  1. `ACCESS_FINE_LOCATION` - الموقع الدقيق
  2. `ACCESS_COARSE_LOCATION` - الموقع التقريبي
  3. `SEND_SMS` - إرسال الرسائل
- يجب أن تكون جميعها ممنوحة

---

#### `public void onRequestPermissionsResult(...)`
**الوصف:** يتم استدعاؤه بعد طلب الصلاحيات

**الكود:**
```java
if (allGranted) {
    // جميع الصلاحيات ممنوحة
    if (pendingEnable) {
        // المستخدم كان يريد التفعيل → فعله الآن
        switchGestureService.setChecked(true);
        startGestureService();
    }
} else {
    // بعض الصلاحيات مرفوضة
    switchGestureService.setChecked(false);
}
```

**الشرح:**
- `pendingEnable`: متغير يحفظ رغبة المستخدم في التفعيل
- إذا منح الصلاحيات → يفعل الخدمة تلقائياً
- إذا رفض → يوقف التفعيل

---

## 4. SOSUtil.java

### 📖 الوصف العام
Utility class لإرسال تنبيهات SOS والإحداثيات.

### 📝 Methods الرئيسية

#### `public static Location getCurrentLocation(Context context)`
**الوصف:** يحصل على الموقع الحالي

**الكود:**
```java
LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

// محاولة GPS أولاً
if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
}

// إذا فشل، استخدم الشبكة
if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
}
```

**الشرح:**
1. يحصل على `LocationManager`
2. يحاول GPS أولاً (أكثر دقة)
3. إذا فشل، يستخدم Network (أسرع لكن أقل دقة)

---

#### `public static void sendSOSAlert(Context context)`
**الوصف:** يرسل تنبيه SOS مع الموقع

**الكود:**
```java
// 1. الحصول على المستخدم
User user = UserSession.getUser();

// 2. الحصول على الموقع
Location location = getCurrentLocation(context);

// 3. الحصول على جهات الاتصال الطارئة
List<EmergencyContact> contacts = db.emergencyContactDao().getForUser(user.getId());

// 4. إنشاء الرسالة
String message = String.format(SOS_MESSAGE_TEMPLATE, userName, locationStr, lat, lon, timeStr);

// 5. إرسال SMS لكل جهة اتصال
for (EmergencyContact contact : contacts) {
    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    sentCount++;
}

// 6. حفظ في التاريخ
AlertHistory alertHistory = new AlertHistory(...);
db.alertHistoryDao().insert(alertHistory);
```

**الشرح:**
1. **التحقق من المستخدم:** يجب أن يكون مسجل دخول
2. **الحصول على الموقع:** GPS أو Network
3. **جلب جهات الاتصال:** من قاعدة البيانات
4. **إنشاء الرسالة:** نص يحتوي على الموقع والوقت
5. **إرسال SMS:** لكل جهة اتصال
6. **حفظ في التاريخ:** لتتبع التنبيهات

**الرسالة المرسلة:**
```
🚨 SOS ALERT 🚨
User: [اسم المستخدم]
Location: Lat: XX.XXXXXX, Lon: XX.XXXXXX
Coordinates: XX.XXXXXX, XX.XXXXXX
Time: 2024-12-29 10:30:00
Please help immediately!
```

---

#### `public static void sendGPSCoordinates(Context context)`
**الوصف:** يرسل الإحداثيات فقط (بدون SOS)

**الفرق عن sendSOSAlert():**
- رسالة أبسط
- خطورة "HIGH" بدلاً من "CRITICAL"
- لا يحتوي على "SOS ALERT"

---

## 5. FlashlightUtil.java

### 📖 الوصف العام
Utility class للتحكم في الفلاش.

### 📝 Methods الرئيسية

#### `public static void initialize(Context context)`
**الوصف:** يهيئ الفلاش

**الكود:**
```java
cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
cameraId = cameraManager.getCameraIdList()[0];
```

**الشرح:**
- يحصل على `CameraManager`
- يحصل على ID الكاميرا الأولى (عادة الكاميرا الخلفية)

---

#### `public static void toggleFlashlight(Context context)`
**الوصف:** يشغل/يوقف الفلاش

**الكود:**
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    toggleFlashlightModern(context);  // Android 6.0+
} else {
    toggleFlashlightLegacy(context);  // Android قديم
}
```

**الشرح:**
- **Android 6.0+:** يستخدم `CameraManager.setTorchMode()`
- **Android قديم:** يستخدم `Camera.Parameters`

---

#### `private static void toggleFlashlightModern(Context context)`
**الوصف:** تشغيل/إيقاف الفلاش (طريقة حديثة)

**الكود:**
```java
isFlashlightOn = !isFlashlightOn;
cameraManager.setTorchMode(cameraId, isFlashlightOn);

if (isFlashlightOn) {
    AlertSender.vibrate(context, 200);
    saveFlashlightAlert(context, true);
} else {
    saveFlashlightAlert(context, false);
}
```

**الشرح:**
1. **تبديل الحالة:** `isFlashlightOn = !isFlashlightOn`
2. **تطبيق التغيير:** `setTorchMode(cameraId, isFlashlightOn)`
3. **ردود الفعل:**
   - اهتزاز عند التشغيل
   - حفظ في التاريخ

---

## 🔄 تدفق العمل الكامل (Complete Work Flow)

### سيناريو: المستخدم يهز الهاتف مرتين

1. **المستخدم يهز الهاتف** 
   ↓
2. **Accelerometer يكتشف الحركة**
   ↓
3. **GestureDetector.handleAccelerometerEvent()**
   - يحسب التغيير في التسارع
   - إذا > 8.0 → هزة واحدة
   ↓
4. **هزة ثانية خلال ثانية**
   ↓
5. **GestureDetector.onDoubleShakeDetected()**
   ↓
6. **GestureService.onDoubleShakeDetected()**
   ↓
7. **SOSUtil.sendSOSAlert()**
   - يحصل على الموقع
   - يجلب جهات الاتصال
   - يرسل SMS
   - يحفظ في التاريخ
   ↓
8. **المستخدم يتلقى:**
   - Toast: "SOS alert sent"
   - إشعار
   - اهتزاز

---

## 📌 نقاط مهمة للفهم

### 1. لماذا Accelerometer للهز وليس Gyroscope؟
- **Accelerometer:** يقيس التسارع الخطي (الحركة المستقيمة)
- **Gyroscope:** يقيس السرعة الزاوية (الدوران)
- **الهز:** حركة خطية → Accelerometer أفضل

### 2. لماذا Foreground Service؟
- Android يقتل الخدمات في الخلفية
- **الحل:** Foreground Service مع إشعار دائم
- المستخدم يرى الإشعار ويعرف أن الخدمة تعمل

### 3. لماذا `isUpdatingSwitch`؟
- عند تحديث Switch برمجياً (`setChecked()`)، يستدعي Listener
- هذا يسبب حلقة لانهائية
- **الحل:** `isUpdatingSwitch` يمنع استدعاء Listener عند التحديث البرمجي

### 4. لماذا Thread في SOSUtil؟
- إرسال SMS وعمليات قاعدة البيانات **بطيئة**
- لا يجب تنفيذها على Main Thread (يسبب تجمد الواجهة)
- **الحل:** Thread منفصل

---

## 🎯 الخلاصة

**Module 4 يتكون من:**
1. **GestureDetector:** يكتشف الإيماءات
2. **GestureService:** يعمل في الخلفية وينفذ الإجراءات
3. **ControlFragment:** واجهة المستخدم
4. **SOSUtil:** إرسال التنبيهات
5. **FlashlightUtil:** التحكم في الفلاش

**الإيماءات المدعومة:**
- **Double Shake** → SOS Alert
- **Hand Wave** → Toggle Flashlight
- **Double Tilt** → Send GPS

**كل شيء يعمل حتى عند قفل الشاشة!** 🔒✨

