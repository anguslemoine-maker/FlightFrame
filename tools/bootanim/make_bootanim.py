#!/usr/bin/env python3
"""Flight Frame boot animation: radar rings, a plane flying an arc, the name around the ring, then a radar sweep loop.
   python3 make_bootanim.py [--w 1080 --h 1920] --out bootanimation.zip"""
import argparse, math, os, shutil, zipfile
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ap = argparse.ArgumentParser()
ap.add_argument('--w', type=int, default=1080); ap.add_argument('--h', type=int, default=1920)
ap.add_argument('--out', default='bootanimation.zip'); ap.add_argument('--font', default='/usr/share/fonts/opentype/inter/Inter-SemiBold.otf')
ap.add_argument('--preview', action='store_true')
a = ap.parse_args()
W, H = a.w, a.h
SS = 2
BOX = int(min(W, H) * 0.98)            # emblem region (square), drawn supersampled
R = BOX * 0.30                          # radar radius
C = BOX * SS / 2
BG = (7, 9, 13); RING = (34, 46, 64); AMBER = (255, 178, 56); CYAN = (79, 209, 255); WHITE = (238, 242, 247); DIM = (125, 136, 152)
FPS, N0, N1 = 20, 60, 40                # intro 3 s, loop 2 s

def ease(x): x = max(0.0, min(1.0, x)); return 1 - (1 - x) ** 3
def lerp(c1, c2, t): return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

PLANE = [(0,-7),(1.2,-2),(7,1.5),(7,3),(1.2,1.5),(0.9,5),(2.6,6.4),(2.6,7.4),(0,6.7),(-2.6,7.4),(-2.6,6.4),(-0.9,5),(-1.2,1.5),(-7,3),(-7,1.5),(-1.2,-2)]
def bez(s):
    p0, p1, p2 = (-R*0.92, R*0.42), (-R*0.10, -R*0.80), (R*0.92, -R*0.30)
    x = (1-s)**2*p0[0] + 2*(1-s)*s*p1[0] + s*s*p2[0]; y = (1-s)**2*p0[1] + 2*(1-s)*s*p1[1] + s*s*p2[1]
    dx = 2*(1-s)*(p1[0]-p0[0]) + 2*s*(p2[0]-p1[0]); dy = 2*(1-s)*(p1[1]-p0[1]) + 2*s*(p2[1]-p1[1])
    return x, y, math.atan2(dy, dx)

TOP, BOT = 'FLIGHT FRAME', 'OVERHEAD  \u00b7  RIGHT NOW'
font = ImageFont.truetype(a.font.replace('SemiBold', 'Bold'), int(R * 0.15 * SS))
font2 = ImageFont.truetype(a.font, int(R * 0.085 * SS))
TR = (R + R * 0.20) * SS                # top text baseline radius
BR = (R + R * 0.17) * SS                # bottom text radius
glyph_imgs = {}
def glyph(ch, col, f):
    k = (ch, col, id(f))
    if k not in glyph_imgs:
        s = int(R * 0.3 * SS); im = Image.new('RGBA', (s, s), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
        d.text((s / 2, s / 2), ch, font=f, fill=col + (255,), anchor='mm'); glyph_imgs[k] = im
    return glyph_imgs[k]
def arc_layout(text, f, radius, track, centre, clockwise):
    ws = [f.getlength(c) + track for c in text]; tot = sum(ws) - track
    th = centre - (tot / 2) / radius * (1 if clockwise else -1)
    out = []
    for c, w in zip(text, ws):
        mid = th + ((w - track) / 2) / radius * (1 if clockwise else -1)
        out.append((c, mid)); th += w / radius * (1 if clockwise else -1)
    return out
TOPL = arc_layout(TOP, font, TR, font.size * 0.32, -math.pi / 2, True)
BOTL = arc_layout(BOT, font2, BR, font2.size * 0.30, math.pi / 2, False)

BLIPS = [(0.55, 40), (0.78, 165), (0.35, 255), (0.9, 300)]   # (radius fraction, angle deg from north, clockwise)

def frame(t):
    """t: 0..N0-1 intro, N0.. loop."""
    im = Image.new('RGB', (BOX * SS, BOX * SS), BG)
    lay = Image.new('RGBA', im.size, (0, 0, 0, 0)); d = ImageDraw.Draw(lay)
    intro = t < N0
    # rings
    for i, f in enumerate([0.25, 0.5, 0.75, 1.0]):
        p = ease((t - i * 3) / 16) if intro else 1
        if p <= 0: continue
        r = R * f * SS * (0.6 + 0.4 * p)
        col = lerp(BG, CYAN if f == 1.0 else RING, p * (0.55 if f == 1.0 else 1))
        d.ellipse((C - r, C - r, C + r, C + r), outline=col + (255,), width=int(3 * SS if f == 1.0 else 2 * SS))
    pc = ease((t - 8) / 14) if intro else 1
    if pc > 0:
        col = lerp(BG, RING, pc * 0.8) + (255,)
        d.line((C - R * SS * 1.04, C, C + R * SS * 1.04, C), fill=col, width=SS); d.line((C, C - R * SS * 1.04, C, C + R * SS * 1.04), fill=col, width=SS)
    # sweep: rotates 360 deg per loop; fades in over the last part of the intro
    sweep_a = ((t - N0) * 360.0 / N1) % 360
    sa = 1.0 if not intro else ease((t - 42) / 18)
    if sa > 0:
        sw = Image.new('RGBA', im.size, (0, 0, 0, 0)); sd = ImageDraw.Draw(sw)
        rr = R * SS * 0.995
        for k in range(60):                       # trailing gradient wedge (60 deg)
            a0 = sweep_a - 90 - k - 1; al = int(90 * sa * (1 - k / 60) ** 2)
            sd.pieslice((C - rr, C - rr, C + rr, C + rr), a0, a0 + 1.2, fill=CYAN + (al,))
        sd.line((C, C, C + rr * math.cos(math.radians(sweep_a - 90)), C + rr * math.sin(math.radians(sweep_a - 90))), fill=CYAN + (int(200 * sa),), width=2 * SS)
        lay = Image.alpha_composite(lay, sw); d = ImageDraw.Draw(lay)
        for fr, ang in BLIPS:                      # blips glow after the sweep passes
            since = (sweep_a - ang) % 360
            g = max(0.0, 1 - since / 270) * sa
            if g <= 0.02: continue
            x = C + fr * R * SS * math.sin(math.radians(ang)); y = C - fr * R * SS * math.cos(math.radians(ang))
            rb = (5 + 4 * g) * SS
            d.ellipse((x - rb, y - rb, x + rb, y + rb), fill=lerp(BG, CYAN, g) + (int(255 * g),))
    # home dot
    hd = ease((t - 4) / 10) if intro else 1
    if hd > 0:
        pulse = 1 + 0.15 * math.sin(t / FPS * 2 * math.pi)
        r = 9 * SS * hd * pulse; d.ellipse((C - r, C - r, C + r, C + r), fill=AMBER + (255,))
        r2 = 22 * SS * hd; d.ellipse((C - r2, C - r2, C + r2, C + r2), outline=AMBER + (int(110 * hd),), width=2 * SS)
    # plane + amber trail (intro only)
    if intro:
        s_end = ease((t - 10) / 36) * 1.0
        if t >= 10 and s_end > 0:
            fade = 1 - ease((t - 48) / 11)
            pts = [bez(i / 120 * s_end) for i in range(int(120 * s_end) + 1)]
            for i in range(1, len(pts)):
                al = int(230 * fade * (i / len(pts)) ** 1.5)
                d.line((C + pts[i-1][0] * SS, C + pts[i-1][1] * SS, C + pts[i][0] * SS, C + pts[i][1] * SS), fill=AMBER + (al,), width=4 * SS)
            x, y, th = bez(s_end)
            k = R * 0.019 * SS; ang = th + math.pi / 2
            poly = [(C + x * SS + k * (px * math.cos(ang) - py * math.sin(ang)), C + y * SS + k * (px * math.sin(ang) + py * math.cos(ang))) for px, py in PLANE]
            d.polygon(poly, fill=WHITE + (int(255 * fade),))
    # name on the top of the ring, tagline upright along the bottom
    for li, (L, radius, f, col0, start) in enumerate([(TOPL, TR, font, WHITE, 22), (BOTL, BR, font2, DIM, 38)]):
        for i, (ch, th) in enumerate(L):
            if ch == ' ': continue
            age = 1.0 if not intro else ease((t - start - i * 1.2) / 8)
            if age <= 0: continue
            col = AMBER if ch == '\u00b7' else col0
            rot = -math.degrees(th + math.pi / 2) if li == 0 else -math.degrees(th - math.pi / 2)
            g = glyph(ch, col, f).rotate(rot, resample=Image.BICUBIC)
            if age < 1: g.putalpha(g.getchannel('A').point(lambda v, q=age: int(v * q)))
            x = C + radius * math.cos(th); y = C + radius * math.sin(th)
            lay.alpha_composite(g, (int(x - g.width / 2), int(y - g.height / 2)))
    im = Image.alpha_composite(im.convert('RGBA'), lay).convert('RGB')
    em = im.resize((BOX, BOX), Image.LANCZOS)
    out = Image.new('RGB', (W, H), BG); out.paste(em, ((W - BOX) // 2, (H - BOX) // 2))
    return out

work = 'bootanim_build'; shutil.rmtree(work, ignore_errors=True)
os.makedirs(work + '/part0'); os.makedirs(work + '/part1')
frames = [(t, 'part0/%04d.jpg' % t) for t in range(N0)] + [(N0 + t, 'part1/%04d.jpg' % t) for t in range(N1)]
if a.preview: frames = [f for f in frames if f[0] in (0, 12, 24, 36, 48, 59, 70, 85)]
for t, name in frames:
    frame(t).save(os.path.join(work, name), quality=88)
with open(work + '/desc.txt', 'w') as f: f.write('%d %d %d\np 1 0 part0\np 0 0 part1\n' % (W, H, FPS))
with zipfile.ZipFile(a.out, 'w', zipfile.ZIP_STORED) as z:   # boot animations must be stored, not compressed
    z.write(work + '/desc.txt', 'desc.txt')
    for _, name in frames: z.write(os.path.join(work, name), name)
print('wrote', a.out, os.path.getsize(a.out) // 1024, 'KB,', len(frames), 'frames')
