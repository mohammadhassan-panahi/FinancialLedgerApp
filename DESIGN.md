---
name: Midnight Obsidian & Indigo Glass
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#c7c4d7'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#908fa0'
  outline-variant: '#464554'
  surface-tint: '#c0c1ff'
  primary: '#c0c1ff'
  on-primary: '#1000a9'
  primary-container: '#8083ff'
  on-primary-container: '#0d0096'
  inverse-primary: '#494bd6'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffb95f'
  on-tertiary: '#472a00'
  tertiary-container: '#ca8100'
  on-tertiary-container: '#3e2400'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-lg:
    fontFamily: Vazirmatn
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-sm:
    fontFamily: Vazirmatn
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Vazirmatn
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: 0em
  headline-md:
    fontFamily: Vazirmatn
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: 0em
  title-lg:
    fontFamily: Vazirmatn
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: 0em
  title-md:
    fontFamily: Vazirmatn
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0em
  body-lg:
    fontFamily: Vazirmatn
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: 0em
  body-md:
    fontFamily: Vazirmatn
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0em
  body-sm:
    fontFamily: Vazirmatn
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  label-lg:
    fontFamily: Vazirmatn
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0em
  label-md:
    fontFamily: Vazirmatn
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Vazirmatn
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 1.5rem
  space-2xl: 2rem
  space-3xl: 2.5rem
  gutter-mobile: 1rem
  margin-mobile: 1rem
  bottom-nav-height: 4.5rem
  header-safe-top: 3rem
---

## Brand & Style

This design system delivers an elite, bespoke private wealth management experience tailored for high-net-worth individuals navigating modern multi-asset portfolios (equities, gold, currencies, and fixed income). The aesthetic blends Material 3 structural discipline with deep nocturnal glassmorphism, instilling precision, calm authority, and unwavering security.

### Visual Philosophy
- **Dark Elegance:** Grounded in layered nocturnal tones (obsidian slate and midnight indigo) to reduce cognitive load during high-stakes financial decisions.
- **Luminescent Hierarchy:** Information hierarchy is created through translucent glass tiers, precise 1px light-refracting borders, and focused ambient glows rather than noisy structural fills.
- **RTL-Native Precision:** Built from the ground up for Persian typography, honouring natural right-to-left reading flow, balanced letter tracking, and optical alignment for bilingual financial metrics and Western/Eastern Arabic numerals.

## Colors

The palette is engineered for high-contrast legibility in low-light environments, relying on deep indigo shades and dark slate foundations accented by functional status indicators.

### Foundation & Surfaces
- **Canvas / Background:** `#0F172A` (Obsidian Slate 900)
- **Surface Elevation 1 (Container Low):** `#1E293B` with 70% opacity and 24px backdrop blur.
- **Surface Elevation 2 (Container High):** `#1E1B4B` (Deep Midnight Indigo) layered with 40% opacity.
- **Active Interactive Surface:** `#312E81` to `#4338CA` with `#6366F1` edge highlights.

### Functional Accents
- **Primary / Brand Accent:** `#6366F1` (Indigo Electric) — primary actions, active navigation states, selected chart ranges.
- **Gain / Growth:** `#10B981` (Emerald Core) and `#059669` (Deep Emerald) — positive yield, upward trends, executed buy orders.
- **Loss / Decline:** `#F43F5E` (Rose Coral) and `#E11D48` (Deep Crimson) — negative balances, portfolio drawdown, stop-loss triggers.
- **Asset Tier / Gold Standard:** `#F59E0B` (Refined Amber Gold) — gold coin metrics, precious metal holdings, and VIP tier badges.

### Text & Border Tokens
- **Text High-Emphasis:** `#F8FAFC` (Slate 50)
- **Text Medium-Emphasis:** `#94A3B8` (Slate 400)
- **Text Disabled / Subtle:** `#475569` (Slate 600)
- **Glass Specular Border:** `rgba(255, 255, 255, 0.08)` to `rgba(255, 255, 255, 0.15)`

## Typography

Typography is calibrated for Persian script with bidirectional financial figures. Vazirmatn is deployed across all tiers for its open counters, balanced ascenders/descenders, and clear Persian and Arabic numerical glyphs.

### Typographic Rules
- **Bidirectional Numerals:** Ensure currency values and percentage shifts maintain strict tabular figure alignment (`font-variant-numeric: tabular-nums`) so that data columns remain readable regardless of script mix.
- **Vertical Metric Alignment:** Persian typography requires 8-15% more vertical line-height than Latin scripts to prevent diacritic clipping; all tokens have line heights calibrated specifically for Persian ligatures.
- **Hierarchy Distinction:** Display sizes (`display-lg`, `display-sm`) are reserved for net worth valuations and principal balances. Secondary metrics utilize `label-md` in uppercase or semibold weights with medium-emphasis coloration (`#94A3B8`).

## Layout & Spacing

The layout system is tailored for high-density mobile interfaces following Material 3 guidelines and native Android edge-to-edge conventions.

### Structural Mechanics
- **Base Grid Unit:** Strictly built on a 4px/8px incremental rhythm.
- **Mobile Margin System:** Fixed 16px screen margins (`margin-mobile`) with 12px or 16px inter-card gutters.
- **Edge-to-Edge Safe Zones:** Background gradients and frosted glass elements extend behind the Android translucent status bar and gesture navigation bar, reserving dynamic top padding (`header-safe-top`) and bottom buffer (`bottom-nav-height` + Android gesture inset).
- **RTL Layout Mirroring:** Horizontal padding, leading icons, indicators, and chart time axes automatically mirror for RTL: primary actions and summary figures sit on the right, directional chevron arrows flip automatically, and chart progress flows leftward.

## Elevation & Depth

Visual depth is achieved through glassmorphism and subtle directional light sources, rather than opaque grey shadows.

### Glassmorphic Layers
- **Base Layer (Elevation 0):** Pure dark background (`#0F172A`) augmented by static ambient radial flares: a subtle Indigo glow (`rgba(99, 102, 241, 0.08)`) centered behind the hero balance card, and an optional subtle Emerald or Amber glow depending on overall market performance.
- **Glass Container (Elevation 1):** Main dashboard cards use `rgba(30, 41, 59, 0.65)` with `backdrop-filter: blur(20px)` and an inner highlight rim of `1px solid rgba(255, 255, 255, 0.08)`.
- **Floating Modals & Sheets (Elevation 2):** Bottom sheets, asset transaction sheets, and contextual drawers use `rgba(30, 27, 75, 0.85)` with `backdrop-filter: blur(32px)` and a top boundary highlight `1px solid rgba(99, 102, 241, 0.25)`.
- **Elevated Interactive Floating Dock (Elevation 3):** Bottom navigation floats 16px above the physical bottom edge, using `rgba(15, 23, 42, 0.75)` with `backdrop-filter: blur(28px)`, surrounded by an ambient drop shadow: `0 12px 32px -4px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.08)`.

## Shapes

The design adopts rounded shapes (Factor 2: 16px default container corner radius, 24px for prominent cards) inspired by Material 3 rounded card semantics.

### Surface Form Factors
- **Primary Hero Cards:** `rounded-2xl` (16px to 24px) providing soft, organic contrast against strict financial data tables.
- **Action Buttons & Inputs:** `rounded-xl` (12px) to guarantee tactile tap reassurance.
- **Filter Chips & Status Badges:** `rounded-full` (pill shape) for asset classification tags (e.g., Gold, Crypto, FX, Bourse).
- **Interactive Press Feedback:** Subtle continuous corner transitions (`smooth corners / squircle` where supported) to emulate high-end hardware finishes.

## Components

### Buttons
- **Primary Call to Action:** Solid deep indigo-to-violet gradient (`linear-gradient(135deg, #6366F1, #4338CA)`), white semibold Persian text, `12px` border radius, subtle outer glow (`0 4px 16px rgba(99, 102, 241, 0.35)`). Active state scales down to `98%`.
- **Secondary Action:** Frosted glass fill (`rgba(255, 255, 255, 0.05)`), border `1px solid rgba(255, 255, 255, 0.12)`, text `#F8FAFC`.
- **Destructive / Sell Action:** Rose tinted glass surface with `#F43F5E` label and border `rgba(244, 63, 94, 0.3)`.

### Cards & Wealth Metric Tiles
- **Hero Net Worth Card:** Frosted dark slate container (`#1E293B` at 70%) with a dual inner gradient overlay, showing total valuation in Persian numerals, real-time gain percentage chip (`#10B981`), and quick actions (Deposit, Transfer, Analytics).
- **Asset Allocation Mini-Card:** Glass surface displaying asset icon (e.g., Gold coin, Stock candle), Persian asset title, localized holding amount, and mini sparkline chart.

### Financial Sparklines & Tickers
- **Trend Charts:** Minimalist vector paths with soft stroke (`2px`) in Emerald Green for profit or Rose Red for loss, accompanied by an underlying vertical gradient fill terminating to 0% opacity.
- **Live Price Marquee / Ticker:** Horizontal smooth-scrolling ribbon with semi-transparent separators, monospace numerals, and direction chevrons indicating basis point fluctuations.

### Chips & Asset Filters
- **Filter Chips:** Pill-shaped (`rounded-full`), height `32px`, horizontal padding `12px`. Inactive state: `rgba(255, 255, 255, 0.05)`. Active state: `#312E81` background with glowing `#6366F1` 1px perimeter and high-contrast white text.

### Form Inputs & Keypads
- **Numeric Wealth Input:** Clean, bottom-bordered or softly framed surface (`rgba(255, 255, 255, 0.04)`), displaying large Persian numerals, clear suffix units (Toman, USD, Gram), and an integrated quick-percentage selector (`25%`, `50%`, `100%`).
- **Focus States:** Outer perimeter transition to `#6366F1` with an inner diffuse indigo halo.

### Floating Bottom Navigation Dock
- Compact floating capsule anchored above the Android gesture bar.
- Five core icons (Portfolio, Markets, Trade/Exchange, Vault, Profile). Active tab reveals a glowing pill indicator with subtle emerald or indigo under-glow.