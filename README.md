# PixelLock

Eigene „Good Lock“-ähnliche App für das Google Pixel (Android 10+).

## Module (Version 0.1)

| Modul | Vorbild | Was es tut |
|---|---|---|
| Launcher | Home Up | Eigener Startbildschirm: App-Raster, Spalten, Icon-Größe, Namen an/aus, Uhr oben |
| Uhr-Stile | ClockFace | Digital / Analog / Gestapelt, 8 Farben, optional Sekunden, Live-Vorschau |
| Sperr-Uhr | LockStar (teilweise) | Vollbild-Uhr, die auch bei gesperrtem Gerät erscheint |
| Uhr-Widget | – | Widget für den Startbildschirm |

## Bauen

1. Ordner `PixelLock` in **Android Studio** öffnen (File → Open).
2. Wenn Android Studio nach einem Gradle-/Plugin-Update fragt: zustimmen. Der Gradle-Wrapper wird dabei automatisch erzeugt.
3. Pixel per USB anschließen (Entwickleroptionen + USB-Debugging an) und auf **Run** drücken.
4. In der App „PixelLock als Startbildschirm festlegen“ antippen. Langer Druck auf die Uhr im Launcher öffnet die Einstellungen.

## Was auf dem Pixel nicht geht

Die meisten übrigen Good-Lock-Module greifen mit Samsung-eigenen Schnittstellen (One UI) tief ins System ein: Schnellmenü (QuickStar), Navigationsleiste (NavStar), Tastenbelegung (Keys Cafe), echter Sperrbildschirm (LockStar), Multitasking (MultiStar). Auf einem Pixel gibt es dafür keine offenen Schnittstellen. Möglich wären nur Teilersatz über Bedienungshilfen oder Shizuku/ADB – das wäre ein späterer Ausbau.
