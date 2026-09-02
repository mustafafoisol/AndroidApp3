# AndroidApp3 — Challan & Memo

Android app for Destination Tools & Electronics that captures a challan or memo and
generates the A4 PDF. Built with Kotlin + Jetpack Compose, entirely in CI — no local
Android Studio or JDK required.

## What it does

- **CHALLAN** — S/N, item description and quantity only
- **MEMO** — adds unit price and total price columns plus a grand total
- Line total (quantity x unit price) and the grand total are **calculated automatically**;
  neither is typed. Quantity may be written freely (`24 Pcs`) — the leading number drives
  the maths.
- The preview shows the rasterised first page of the real PDF, so what you see is exactly
  what gets saved
- **Save as PDF** writes an A4 (595x842pt) file wherever you choose, via the system file
  picker — no storage permission needed

## Configuration

| Setting | Value |
| --- | --- |
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material 3) |
| `minSdk` | 28 (Android 9 Pie) |
| `targetSdk` / `compileSdk` | 35 |
| JDK | 17 |
| Gradle / AGP | 8.11.1 / 8.7.3 |
| App ID | `com.mustafafoisol.androidapp3` |

## Getting a build

Every push to `main` and every PR runs [Android CI](.github/workflows/android.yml).
The debug APK is attached to the run as the `app-debug-apk` artifact:

1. Open the **Actions** tab
2. Pick the latest **Android CI** run
3. Download **app-debug-apk** from the Artifacts section

You can also trigger a build manually via **Run workflow** on that page.

## Layout

```
app/src/main/java/com/mustafafoisol/androidapp3/
  MainActivity.kt          entry point
  ui/theme/                colors, typography, Material 3 theme
ui/                        design source files (see ui/README.md)
gradle/libs.versions.toml  dependency versions
```

## Designs

Drop UI designs into [`ui/`](ui/). Screens get implemented as Compose composables under
`app/src/main/java/com/mustafafoisol/androidapp3/`, with the palette and type ramp
lifted into `ui/theme/`.
