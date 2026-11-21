# PixelLock

Eigene „Good Lock“-ähnliche App für das Google Pixel (Android 10+), gebaut mit Material 3 / Material You.

## Module

**Make up**

| Modul | Was es tut |
|---|---|
| Home Up | Eigener Startbildschirm: Raster, Icon-Größe und -Form, Dock, Suchleiste, Apps ausblenden, Wischgeste |
| ClockFace | Uhr-Stile (Digital, Analog, Gestapelt), Schrift, 9 Farben inkl. Systemfarbe, Widget |
| LockStar | Sperr-Uhr über dem Sperrbildschirm, Einbrennschutz, Akkustand, Schnelleinstellungen-Kachel |
| Theme Park | Hell/Dunkel und Material-You-Farben |

**Life up**

| Modul | Was es tut |
|---|---|
| NotiStar | Verlauf aller Benachrichtigungen (benötigt Benachrichtigungszugriff) |

## Auf dem Pixel nicht möglich
NavStar, QuickStar (Umgestaltung), Keys Cafe, MultiStar, Routines+ und Sound Assistant benötigen Samsung-eigene Schnittstellen. Sie sind in der App aufgeführt und erklären, warum sie fehlen.

## Bauen
Bei jedem Push baut GitHub Actions automatisch eine APK: Actions → letzter Lauf → Artifacts → PixelLock-APK.
