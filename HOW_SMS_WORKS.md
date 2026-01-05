# كيف يعمل إرسال SMS بدون API خارجي؟

## 📱 SmsManager - جزء من Android SDK

### ✅ **لا نحتاج API خارجي!**

Android SDK يحتوي على `SmsManager` وهو **جزء من النظام نفسه**، ليس API خارجي مثل Twilio أو Firebase.

---

## 🔧 كيف يعمل؟

### 1. **SmsManager.getDefault()**

```java
SmsManager smsManager = SmsManager.getDefault();
```

**الشرح:**
- `SmsManager` هو **كلاس مدمج** في Android SDK
- `getDefault()` يعطيك الـ SmsManager الافتراضي للجهاز
- **لا يحتاج:** 
  - ❌ API Key
  - ❌ Server
  - ❌ Internet Connection
  - ❌ External Service

**يعمل مباشرة من:**
- ✅ SIM Card في الهاتف
- ✅ شبكة الاتصالات (GSM/CDMA)
- ✅ نظام Android نفسه

---

### 2. **sendTextMessage() Method**

```java
smsManager.sendTextMessage(
    phoneNumber,  // رقم الهاتف المستقبل
    null,         // Service Center (null = default)
    message,      // نص الرسالة
    null,         // PendingIntent للنجاح (null = لا نحتاج)
    null          // PendingIntent للفشل (null = لا نحتاج)
);
```

**الشرح التفصيلي:**

#### **المعاملات (Parameters):**

1. **`phoneNumber`** (String)
   - رقم الهاتف المستقبل
   - مثال: `"1234567890"` أو `"+21612345678"`

2. **`null`** (Service Center)
   - مركز الخدمة (SMS Center)
   - `null` = يستخدم الافتراضي من SIM Card
   - عادة لا نحتاج تغييره

3. **`message`** (String)
   - نص الرسالة المراد إرسالها
   - مثال: `"🚨 SOS ALERT 🚨\nUser: Ahmed\nLocation: ..."`

4. **`null`** (PendingIntent للنجاح)
   - إذا أردت إشعار عند نجاح الإرسال
   - `null` = لا نحتاج إشعار

5. **`null`** (PendingIntent للفشل)
   - إذا أردت إشعار عند فشل الإرسال
   - `null` = لا نحتاج إشعار

---

## 🔄 كيف يعمل من الناحية التقنية؟

### **الخطوات:**

```
1. التطبيق يستدعي sendTextMessage()
   ↓
2. Android System يستقبل الطلب
   ↓
3. Android يرسل الرسالة إلى:
   - Telephony Service (خدمة الاتصالات)
   ↓
4. Telephony Service يرسل الرسالة عبر:
   - Radio Interface Layer (RIL)
   ↓
5. RIL يتواصل مع:
   - Modem (المودم) في الهاتف
   ↓
6. Modem يرسل الرسالة عبر:
   - شبكة الاتصالات (GSM/CDMA)
   ↓
7. شبكة الاتصالات ترسل الرسالة إلى:
   - رقم الهاتف المستقبل
```

---

## 📋 المتطلبات (Requirements)

### 1. **الصلاحية (Permission)**

```xml
<uses-permission android:name="android.permission.SEND_SMS" />
```

**في AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.SEND_SMS" />
```

**في الكود:**
```java
if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
        != PackageManager.PERMISSION_GRANTED) {
    // طلب الصلاحية
    return;
}
```

**لماذا؟**
- Android 6.0+ يحتاج صلاحية runtime
- المستخدم يجب أن يوافق

---

### 2. **SIM Card**

- ✅ يجب أن يكون هناك SIM Card في الهاتف
- ✅ يجب أن يكون هناك إشارة شبكة
- ✅ يجب أن يكون هناك رصيد (في بعض البلدان)

---

### 3. **لا يحتاج Internet**

- ❌ **لا يحتاج:** Wi-Fi
- ❌ **لا يحتاج:** Mobile Data
- ✅ **يحتاج فقط:** شبكة الاتصالات (GSM/CDMA)

---

## 💰 التكلفة

### **رسوم SMS عادية:**
- حسب باقة الاتصالات
- رسوم SMS عادية من المشغل
- **لا تكلفة إضافية** للتطبيق

---

## 🔒 الأمان والخصوصية

### **الرسائل تُرسل مباشرة:**
- ✅ لا تمر عبر خوادم خارجية
- ✅ لا يتم تخزينها في سحابة
- ✅ مباشرة من هاتفك إلى المستقبل

### **لكن:**
- ⚠️ المشغل (Carrier) يمكنه رؤية الرسائل
- ⚠️ الحكومة يمكنها الوصول (حسب القوانين)

---

## 📊 مقارنة: SmsManager vs API خارجي

| الميزة | SmsManager (مدمج) | API خارجي (Twilio/Firebase) |
|--------|-------------------|---------------------------|
| **التكلفة** | رسوم SMS عادية | تكلفة إضافية |
| **Internet** | ❌ لا يحتاج | ✅ يحتاج |
| **API Key** | ❌ لا يحتاج | ✅ يحتاج |
| **Server** | ❌ لا يحتاج | ✅ يحتاج |
| **السرعة** | سريع جداً | يعتمد على الإنترنت |
| **الموثوقية** | عالية (شبكة مباشرة) | تعتمد على الإنترنت |
| **الخصوصية** | جيدة (مباشرة) | تمر عبر خوادم |

---

## 💻 مثال كامل من الكود

```java
public static void sendSOSAlert(Context context) {
    // 1. التحقق من الصلاحية
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(context, "SMS permission required", Toast.LENGTH_LONG).show();
        return;
    }

    // 2. الحصول على SmsManager
    SmsManager smsManager = SmsManager.getDefault();
    
    // 3. إنشاء الرسالة
    String message = "🚨 SOS ALERT 🚨\nUser: Ahmed\nLocation: ...";
    
    // 4. إرسال الرسالة
    try {
        smsManager.sendTextMessage(
            "1234567890",  // رقم المستقبل
            null,          // Service Center (افتراضي)
            message,       // نص الرسالة
            null,          // Success callback
            null           // Failure callback
        );
        
        Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Log.e("SMS", "Failed to send SMS", e);
        Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show();
    }
}
```

---

## 🎯 الخلاصة

### **SmsManager هو:**
- ✅ جزء من Android SDK (مدمج)
- ✅ لا يحتاج API خارجي
- ✅ يعمل مباشرة من SIM Card
- ✅ لا يحتاج Internet
- ✅ سريع وموثوق
- ✅ مجاني (فقط رسوم SMS عادية)

### **كيف يعمل:**
1. التطبيق → `SmsManager.sendTextMessage()`
2. Android System → Telephony Service
3. Telephony Service → Modem
4. Modem → شبكة الاتصالات
5. شبكة الاتصالات → المستقبل

### **المتطلبات:**
- ✅ صلاحية `SEND_SMS`
- ✅ SIM Card
- ✅ إشارة شبكة
- ❌ لا يحتاج Internet
- ❌ لا يحتاج API Key

---

## 📚 مراجع

- [Android SmsManager Documentation](https://developer.android.com/reference/android/telephony/SmsManager)
- [Android SMS Permissions](https://developer.android.com/reference/android/Manifest.permission#SEND_SMS)

---

**ملاحظة:** في Android 10+، Google تقيد بعض استخدامات SmsManager لأسباب أمنية، لكنه ما زال يعمل للتطبيقات التي تحتاجه حقاً (مثل تطبيقات الطوارئ).

