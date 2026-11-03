# Begin

A tiny Android app. A home-screen widget and one screen that give you a short
message to help you start: affirmations, why the task matters, a tiny first step,
restart messages for coming back after a break, and faith lines (prayer, scripture,
sayings). English or Amharic.

## Build it (Android Studio, about 10 minutes the first time)

1. Unzip the project.
2. Android Studio: **File > Open** and choose the `begin-app` folder (the one with `settings.gradle.kts`).
3. Wait for "Gradle sync" to finish (it downloads things, so you need internet).
   - If it offers to install a missing "Android SDK Platform 34", click Install.
   - If it offers to "Upgrade" the Android Gradle Plugin, you can click that or skip it.
4. **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. When it says "APK(s) generated successfully", click **locate**. The file is `app-debug.apk`
   (in `app/build/outputs/apk/debug/`).

## Put it on your phone

1. Send `app-debug.apk` to your phone (USB cable, Telegram "Saved Messages", Google Drive, email).
2. Open the file on the phone and tap **Install**. Android will ask you to allow installs
   from that app (Files, Telegram, Chrome...). Allow it. This is normal for apps outside Play Store.
3. Long-press an empty spot on your home screen > **Widgets** > find **Begin** > drag it out.

Or, with the phone plugged in and USB debugging on, press the green Run button in Android Studio.

## Using it

- **Widget:** one widget that you resize yourself: long-press it, then drag its edges (it can be a thin strip or a big card).
  Tap the right half for the next message, the left half to go back. Double-tap is a setting (Settings > Widget > Double-tap: Off, Saved or Mine): the right half goes forward and the left half goes back through that list. Normal taps stay as they are.
  The small arrow in the corner opens the app. It also changes by itself about once an hour.
- **App:** the top-right switch picks the language (Eng or Am; tap the selected one again for both).
  The chips under the title pick the type. On the card, tap the right half (or swipe left) for the next
  message and the left half (or swipe right) to go back. There is no Push me button any more.
- **Settings (gear at the top right):** Appearance (color, Glass, Lines Off/Soft/Clear), Language and fonts,
  Widget text size (with a live preview), Reminders, Daily verse, Vibration, and your Saved and My messages lists.
- **Resizing the widget:** long-press the widget on your home screen, then drag its edges.
  It can be made small (a thin strip) or large. The text re-fits itself every time.

## New in this version

- **Saved:** tap the heart on the card to save a message. Saved messages show up three times as often.
  The "♥ Saved" chip shows only saved messages, and Settings > Messages > Saved lists them all (tap one to show it, tap its heart to remove it).
- **My messages:** Settings > Messages > My messages. Write a line, pick its type, tap Add.
  Tap a line to edit it, tap the cross to delete it. Your lines show up twice as often.
  The app works out the language by itself.
- **Reminders:** Settings > Reminders > "+ Add time". You can add up to 6 times a day.
  Tap a time to remove it. Android asks for notification permission the first time.
  To save battery, Android may deliver a reminder a few minutes late.
- **Daily verse:** Settings > Daily verse > On. One faith line every morning (default 7:00 AM, tap the time to change).
  It also appears on your widget. It uses the language chosen at the top right.
- **Vibration:** Settings > Vibration. A very short soft buzz when a new message appears.

## About the glass

- In the app, the card really blurs the colors behind it on Android 12 and newer.
  On older phones the card is tinted glass without blur.
- A home-screen widget cannot blur your wallpaper (Android does not allow it). The widget
  paints its own glass look instead: soft colored glow under a tinted, shiny panel.

## Change things

- **Fonts:** files are in `app/src/main/res/font/`. All are open-source (SIL Open Font License).
- **Name:** `app/src/main/res/values/strings.xml`, line `app_name`.
- **Messages:** `app/src/main/java/com/pushme/app/Messages.kt`. Add a line inside the right group.
  Keep each line in quotes and end it with a comma, except the last line of a group.
- **Scripture:** the verse wording was written from memory. Please check it against your own Bible
  and correct anything that differs.



- Notifications at times you choose.
- Adding your own messages inside the app.

## New in 1.7

- **Time-aware tone** (Settings > Messages): gentle in the morning (5-11), firmer in the afternoon (12-17),
  easing off in the evening (18-21), calm at night. It only applies when no single message type is chosen.
- **More messages:** about 70 new lines (2-minute start, never miss twice, done beats perfect, new faith lines).
  Scripture lines are written from memory, so please compare them with your own Bible.
- **Aurora:** a new dark theme (emerald and violet over a deep night sky), so light and dark are 5 and 5.
- **Softer widget colors,** especially on light themes (softened colors, gentler shine, slate text from the middle glass level up).
- **Balanced text:** lines now break evenly, with no lonely single word on the last line.

## New in 1.8

- **Animations** (Settings > Feedback): messages slide in, the card follows your finger, a heart bursts when you save.
  One switch turns all motion off. Until you choose, it follows your phone's own animation setting.
- **Live widget preview** (Settings > Widget): shows the widget at the size of the one on your home screen, on a dark
  wallpaper-like frame; it changes the moment you pick a text size. Tap the picture to try another message.
- **Share:** the share button on the card turns the message into a picture and opens the share sheet.
- **Backup:** Settings > Backup saves your own messages and saved ones to a file, and restores them (nothing is duplicated).
- **Auto theme, bigger tap targets, themed launcher icon** (Settings).
- The "Mix" language choice and the mixed English/Amharic built-in lines were removed.

## New in 2.0

- **2-minute start timer:** the small "▶ 2 min start" pill at the bottom of the card. Tap it, do the task for two minutes, and a
  three-pulse buzz tells you when time is up (if you leave the app, a notification does). Tap again to stop. After two minutes
  you may stop with a win, or keep going.
- **Different haptic feels:** a tap for a new message, a light tick for chips and switches, two soft thumps when you save, and three rising
  pulses when the timer ends. The one Vibration switch still turns all of them off.
- **Drifting glass** (Settings > Feedback): in the app the colors float slowly. A widget cannot animate live, so its glow moves to a
  new spot each time it refreshes (a full lap takes 3 hours). One switch turns it off for both.
- **Morning check-in** (Settings > Reminders): one notification, "What will you begin today?", at a time you choose (default 8:00 AM).
- Parked for later: a "Today's one thing" line you type and the widget shows.

## New in 2.1

- Changing the type, language, Saved or Mine filter now starts a fresh history, so "Back" never shows a message that does not fit the filter you just chose.

## New in 2.2

- **Words fade in softly:** a new message appears word by word (about a second at most). It follows the Animations switch.
- **Time of day** (Settings > Appearance): a soft peach glow in the morning (peak around 8 AM) and a cooler blue tone from 6 PM into the night, on top of your chosen theme, in the app and on the widget. Switch it off any time.
- **Long-press the card to copy** the message (already in 2.0).

## New in 2.3

- **Widget vibration:** tapping the widget now gives the same soft buzz as in the app (also on double-taps). It follows the Vibration switch.
- **No pop-ups on the heart:** saving or unsaving only shows the heart itself (a pop, a small burst, a buzz).
- **More fonts** (Settings > Language and fonts, scroll the row sideways). Ten of each, all open-source:
  - English: Inter, Poppins, DM Sans, Nunito, Playfair, Lora, Carlito, Manrope, Fraunces, Space Grotesk.
  - Amharic: Noto Sans, Noto Serif, Abyssinica, Lessan, Sadiss, Abay (these three from your collection), Noto Sans Light,
    Noto Serif Light, Noto Sans Black, FreeSerif. "System" is also there as an 11th choice.
  - The two fonts without a license (WashRa, Wookianos) were removed. Licenses are listed in `FONT-LICENSES.md`.

## New in 2.4

- Fonts: ten open-source choices each for English and Amharic (see above), downloaded from the Google Fonts library or taken from your collection.

## New in 2.5

- **First look fixed:** the Eng/Am switch no longer stretches to the height of the screen when the app opens (its highlight now has the same height as the row of languages).
- **Widget double-tap, as you meant it:** double-tap the right half to step through your own messages, the left half to step through your saved ones, one by one. Single taps work as before and the widget's filter is no longer changed.

## New in 2.6

- **Heart on notifications:** the morning verse and your reminder notifications now have a "♥ Save" button. One tap saves that message (a soft double-thump buzz), the widget updates, and the notification switches quietly to "♥ Saved".

## New in 2.6

- **Double-tap setting** (Settings > Widget > Double-tap): choose **Off**, **Saved** or **Mine**. With Saved or Mine, double-tapping the right half of the widget goes forward through that list, and the left half goes back (it carries on from where it stopped and wraps around). With Off, every tap is a normal tap.
- **Like button on notifications:** the morning verse and the reminder notifications have a "♥ Save" button. Tapping it saves the message and the notification changes to "♥ Saved".

## New in 2.9

- **The 2-minute timer follows you out of the app:** while it runs, a quiet notification with one big countdown number shows in the notification shade (no sound). It ticks by itself and disappears when the timer ends or you stop it. (A countdown on the widget was tried and removed: the widget can't count down reliably.)

## New in 2.8

- **Double-tap is gone, replaced by a simple Gesture:** in Settings > Widget, the row is now called Gesture (Off / Saved / Mine). Tap the top of the widget to go forward through that list, the bottom to go back. The middle halves still go back / next through everything. Faint arrows mark the top and bottom zones.
- Single taps are instant again (no waiting).
- Android widgets cannot detect real swipes, so "up / down" are tap zones. They switch off on very thin widgets (under about two rows tall).

## New in 2.7

- **Double-tap no longer moves the message first:** with a double-tap list chosen, a single tap on the widget now waits about a third of a second to see whether a second tap follows. A double-tap only steps through Saved or Mine; a single tap does its normal move (slightly later). Set Double-tap to Off in Settings to get instant single taps back.
