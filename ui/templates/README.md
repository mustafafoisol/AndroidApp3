# Letterhead artwork

Put the blank letterhead scan here, then it gets copied into
`app/src/main/res/drawable-nodpi/` to be picked up by the build.

Resource names, in the order the app looks for them:

| File | Used for |
| --- | --- |
| `template_challan.png` | CHALLAN only, overrides the shared file |
| `template_memo.png` | MEMO only, overrides the shared file |
| `template_letterhead.png` | both, when no per-type file exists |

Requirements:

- A4 proportions (1:1.414), portrait, ideally 1654x2339 px (200dpi) or larger
- **Letterhead only** — header strip, watermark and brand footer
- **Blank middle** — no table grid, no column headers, no customer data, no item rows
- Full bleed, reaching all four page edges

With no file present the app draws its own letterhead and brand strip, so the build
stays green either way.
