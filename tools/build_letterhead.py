"""Build the A4 letterhead that sits under generated challans and memos.

The app draws the customer block, item table and signatures itself, so this page
carries only the artwork: the header strip, the pale watermark, and the brand logo
strip. Run it after replacing any source image:

    python tools/build_letterhead.py

Output goes to both ui/templates/ (for review) and app/src/main/res/drawable-nodpi/
(where the build picks it up).
"""

import os
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Source art. The challan scan supplies the header and the rule above the logos;
# it is already true A4 proportion, so it doubles as the page canvas.
SCAN = os.path.join(ROOT, "ui", "WhatsApp Image 2026-09-02 at 3.27.32 PM.jpeg")
BLANK = os.path.join(ROOT, "ui", "templates", "source", "blank_template.jpeg")
LOGOS = os.path.join(ROOT, "ui", "photo_6107193481030737247_y.jpg")

OUT = os.path.join(ROOT, "ui", "templates", "template_letterhead.png")
RES = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi", "template_letterhead.png")

# Bands measured off the 1131x1600 scan.
HEADER_END = 197        # through the thin rule under the contact line
RULE_BAND = (1488, 1502)  # the rule above the brand logos
LOGO_BOX = (68, 1514, 1067, 1556)  # where the logo strip sits, left/top/right/bottom

# The watermark silhouette lives between these rows on the blank template. The table
# header row is excluded so its printed labels cannot leave ghosts behind.
WATERMARK_BAND = (510, 1226)
WATERMARK_RGB = (253, 233, 222)


def dilate(mask, n):
    for _ in range(n):
        grown = mask.copy()
        grown[1:, :] |= mask[:-1, :]
        grown[:-1, :] |= mask[1:, :]
        grown[:, 1:] |= mask[:, :-1]
        grown[:, :-1] |= mask[:, 1:]
        mask = grown
    return mask


def close(mask, n):
    """Bridge the table grid lines crossing the silhouette, keeping the wrench cut-outs."""
    return ~dilate(~dilate(mask, n), n)


def watermark_layer(shape):
    """Recover the watermark as a flat silhouette from the blank template."""
    if not os.path.exists(BLANK):
        return None

    a = np.asarray(Image.open(BLANK).convert("RGB")).astype(int)
    if a.shape[:2] != shape[:2]:
        a = np.asarray(Image.fromarray(a.astype("uint8")).resize(
            (shape[1], shape[0]), Image.LANCZOS)).astype(int)

    lum = a.mean(axis=2)
    red, blue = a[:, :, 0], a[:, :, 2]

    top, bottom = WATERMARK_BAND
    pale = np.zeros(lum.shape, bool)
    band = slice(top, bottom)
    pale[band] = (lum[band] >= 195) & (lum[band] < 253) & ((red - blue)[band] > 8)

    mask = close(pale, 6)
    mask[:top] = False
    mask[bottom:] = False

    layer = np.full(a.shape, 255, dtype=np.uint8)
    layer[mask] = WATERMARK_RGB
    return layer


def main():
    scan = Image.open(SCAN).convert("RGB")
    width, height = scan.size
    sheet = Image.new("RGB", (width, height), (255, 255, 255))

    sheet.paste(scan.crop((0, 0, width, HEADER_END)), (0, 0))
    sheet.paste(scan.crop((0, RULE_BAND[0], width, RULE_BAND[1])), (0, RULE_BAND[0]))

    # Brand logos come from the clean export rather than the scan.
    logos = Image.open(LOGOS).convert("RGB")
    bbox = Image.eval(logos, lambda p: 255 - p).getbbox()
    logos = logos.crop(bbox)
    left, top, right, bottom = LOGO_BOX
    sheet.paste(logos.resize((right - left, bottom - top), Image.LANCZOS), (left, top))

    out = np.asarray(sheet).astype(int)
    marks = watermark_layer(out.shape)
    if marks is not None:
        out = np.minimum(out, marks.astype(int))

    result = Image.fromarray(out.astype("uint8"))
    result.save(OUT, optimize=True)
    result.save(RES, optimize=True)
    print("wrote %s and %s (%dx%d)" % (OUT, RES, width, height))


if __name__ == "__main__":
    main()
