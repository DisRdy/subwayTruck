# Subway Truck GUI Handover

## Purpose
This document is for the next agent who will continue tweaking the Swing GUI for `Subway Surfer/Truck`.
It explains the current UI structure, key files, styling approach, and the current overlap/layout issues.

## Main file to edit
- `SubwaySurferGame.java`

## Current UI structure
- `SubwaySurferGame` is a `JFrame` containing a `CardLayout` with two screens:
  - `HomePanel`
  - `GamePanel`
- `HomePanel` uses absolute positioning (`setLayout(null)`) and custom painting.
- `GamePanel` also uses absolute positioning and handles all game rendering in `paintComponent`.

## Key constants and styles
- `SAFE_MARGIN = 20` is used for padding from screen edges.
- Colors:
  - `BG` background
  - `ROAD` road area
  - `LANE_LINE` lane dividers
  - `PANEL_BG` semi-transparent panels
  - `GOLD` accent color
- Font constants:
  - `FONT_TITLE`
  - `FONT_SUBTITLE`
  - `FONT_BODY`
  - `FONT_HEADLINE`
  - `FONT_SMALL`

## Current UI features
### Home screen
- Gradient background with a translucent frame.
- Animated title glow using `Timer animator`.
- Two buttons: `PLAY` and `EXIT`.
- Info section with "Why play?" and top players preview.
- Safe margins are intended, but layout is still using fixed coordinates.

### Game screen
- Padded road area with lane dividers.
- Rounded player and obstacle drawing.
- HUD panel on the top left.
- Leaderboard panel on the top right.
- Bottom controls panel.
- Name input and submit button are positioned near the bottom.
- Fade-in animation for the game screen.

## Known issues to fix
- Absolute positioning can still cause overlap on small layout changes.
- `HomePanel` content and buttons may overlap if text grows.
- `GamePanel` bottom input controls and bottom info bar are close; spacing should be improved.
- `GamePanel` uses many hard-coded coordinates, which makes future layout tweaks fragile.
- `JTextField` and submit button should remain visible and fully clear of the controls panel.
- Consider moving `GamePanel` HUD and leaderboard elements into explicit safe areas rather than absolute coordinates.

## Recommended approach for tweaks
1. Start in `SubwaySurferGame.java`.
2. Keep `SAFE_MARGIN` as the base padding constant, but use it consistently for all component positions.
3. Prefer relative positions based on `W`, `H`, and safe margins.
4. Consider replacing `null` layout with simple layout managers for `HomePanel` buttons and input overlay if possible.
5. Test after each change by recompiling and running with the connector classpath:
   - `javac -cp ".;mysql-connector-j-9.7.0.jar" *.java`
   - `java -cp ".;mysql-connector-j-9.7.0.jar" SubwaySurferGame`

## Useful sections to inspect
- `HomePanel.button(...)`
- `HomePanel.paintComponent(...)`
- `GamePanel.setupInput()`
- `GamePanel.drawRoad(...)`
- `GamePanel.drawHUD(...)`
- `GamePanel.drawLeaderboard(...)`
- `GamePanel.drawControls(...)`
- `GamePanel.drawGameOver(...)`

## Suggested GUI improvements
- Add safer vertical spacing between major sections.
- Increase bottom padding for the input area.
- Use `Insets` or an explicit margin variable inside `GamePanel` for game objects.
- Consider adding a translucent overlay behind the input area and controls when game over is active.
- Keep the home screen title and info panel away from the interactive buttons.

## Notes for the next agent
- This project is a lightweight Swing app; avoid adding heavy external dependencies.
- The handbook should stay focused on the UI, not the database.
- If you add new animation timers, ensure they stop when switching panels to avoid background repaint churn.

## Deliverable goal
Produce a polished, non-overlapping Swing GUI with consistent edge spacing and simple visual hierarchy.

## Recent UI changes (automated edits)
The following edits were made to reduce absolute positioning and improve spacing. These were applied directly to `SubwaySurferGame.java`.

- Replaced `null` layouts in `HomePanel` and `GamePanel` with simple layout managers (`BorderLayout`, `FlowLayout`, `BoxLayout`) to anchor buttons and input controls.
- Moved home screen buttons into a bottom `BoxLayout` panel to avoid overlap with title and info panels.
- Created a bottom `inputPanel` in `GamePanel` (vertical BoxLayout) to keep the name `JTextField` and `Submit` button above the controls area and consistent across window sizes.
- Made the main window non-resizable and removed system LookAndFeel for a consistent mobile appearance.
- Updated `endGame()` and input visibility methods to show/hide the new `inputPanel` instead of absolute-positioned components.

## Mobile UI optimization (latest iteration)
- Changed window dimensions from 400×700 to 360×640 (standard mobile phone size).
- Reduced SAFE_MARGIN from 20 to 12 pixels for better mobile screen utilization.
- Scaled down all player and obstacle sizes for the smaller screen (PLAYER_W/H: 40→35/50, OBS_W/H: 40→35/45).
- Font sizes optimized for mobile readability:
  - Title: 48→36px (still eye-catching but not oversized)
  - Subtitle: 16→13px
  - Body text: 14→12px
  - Headlines: 18→14px
  - Small text: 12→10px
- Home screen redesigned for mobile:
  - Title shortened to "🚛 SUBWAY" with subtitle "Mobile edition"
  - Condensed content: "Why play?" section now 4 lines instead of lengthy text
  - Best score displayed inline in the info panel
- Game screen optimizations:
  - HUD panel: 220×100→180×90 with adjusted font sizes
  - Leaderboard: 220×160→180×130, showing "🏆 TOP 5" instead of full title
  - Controls panel: 90px→80px height, condensed text (e.g., "← → Move" instead of "← / → : Move lanes")
  - Game-over dialog sized appropriately for mobile with shorter text
  - Submit button and name field: 220×34→200×36 with better mobile touch targets
- Bottom buttons now stack vertically instead of horizontally to fit mobile layout
- Improved input panel padding: adjusted to 110px bottom (was 140px on desktop)

## How to test (quick smoke test)
1. Compile: `javac *.java`
2. Run: `java SubwaySurferGame`
3. Verify:
   - Window is 360×640 (mobile-sized, non-resizable)
   - All UI elements scale properly for a phone screen
   - Buttons are touch-friendly (48px tall for mobile)
   - Text is readable at mobile font sizes
   - Game controls and HUD fit neatly in the mobile viewport
   - During game-over, the name input and submit stay visible above the controls panel

## Notes & next recommendations
- The mobile UI is now optimized for portrait orientation (360×640).
- All spacing and fonts are calibrated for fingers rather than mouse clicks.
- Consider adding landscape support if needed by rotating the screen layout.
- Font sizes can be fine-tuned further if needed based on user feedback (especially for readability on different phone sizes).
- The "why play?" section on the home screen is now more concise to fit mobile screens; consider making it collapsible if expansion is desired.

If you want, I can run the app now and verify it displays correctly on the mobile screen size.
