# Product Requirements Document (PRD): Custom Pomodoro & Habit Tracker

## 1. Product Overview
- **Objective:** Membangun *habit* produktif dan melakukan *tracking output* harian melalui integrasi Pomodoro timer, *task management*, dan *strict app blocking*.
- **Target Platform:** Android (Native Kotlin/Java atau Flutter), dioptimalkan untuk arsitektur dan sistem manajemen memori Xiaomi Redmi Note 12 Pro 5G (MIUI/HyperOS).
- **Core Value Proposition:** Menjaga fokus secara paksa (*forced focus*) dengan hanya mengizinkan WhatsApp, sambil memastikan setiap sesi waktu terikat dengan pencapaian target kerja yang konkret.

## 2. Core Features & User Flow

### A. Pomodoro Engine & App Blocker
- **Customizable Timer:** Pengaturan durasi *Focus Time* (default 25 menit) dan *Rest Time* (default 5 menit).
- **Strict Whitelisting:** Selama *Focus Time* berjalan, OS hanya mengizinkan aplikasi berjalan di *foreground* jika `package_name` adalah aplikasi ini atau `com.whatsapp`.
- **Blocking Overlay:** Jika sistem mendeteksi user membuka aplikasi di luar whitelist (misal: Instagram), aplikasi langsung memunculkan layar *overlay fullscreen* yang menutupi aplikasi tersebut.
- **Cancel/Bypass Mechanism:** Terdapat tombol *emergency exit* untuk membatalkan sesi pada layar *overlay* atau layar timer. Menggunakan fitur ini akan mencatat sesi sebagai *Failed/Canceled* dan mereset probabilitas *streak* hari itu.

### B. Task Management (Target System)
- **Todo Binding:** Sebelum memulai timer, user wajib/opsional menambahkan *target todo* yang harus diselesaikan dalam sesi tersebut.
- **Achieved Targets:** Target yang ditandai *checked/done* saat sesi selesai akan diarsipkan ke halaman *Achieved Targets* beserta *timestamp* dan total waktu fokus yang dihabiskan.
- **Unfinished Targets:** Target yang belum di-*check* hingga hari berakhir akan masuk ke *backlog* (*Unfinished Targets*) agar bisa ditarik kembali ke antrean tugas esok harinya.

### C. Habit & Progress Tracking
- **Streak System:** Algoritma akan menambahkan +1 pada *current streak* jika dalam satu siklus hari (00:00 - 23:59) user berhasil menyelesaikan minimal 1 sesi Pomodoro utuh **dan** mencetak minimal 1 *Achieved Target*.
- **Calendar Heatmap/Tracker:** UI Kalender bulanan dengan dua indikator visual:
  - *Tier 1 (Partial/Yellow):* Tanggal di mana user menggunakan fitur Pomodoro sampai selesai, namun tidak menyelesaikan target apa pun.
  - *Tier 2 (Perfect/Green):* Tanggal di mana user menyelesaikan sesi Pomodoro **dan** berhasil mencatat *Achieved Target*.

## 3. Technical & System Architecture (Redmi Note 12 Pro 5G / HyperOS Specifics)

Sistem operasi HyperOS sangat agresif mematikan proses di *background* untuk menghemat baterai. Aplikasi Pomodoro dengan sistem *blocking* akan gagal berfungsi di HP ini jika tidak dirancang dengan *workaround* berikut:

- **Required Android Permissions:**
  - `PACKAGE_USAGE_STATS` (Usage Access): Akses kritikal untuk membaca log OS dan mengetahui aplikasi apa yang sedang dibuka oleh user secara *real-time*.
  - `SYSTEM_ALERT_WINDOW` (Display over other apps): Izin untuk me-render UI *blocking screen* di atas aplikasi lain secara paksa.
  - `FOREGROUND_SERVICE` & `POST_NOTIFICATIONS`: Timer dan *monitoring engine* wajib di-bind ke notifikasi persisten yang tidak bisa di-swipe.
- **HyperOS Battery Optimization Bypass:** 
  Aplikasi harus menyediakan *onboarding screen* yang memaksa/mengarahkan user ke pengaturan *App Info*, untuk mengatur opsi **Battery Saver** menjadi **No Restrictions** dan menyalakan opsi **Autostart**. Tanpa ini, OS akan membunuh timer di menit ke-3 hingga ke-5 saat layar mati.
- **Local Storage:** Menggunakan SQLite (via Room di Android) untuk menyimpan tabel log sesi dan transisi status target (`TODO` $\rightarrow$ `ACHIEVED` atau `TODO` $\rightarrow$ `UNFINISHED`).