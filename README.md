<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/5e00673f-ec3b-479d-bded-043eaa62b644

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## وضعیت این نسخه

این نسخه چند اصلاح مهم Production را دارد:
- حذف `fallbackToDestructiveMigration` و استفاده از migrationهای Room
- داده‌های نمونه فقط در buildهای Debug ساخته می‌شوند
- Import پشتیبان اعتبارسنجی و transaction اتمیک دارد
- PIN دارای salt تصادفی و rate-limit/lockout است
- فروش دارایی ورودی‌ها و موجودی را اعتبارسنجی می‌کند
- حذف خریدی که قبلاً در فروش استفاده شده مسدود می‌شود

### راه‌اندازی قیمت زنده

اپ الان **مستقیم** به BrsApi.ir وصل می‌شه (بدون پراکسی):

1. یک کلید از [brsapi.ir](https://brsapi.ir) بگیر.
2. مقدار `BRSAPI_KEY` را در فایل `.env` (کنار `.env.example`) قرار بده.
3. اپ رو build کن — کلید به‌صورت خودکار به `BuildConfig.BRSAPI_KEY` تزریق می‌شه.

**⚠️ هشدار امنیتی — این یک ریسک شناخته‌شده و آگاهانه است، نه یک باگ:**

قبلاً یک پراکسی Cloudflare Worker امتحان شد تا کلید هرگز داخل APK نره (کلید فقط به‌عنوان
`wrangler secret` روی Worker می‌نشست). اما BrsApi.ir به درخواست‌هایی که از IP رنج Worker
می‌اومدن، خطای **401 Unauthorized** برمی‌گردوند — یعنی BrsApi این ترافیک رو رد می‌کرد،
احتمالاً به خاطر مسدودیت IP دیتاسنترها. به همین دلیل پراکسی حذف شد و اپ مستقیم به BrsApi
وصل می‌شه.

پیامد این تصمیم: کلید API الان **داخل APK کامپایل می‌شه** (در `BuildConfig.BRSAPI_KEY`) و
هرکسی که APK رو با ابزارهایی مثل `apktool` یا `jadx` دیکامپایل کنه، در چند دقیقه می‌تونه
کلید رو استخراج کنه. راه‌های کاهش ریسک (نه حذف کامل ریسک):
- فقط از کلیدی استفاده کن که مشکلی نداری عمومی بشه (نه یک کلید Pro گران‌قیمت).
- مصرف/کوتای حساب `brsapi.ir`‌ت رو گاه‌به‌گاه چک کن تا اگه کسی کلید رو استخراج و سوءاستفاده
  کرد، متوجه بشی.
- اگه بعداً خواستی دوباره پراکسی رو امتحان کنی، احتمالاً باید یه راه‌حل غیر-Cloudflare (مثلاً
  یک VPS با IP معمولی، نه IP رنج شناخته‌شده‌ی CDN) رو امتحان کنی، چون به نظر می‌رسه مشکل از
  مسدودیت IP دیتاسنتر/CDN توسط BrsApi بوده، نه یه باگ تو کد Worker.

### نکته Backup

فایل JSON پشتیبان عمداً قابل خواندن است؛ بنابراین آن را مانند اطلاعات مالی محرمانه نگهداری کن. در نسخه بعدی بهتر است Backup رمزنگاری‌شده با رمز عبور کاربر اضافه شود.
