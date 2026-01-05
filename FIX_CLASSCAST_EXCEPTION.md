# حل ClassCastException: SwitchCompat cannot be cast to Switch

## 🔴 الخطأ:
```
java.lang.ClassCastException: androidx.appcompat.widget.SwitchCompat cannot be cast to android.widget.Switch
at com.example.projet.Fragments.Control.ControlFragment.onCreateView(ControlFragment.java:60)
```

## ✅ الحل:

### **الخطوة 1: تأكد من الكود**

الكود يجب أن يكون:
```java
import androidx.appcompat.widget.SwitchCompat;  // ✅ صحيح

private SwitchCompat switchGestureService;  // ✅ صحيح

switchGestureService = (SwitchCompat) view.findViewById(R.id.switchGestureService);  // ✅ صحيح
```

### **الخطوة 2: Clean و Rebuild**

**في Android Studio:**
1. **Build → Clean Project**
2. انتظر حتى ينتهي
3. **Build → Rebuild Project**
4. انتظر حتى ينتهي
5. **شغّل التطبيق من جديد**

### **الخطوة 3: Invalid Caches**

إذا لم يعمل:
1. **File → Invalidate Caches...**
2. اختر **"Invalidate and Restart"**
3. انتظر حتى يعيد التشغيل
4. شغّل التطبيق من جديد

### **الخطوة 4: تأكد من XML**

في `fragment_control.xml` يجب أن يكون:
```xml
<androidx.appcompat.widget.SwitchCompat
    android:id="@+id/switchGestureService"
    ... />
```

**وليس:**
```xml
<Switch  <!-- ❌ خطأ -->
    android:id="@+id/switchGestureService"
    ... />
```

---

## 🔍 التحقق من الكود الحالي

### **في ControlFragment.java:**

✅ **يجب أن يكون:**
```java
import androidx.appcompat.widget.SwitchCompat;  // السطر 17

private SwitchCompat switchGestureService;  // السطر 31

switchGestureService = (SwitchCompat) view.findViewById(R.id.switchGestureService);  // السطر 63
```

### **في fragment_control.xml:**

✅ **يجب أن يكون:**
```xml
<androidx.appcompat.widget.SwitchCompat
    android:id="@+id/switchGestureService"
    ... />
```

---

## 🛠️ إذا استمرت المشكلة

### **1. احذف Build Folder**
- احذف مجلد `app/build`
- احذف مجلد `.gradle` في المشروع
- Clean و Rebuild

### **2. تأكد من Dependencies**
في `build.gradle.kts` يجب أن يكون:
```kotlin
implementation(libs.appcompat)
implementation(libs.material)
```

### **3. Sync Project**
- **File → Sync Project with Gradle Files**

---

## ✅ الحل النهائي

الكود تم إصلاحه بالفعل. فقط:

1. **Clean Project**
2. **Rebuild Project**
3. **شغّل التطبيق**

يجب أن يعمل الآن! ✅

