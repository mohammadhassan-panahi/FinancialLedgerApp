<div align="center">
<img src="https://lh3.googleusercontent.com/aida/AEtjO1WKb-JdsP2NuvqL2iG_BxlPdgxrOhrOUd38Uq79YflzYtfw0btA5u1Leayr4ywNjITQ0m4tqK6H_6JcBKS_ctY9J9Mexqqim6vyQb1ktoMpFvZ9IB7ID0fbHW8B-gTkwM7ffip97krcMQFlUfqJAw6PTpe9RqefbKcdV4VcCKyRc24z_m8a81BghgI9zL__-G4zB8gsE0CVFM8OZdwyjQgSv-wLplJHAF-SgdtyiYDfAMeCs8jIQ9G3iDc" width="128" height="128" />

# دارا (DARA) — دستیار هوشمند مدیریت سرمایه و پورتفو
### پایش دقیق، تحلیل هوشمند و رصد لحظه‌ای بازارهای مالی در یک اپلیکیشن حرفه‌ای

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-latest-green.svg)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/AI-Gemini_1.5_Flash-orange.svg)](https://deepmind.google/technologies/gemini/)

</div>

---

## 🌟 ویژگی‌های کلیدی

### 🧠 هوش مصنوعی دارا (Dara AI)
*   **مشاور اختصاصی:** تحلیل پورتفوی شخصی بر اساس ریسک و تنوع‌بخشی.
*   **خلاصه هوشمند اخبار:** دریافت عصاره و نکات کلیدی اخبار اقتصادی در چند ثانیه.
*   **امتیازدهی بنیادی:** رتبه‌بندی دارایی‌ها بر اساس فاکتورهای فنی و بازار.

### 📈 تحلیل تکنیکال و نمودارها
*   **Candlestick Pro:** نمودارهای شمعی حرفه‌ای با پشتیبانی از بازه‌های زمانی مختلف.
*   **اندیکاتورها:** مجهز به **EMA، RSI، MACD** و **Bollinger Bands**.
*   **سیگنال‌های هوشمند:** نمایش نقاط قوت و ضعف روند بازار مستقیماً روی نمودار.

### 🌐 مرکز کنترل بازار
*   **پایش چندگانه:** رصد هم‌زمان قیمت‌های طلا، سکه، ارزهای خارجی و رمزارزها.
*   **مبدل ارز پیشرفته:** تبدیل هم‌زمان مقدار پایه به ۷ ارز معتبر جهانی با نرخ‌های واقعی.
*   **دیده بان (Watchlist):** دسته‌بندی دلخواه دارایی‌ها و تنظیم هشدارهای نوسان درصدی.

### 📅 مدیریت جامع تراکنش‌ها
*   **تقویم مالی:** مشاهده فعالیت‌های مالی، یادآورها و خرید/فروش‌ها در تقویم شمسی.
*   **دقت BigDecimal:** انجام تمامی محاسبات مالی با دقت بی‌نهایت جهت جلوگیری از خطای رند کردن.
*   **رهگیری سود واقعی:** محاسبه سود خالص با در نظر گرفتن نرخ تورم و کارمزدها.

---

## 🛠 تکنولوژی‌های به‌کار رفته
*   **UI:** Jetpack Compose با معماری MVI/MVVM.
*   **Database:** Room با رمزنگاری **SQLCipher** و سیستم مهاجرت (Migrations) پیشرفته.
*   **Network:** Retrofit و Moshi برای ارتباط با APIهای بازار.
*   **AI Engine:** Google AI SDK (Gemini Integration).
*   **Background Jobs:** WorkManager برای چک کردن هشدارهای قیمت در پس‌زمینه.

---

## 🚀 راه اندازی سریع (Quick Start)

### ۱. پیش‌نیازها
*   [Android Studio](https://developer.android.com/studio) نسخه Koala به بعد.
*   کلید API از [Google AI Studio](https://aistudio.google.com/).
*   کلید API از [BrsApi.ir](https://brsapi.ir) (جهت دریافت نرخ‌های زنده).

### ۲. تنظیمات محیطی
فایل `.env` را در ریشه پروژه ایجاد کرده و مقادیر زیر را جایگزین کنید:
```env
GEMINI_API_KEY=your_gemini_key_here
BRSAPI_KEY=your_brsapi_key_here
CMC_API_KEY=your_coinmarketcap_key_here
```

### ۳. اجرا
پروژه را در اندروید استودیو باز کرده و روی گوشی یا امولاتور خود اجرا (Run) کنید.

---

## 🛡 امنیت و حریم خصوصی
*   **پایگاه داده رمزنگاری شده:** تمامی اطلاعات مالی شما با استفاده از SQLCipher در حافظه گوشی قفل می‌شود.
*   **دسترسی امن:** پشتیبانی از قفل PIN و تشخیص چهره/اثر انگشت (Biometrics).
*   **عدم خروج داده:** اطلاعات پورتفوی شما به هیچ سرور جانبی ارسال نمی‌شود و فقط به صورت محلی مدیریت می‌گردد.

---

## 📅 نقشه راه (Roadmap)
- [x] مهاجرت کامل به سیستم محاسباتی BigDecimal.
- [x] پیاده‌سازی اندیکاتورهای MACD و Bollinger Bands.
- [x] افزودن تقویم تراکنش‌های مالی (شمسی).
- [ ] قابلیت استخراج گزارش‌های اکسل و PDF.
- [ ] همگام‌سازی ابری رمزنگاری شده (اختیاری).

<div align="center">
<b>با «دارا»، هوشمندانه ثروت خود را مدیریت کنید.</b>
</div>
