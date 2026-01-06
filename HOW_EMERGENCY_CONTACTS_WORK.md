# كيف يعمل إرسال SMS لجهات الاتصال الطارئة؟

## ✅ **نعم! الرسائل تُرسل إلى جميع جهات الاتصال التي أضفتها**

---

## 🔄 كيف يعمل النظام؟

### **الخطوة 1: إضافة جهة اتصال طارئة**

عندما تضيف جهة اتصال طارئة:

```java
// في AddEditEmergencyContactFragment.java
EmergencyContact c = new EmergencyContact(
    user.getId(),      // ID المستخدم الحالي
    name,              // اسم جهة الاتصال
    phone,             // رقم الهاتف
    primary            // هل هي أساسية؟
);

db.emergencyContactDao().insert(c);  // حفظ في قاعدة البيانات
```

**يتم حفظ:**
- ✅ الاسم (displayName)
- ✅ رقم الهاتف (phoneNumber)
- ✅ المستخدم المالك (ownerUserId)
- ✅ هل هي أساسية (isPrimary)

---

### **الخطوة 2: عند إرسال SOS**

عندما تهز الهاتف مرتين (Double Shake):

```java
// في SOSUtil.sendSOSAlert()

// 1. الحصول على المستخدم الحالي
User user = UserSession.getUser();

// 2. جلب جميع جهات الاتصال الطارئة من قاعدة البيانات
List<EmergencyContact> contacts = db.emergencyContactDao().getForUser(user.getId());
```

**الكود في EmergencyContactDao:**
```java
@Query("SELECT * FROM emergency_contacts WHERE ownerUserId = :userId ORDER BY isPrimary DESC, displayName ASC")
List<EmergencyContact> getForUser(int userId);
```

**الشرح:**
- يجلب جميع جهات الاتصال التي `ownerUserId = userId`
- مرتبة حسب: الأساسية أولاً، ثم حسب الاسم

---

### **الخطوة 3: إرسال SMS لكل جهة اتصال**

```java
// في SOSUtil.sendSOSAlert()

SmsManager smsManager = SmsManager.getDefault();
int sentCount = 0;

// حلقة على جميع جهات الاتصال
for (EmergencyContact contact : contacts) {
    try {
        String phoneNumber = contact.phoneNumber;  // رقم الهاتف من قاعدة البيانات
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // إرسال SMS إلى هذا الرقم
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            sentCount++;  // زيادة العداد
        }
    } catch (Exception e) {
        // في حالة الفشل، تسجيل الخطأ
        Log.e("SOSUtil", "Failed to send SMS to " + contact.displayName, e);
    }
}
```

---

## 📋 مثال عملي

### **السيناريو:**

1. **المستخدم يضيف 3 جهات اتصال:**
   - أحمد: `12345678`
   - فاطمة: `87654321`
   - محمد: `11223344`

2. **المستخدم يهز الهاتف مرتين:**
   - يتم استدعاء `sendSOSAlert()`

3. **النظام يجلب الجهات:**
   ```java
   contacts = [
       {name: "أحمد", phone: "12345678"},
       {name: "فاطمة", phone: "87654321"},
       {name: "محمد", phone: "11223344"}
   ]
   ```

4. **النظام يرسل SMS لكل واحد:**
   ```
   SMS 1 → 12345678 (أحمد)
   SMS 2 → 87654321 (فاطمة)
   SMS 3 → 11223344 (محمد)
   ```

5. **النتيجة:**
   - `sentCount = 3`
   - رسالة Toast: "SOS alert sent to 3 contact(s)"

---

## 🔍 تفاصيل مهمة

### **1. من أين تأتي الأرقام؟**

الأرقام تأتي من **قاعدة البيانات المحلية** (Room Database):

```java
// جدول emergency_contacts
- id
- ownerUserId        // المستخدم الذي أضاف الجهة
- displayName        // الاسم المعروض
- phoneNumber        // رقم الهاتف ← هذا يُستخدم للإرسال
- isPrimary          // هل هي أساسية؟
```

---

### **2. كيف تضيف جهة اتصال؟**

**طريقتان:**

#### **الطريقة 1: إضافة يدوية**
```
1. اذهب إلى "Emergency Contacts" من القائمة
2. اضغط على زر الإضافة (+)
3. أدخل الاسم والرقم
4. احفظ
```

**الكود:**
```java
// في AddEditEmergencyContactFragment.java
EmergencyContact c = new EmergencyContact(
    user.getId(),
    name,      // من حقل الإدخال
    phone,      // من حقل الإدخال
    primary
);
db.emergencyContactDao().insert(c);
```

#### **الطريقة 2: من قائمة الأصدقاء**
```
1. اذهب إلى "Friends"
2. اختر صديق
3. اضغط على "Make Emergency Contact"
```

**الكود:**
```java
// في FriendEmergencyRepository.java
EmergencyContact ec = new EmergencyContact();
ec.ownerUserId = ownerUserId;
ec.friendUserId = friendUserId;
ec.displayName = friendUser.getUsername();
ec.phoneNumber = friendUser.getPhoneNumber();  // من بيانات الصديق
ec.isPrimary = false;
db.emergencyContactDao().insert(ec);
```

---

### **3. ماذا لو لم تكن هناك جهات اتصال؟**

```java
if (contacts.isEmpty()) {
    Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_LONG).show();
    return;  // لا يتم إرسال أي شيء
}
```

**النتيجة:**
- ❌ لا يتم إرسال SMS
- ✅ رسالة Toast: "No emergency contacts found"

---

### **4. ماذا لو فشل إرسال SMS لجهة واحدة؟**

```java
for (EmergencyContact contact : contacts) {
    try {
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        sentCount++;
    } catch (Exception e) {
        // تسجيل الخطأ فقط، لكن يستمر في إرسال للباقي
        Log.e("SOSUtil", "Failed to send SMS to " + contact.displayName, e);
    }
}
```

**النتيجة:**
- ✅ يستمر في إرسال للجهات الأخرى
- ✅ `sentCount` يحتوي على عدد الناجح فقط
- ⚠️ الخطأ يُسجل في Logcat

---

## 📊 تدفق العمل الكامل

```
1. المستخدم يضيف جهات اتصال
   ↓
   حفظ في قاعدة البيانات (emergency_contacts)
   
2. المستخدم يهز الهاتف مرتين
   ↓
   GestureDetector يكتشف Double Shake
   ↓
   GestureService.onDoubleShakeDetected()
   ↓
   SOSUtil.sendSOSAlert()
   
3. جلب جهات الاتصال
   ↓
   db.emergencyContactDao().getForUser(userId)
   ↓
   List<EmergencyContact> contacts
   
4. إرسال SMS لكل جهة
   ↓
   for (EmergencyContact contact : contacts) {
       smsManager.sendTextMessage(contact.phoneNumber, ...)
   }
   
5. النتيجة
   ↓
   Toast: "SOS alert sent to X contact(s)"
```

---

## ✅ الخلاصة

### **نعم، الرسائل تُرسل إلى:**
- ✅ جميع جهات الاتصال التي أضفتها في "Emergency Contacts"
- ✅ الأرقام التي حفظتها في قاعدة البيانات
- ✅ كل جهة اتصال تحصل على رسالة منفصلة

### **المتطلبات:**
- ✅ يجب أن تكون مسجل دخول
- ✅ يجب أن يكون لديك جهات اتصال طارئة
- ✅ يجب أن تكون الأرقام صحيحة
- ✅ يجب أن يكون لديك صلاحية SEND_SMS
- ✅ يجب أن يكون لديك SIM Card وإشارة

### **الترتيب:**
- جهات الاتصال الأساسية (isPrimary = true) أولاً
- ثم الباقي مرتبة حسب الاسم

---

## 🎯 مثال للاختبار

1. **أضف جهة اتصال:**
   - اسم: "Test Contact"
   - رقم: "12345678" (رقمك للاختبار)

2. **هز الهاتف مرتين**

3. **تحقق:**
   - يجب أن تستقبل SMS على الرقم `12345678`
   - الرسالة تحتوي على:
     ```
     🚨 SOS ALERT 🚨
     User: [اسمك]
     Location: ...
     Coordinates: ...
     Time: ...
     Please help immediately!
     ```

---

**ملاحظة:** تأكد من أن الأرقام صحيحة وأن لديك إشارة شبكة!

