# Android holiday backgrounds

Twenty-eight original, text-free flat SVG backgrounds and a holiday mapping manifest.

## Import

1. In Android Studio, right-click `app/src/main/res/drawable` and select **New > Vector Asset**.
2. Choose **Local file (SVG, PSD)** and import each SVG in `svg/`.
3. Copy `holiday_background_manifest.csv` into the project (for example `app/src/main/assets/`) and use `android_drawable` as the resource name.

All artwork is original for this project, contains no text/logos/flags, and is designed as a 2:1 button/card background. Keep the left 55% free for countdown text.

`local_drawable_overrides` has higher visual specificity than the general region mapping. Resolve it first, then `external_asset_overrides` (only after licence approval), then the background named in the region's holiday row.

## User-supplied external illustrations

`holiday_background_manifest.json` also includes the supplied Unsplash and iStock URLs as optional overrides. They are **not** bundled in this archive. Entries with `approval_required: true` must be downloaded only after confirming an active Unsplash+ subscription or purchasing the iStock licence. Until then, use the listed `fallback_drawable`.
