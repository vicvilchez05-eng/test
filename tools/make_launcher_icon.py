# Genera las capas del icono adaptativo (fondo, logo, monocromo) en mipmap-*dpi a partir de
# docs/design/icono-app-2026-09-07.png. Uso: python3 tools/make_launcher_icon.py [res_dir]
# Requiere Pillow y numpy. Ver HANDOFF.md D-032.
from PIL import Image, ImageDraw, ImageFilter
import numpy as np, os, sys

SRC = os.path.join(os.path.dirname(__file__), '..', 'docs', 'design', 'icono-app-2026-09-07.png')
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')
im = Image.open(SRC).convert('RGB')
a = np.asarray(im).astype(float)

# 1. crop the interior of the tile (inside both gold lines)
x0, x1 = 250, 774
crop = a[x0:x1, x0:x1]
n = crop.shape[0]
print('crop', n)

# 2. estimate the background gradient with a quadratic fit on logo-free pixels
yy, xx = np.mgrid[0:n, 0:n]
cx = cy = n / 2
r = np.hypot(xx - cx, yy - cy)
mask_free = r > 205  # the logo lives inside ~200px of the centre
X = np.stack([np.ones(n*n), xx.ravel(), yy.ravel(), xx.ravel()**2, yy.ravel()**2, (xx*yy).ravel(),
              xx.ravel()**3, yy.ravel()**3, (xx**2*yy).ravel(), (xx*yy**2).ravel()], axis=1)
Xf = X[mask_free.ravel()]
bg = np.zeros_like(crop)
for c in range(3):
    coef, *_ = np.linalg.lstsq(Xf, crop[..., c].ravel()[mask_free.ravel()], rcond=None)
    bg[..., c] = (X @ coef).reshape(n, n)
resid = np.sqrt(((crop - bg) ** 2).sum(axis=2))
print('resid in free zone: mean %.1f max %.1f' % (resid[mask_free].mean(), resid[mask_free].max()))
print('resid in logo zone p50 %.1f p90 %.1f' % (np.percentile(resid[~mask_free], 50), np.percentile(resid[~mask_free], 90)))

# 3. alpha from residual: 0 below lo, 1 above hi; zero outside the logo radius
lo, hi = 30.0, 75.0
alpha = np.clip((resid - lo) / (hi - lo), 0, 1)
alpha[r > 222] = 0
alpha_img = Image.fromarray((alpha * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(0.6))
alpha = np.asarray(alpha_img).astype(float) / 255

# 4. un-premultiply the logo colour against the estimated background so edges stay clean
fg = crop
fg_rgba = np.dstack([fg, alpha * 255]).astype(np.uint8)

# 5. compose on a 108dp canvas where the crop maps to PAD of the canvas
PAD = 0.78   # crop covers 84 % of the canvas -> logo (~66 % of crop) ~ 55 % of canvas, inside the 61 % safe zone
size_full = int(round(n / PAD))
off = (size_full - n) // 2

def pad_edge(arr):
    return np.pad(arr, ((off, size_full - n - off), (off, size_full - n - off), (0, 0)), mode='edge')

bg_full = Image.fromarray(np.clip(pad_edge(bg), 0, 255).astype(np.uint8))
fg_full = Image.new('RGBA', (size_full, size_full), (0, 0, 0, 0))
fg_full.paste(Image.fromarray(fg_rgba, 'RGBA'), (off, off))
mono = Image.new('RGBA', (size_full, size_full), (0, 0, 0, 0))
mono_a = Image.fromarray((np.clip((alpha - 0.25) / 0.5, 0, 1) * 255).astype(np.uint8)).filter(ImageFilter.MedianFilter(5)).filter(ImageFilter.GaussianBlur(0.8))
white = Image.new('RGBA', (n, n), (255, 255, 255, 255)); white.putalpha(mono_a)
mono.paste(white, (off, off))

dens = {'mdpi': 108, 'hdpi': 162, 'xhdpi': 216, 'xxhdpi': 324, 'xxxhdpi': 432}
for d, px in dens.items():
    folder = os.path.join(OUT, f'mipmap-{d}')
    os.makedirs(folder, exist_ok=True)
    bg_full.resize((px, px), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher_background.png'), optimize=True)
    fg_full.resize((px, px), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher_foreground.png'), optimize=True)
    mono.resize((px, px), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher_monochrome.png'), optimize=True)

# 6. previews (only with ICON_PREVIEW=<dir>): circle, rounded square, raw logo and monochrome
PREV = os.environ.get('ICON_PREVIEW')
if not PREV:
    print('done', size_full); sys.exit(0)
os.makedirs(PREV, exist_ok=True)
prev = 432
canvas = bg_full.resize((prev, prev), Image.LANCZOS).convert('RGBA')
canvas.alpha_composite(fg_full.resize((prev, prev), Image.LANCZOS))
vis = int(prev * 72 / 108); o = (prev - vis) // 2
def masked(shape):
    m = Image.new('L', (prev, prev), 0); dr = ImageDraw.Draw(m)
    if shape == 'circle': dr.ellipse([o, o, o + vis, o + vis], fill=255)
    else: dr.rounded_rectangle([o, o, o + vis, o + vis], radius=int(vis * 0.28), fill=255)
    out = Image.new('RGBA', (prev, prev), (245, 247, 250, 255))
    c = canvas.copy(); c.putalpha(m); out.alpha_composite(c); return out.crop((o - 8, o - 8, o + vis + 8, o + vis + 8))
sheet = Image.new('RGBA', (vis * 4 + 80, vis + 16), (245, 247, 250, 255))
sheet.paste(masked('circle'), (0, 0)); sheet.paste(masked('rounded'), (vis + 24, 0))
fgp = Image.new('RGBA', (prev, prev), (245, 247, 250, 255)); fgp.alpha_composite(fg_full.resize((prev, prev), Image.LANCZOS))
sheet.paste(fgp.crop((o - 8, o - 8, o + vis + 8, o + vis + 8)), (2 * (vis + 24), 0))
mp = Image.new('RGBA', (prev, prev), (60, 60, 60, 255)); mp.alpha_composite(mono.resize((prev, prev), Image.LANCZOS))
sheet.paste(mp.crop((o - 8, o - 8, o + vis + 8, o + vis + 8)), (3 * (vis + 24), 0))
sheet.save(os.path.join(PREV, 'preview.png'))
Image.fromarray(np.clip(bg, 0, 255).astype(np.uint8)).save(os.path.join(PREV, 'bg_fit.png'))
print('done', size_full)
