go.teacher.2018@gmail.com
pass : teacher@2e



APK bisa di download di drive (akun email ini)
https://drive.google.com/drive/folders/1JZoQodFtldbuUqrdlvEDG3zhiPnpZOsQ?usp=sharing



Hosting:
https://go-teacher2018.000webhostapp.com/



Repository (SourceCode)
https://bitbucket.org/why_raya/goteacher/src/master/

- Buka email (go.teacher.2018@gmail.com), dan accept invitation untuk memakai repo goteacher di bitbucket

- Masukkan username

- Project bisa di download atau di pull
	a. Download(clone) : Menu >> Download
	b. Pull : setup sourcetree jika belum punya (https://confluence.atlassian.com/get-started-with-sourcetree/install-and-set-up-sourcetree-847359043.html) >> New >> Clone from URL >> Masukkan path (https://why_raya@bitbucket.org/why_raya/goteacher.git) di kolom Source URL, dan pilih folder directory di komputermu.

	Kalau pakai cara pull, nanti setiap ada perubahan tinggal di pull aja via sourcetree, jadi nanti keliatan yang di update apa aja. Plus yang di pull(download) itu cuman data yang di update, jadi bukan 1 project.




Cek image untuk screenshoot di folder GoTeacher/tutorial/image yang ada di repo, jika step dibawah kurang jelas

================================================================

Data Firestore
- https://console.firebase.google.com
- login dengan email go.teacher.2018@gmail.com
- Pilih project Go Teacher
- Database



================================================================

Jika ingin menggunakan service dari google, kita harus menambahkan google-services.json.

Step-by-step:

1. Dapatkan SHA1 komputer kita
	a. Cara yang paling mudah dengan menggunakan android studio
	b. Buka project kita, lalu pilih tombol Gradle di pojok kanan atas
	c. Masuk ke GoTeacher (root) >> Run Configuration >> GoTeacher [signingReport], klik 2x
	d. Hasilnya akan muncul di bagin run, bawah layar
	e. Copy SHA1

2. Daftarkan SHA1 ke Firebase	
	a. Masuk firebase dengan email go.teacher.2018@gmail.com dan pilih project Go Teacher
	b. Settings (Kiri atas) >> Project Settings >> Add Fingerprint (Bawah)
	c. masukkan SHA1 dan nama package disitu 
	d. jika sudah di tambahkan download google-services.json

3. Download google-services.json & taruh di folder app di project kita

Step ini harus selalu dijalankan setiap mengerjakan project di komputer baru yang belum terdaftar (Setiap komputer, SHA1 nya beda).



================================================================

Build APK Release

1. Buka project di android studio dan pilih build.gradle (app) 

2. Pastikan versionCode dan versionName tidak sama dengan release sebelumnya
	a. hanya berlaku jika sudah ada versi sebelumnya, untuk menghindari bug tidak bisa di install akibat version sama tapi

3. Pilih Build(atas) >> Generated Signed Bundle /APK >> APK

4. Masukkan store path (.jks) bisa buat sendiri maupun pakai yang sudah ada
	a. Buat baru : Create New >> Isi data
	b. Pakai yang sudah ada : Choose Existing >> file di GoTeacher/key.jks (pass: teacher@2e | alias: go_teacher)

5. Pilih release, lalu centang Signature Version untuk V1 & V2.

6. APK akan di build, dan jika sukses hasil nya bisa di cek di GoTeacher/app/release/app-release.apk (App bisa langsung diinstal atau di upload ke Playstore)





