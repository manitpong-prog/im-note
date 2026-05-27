# iM Note

แอพจดบันทึก Android แบบ local-first เขียนด้วย Kotlin + Jetpack Compose + Room Database

## สถานะปัจจุบัน

โปรเจกต์นี้เป็นแอพจดโน้ตที่ใช้งานในเครื่องได้แล้ว โดยข้อมูลโน้ตถูกเก็บไว้ใน SQLite ผ่าน Room Database บนอุปกรณ์ของผู้ใช้

ฟีเจอร์ที่มีแล้ว:

- เพิ่ม แก้ไข และลบโน้ต
- บันทึกชื่อเรื่องและเนื้อหา
- เลือกสีโน้ต
- ปักหมุดโน้ตสำคัญ
- ค้นหาโน้ตจากชื่อเรื่องหรือเนื้อหา
- เรียงลำดับตามเวลาที่แก้ไขหรือชื่อเรื่อง
- สลับมุมมอง List / Grid
- Dark Mode
- ตั้งค่าให้โน้ตใหม่ถูกปักหมุดเป็นค่าเริ่มต้น
- หน้าบัญชีและการเข้าสู่ระบบแบบทดลองสำหรับทดสอบ UI

> หมายเหตุ: ระบบบัญชี, Google Login และ Cloud Sync ในเวอร์ชันนี้ยังเป็นโหมดจำลองสำหรับทดสอบหน้าจอเท่านั้น ยังไม่ได้เชื่อมต่อ backend จริง

## Tech Stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Navigation Compose
- Room Database
- SharedPreferences
- Gradle Kotlin DSL

## Run Locally

**Prerequisites:** Android Studio

1. Clone repository นี้ลงเครื่อง
2. เปิดโฟลเดอร์โปรเจกต์ด้วย Android Studio
3. รอให้ Gradle Sync เสร็จ
4. เลือก emulator หรือ Android device
5. กด Run

แอพทำงานแบบ offline-first จึงยังไม่จำเป็นต้องใส่ API key เพื่อเริ่มใช้งาน

## Roadmap

สิ่งที่ควรพัฒนาต่อ:

1. เปลี่ยนระบบบัญชีจำลองเป็น Firebase Auth หรือ Supabase Auth
2. เพิ่ม Cloud Sync จริง
3. เพิ่มหมวดหมู่หรือแท็กของโน้ต
4. เพิ่มระบบ Archive / Trash
5. เพิ่ม Export / Import สำรองข้อมูล
6. เพิ่ม widget หรือ quick note จากหน้า home
7. เพิ่ม automated tests สำหรับ ViewModel และ Room DAO
