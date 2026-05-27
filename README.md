# iM Notes Minimal

แอพจดบันทึก Android แบบ offline-first เขียนด้วย Kotlin + Jetpack Compose + Room Database และเตรียมโครงสร้างสำหรับ Online Sync ด้วย Supabase

## แนวคิดของแอพ

แอพนี้ออกแบบให้ใช้งานได้ 2 แบบ:

1. **Offline Mode** — ไม่ต้องสร้างบัญชีก็จดโน้ตได้ ข้อมูลถูกเก็บไว้ในเครื่องด้วย Room Database
2. **Online Sync Mode** — เมื่อล็อกอินหรือสร้างบัญชี ข้อมูลจะสามารถสำรองและซิงค์ออนไลน์อัตโนมัติได้

เป้าหมายระยะยาวคือให้ผู้ใช้เลือกเองได้ว่าจะใช้ออฟไลน์อย่างเดียว หรือใช้ออนไลน์เพื่อย้ายเครื่องและซิงค์หลายอุปกรณ์

## สถานะปัจจุบัน

โปรเจกต์นี้เป็นแอพจดโน้ตที่ใช้งานในเครื่องได้แล้ว โดยข้อมูลโน้ตถูกเก็บไว้ใน SQLite ผ่าน Room Database บนอุปกรณ์ของผู้ใช้ และมี field เตรียมไว้สำหรับ sync ออนไลน์ เช่น `userId`, `remoteId`, `syncStatus`, `deletedAt`

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
- หน้าบัญชีและการเข้าสู่ระบบสำหรับเตรียมเชื่อมระบบออนไลน์
- Supabase SQL schema สำหรับตารางออนไลน์และ RLS

## Tech Stack

- Kotlin
- Android Jetpack Compose
- Material 3
- Navigation Compose
- Room Database
- SharedPreferences
- Supabase Auth และ Supabase Database สำหรับแผน Online Sync
- Gradle Kotlin DSL

## Run Locally

**Prerequisites:** Android Studio

1. Clone repository นี้ลงเครื่อง
2. เปิดโฟลเดอร์โปรเจกต์ด้วย Android Studio
3. รอให้ Gradle Sync เสร็จ
4. เลือก emulator หรือ Android device
5. กด Run

แอพทำงานแบบ offline-first จึงยังไม่จำเป็นต้องใส่ API key เพื่อเริ่มใช้งาน

## Supabase Setup

ไฟล์ SQL สำหรับเตรียม backend อยู่ที่:

```text
supabase/im_notes_minimal_schema.sql
```

ขั้นตอนใช้งาน:

1. สร้าง Supabase project ใหม่
2. เปิด SQL Editor
3. วางเนื้อหาใน `supabase/im_notes_minimal_schema.sql`
4. กด Run

Schema นี้จะสร้าง:

- `public.profiles`
- `public.notes`
- Trigger สำหรับ `updated_at`
- Trigger สำหรับสร้าง profile หลังสมัครสมาชิก
- Row Level Security เพื่อให้ผู้ใช้เห็นเฉพาะโน้ตของตัวเอง

## Sync Roadmap

ลำดับการทำ Online Sync:

1. เพิ่ม Supabase client dependency ใน Android
2. เก็บ `SUPABASE_URL` และ `SUPABASE_ANON_KEY` แบบปลอดภัย
3. เชื่อม Supabase Auth กับหน้า Login / Register
4. หลัง login ให้ local notes ที่ยังไม่มี `remoteId` ถูก upload ไป `public.notes`
5. เมื่อ upload สำเร็จ บันทึก `remoteId` กลับมาใน Room และตั้ง `syncStatus = SYNCED`
6. เมื่อสร้าง/แก้ไข/ลบโน้ต ให้บันทึกใน Room ก่อน แล้วค่อย sync ขึ้นออนไลน์
7. ทำ pull sync จาก Supabase กลับเข้า Room เพื่อรองรับหลายอุปกรณ์
8. เพิ่ม conflict handling ด้วย `updatedAt`, `sync_version`, และ `deletedAt`

## Roadmap

สิ่งที่ควรพัฒนาต่อ:

1. เชื่อม Supabase Auth จริง
2. เพิ่ม Cloud Sync จริง
3. เพิ่มหมวดหมู่หรือแท็กของโน้ต
4. เพิ่มระบบ Archive / Trash
5. เพิ่ม Export / Import สำรองข้อมูล
6. เพิ่ม widget หรือ quick note จากหน้า home
7. เพิ่ม automated tests สำหรับ ViewModel, Room DAO และ Sync Manager
