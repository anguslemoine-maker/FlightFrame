#!/usr/bin/env python3
"""
Make the dark radar map for Flight Frame, for any location.

    pip install mapbox-vector-tile pillow
    python3 make_map.py --lat 52.1583 --lon 4.5333 --out ./out

Writes radarmap.jpg and radarmap.js. Upload both from the phone app (Location > Radar map),
or copy them into the display folder.

Map data (c) OpenStreetMap contributors, served by OpenFreeMap (https://openfreemap.org).
Downloads ~60-120 small vector tiles once; please don't run it in a loop.
"""
import argparse, json, math, os, sys, time, urllib.request

try:
    import mapbox_vector_tile as mvt
    from PIL import Image, ImageDraw
except ImportError:
    sys.exit("Please run:  pip install mapbox-vector-tile pillow")

Z = 12
UA = {"User-Agent": "FlightFrame-map-generator/1.0 (+https://github.com/anguslemoine-maker/FlightFrame)"}

# colours (dark map; the display recolours it to match other themes)
BG = '#0f141b'
LANDCOVER = {'farmland': '#10171a', 'grass': '#111a1a', 'wood': '#0f1c1a', 'wetland': '#0f1a1f', 'sand': '#1a1c1f'}
LANDUSE = {'residential': '#1a212b', 'industrial': '#1c222c', 'commercial': '#1c222c', 'retail': '#1c222c'}
WATER = '#0e2438'
ROADS = [('tertiary', '#252d38', 1.2), ('secondary', '#2c3542', 1.6), ('primary', '#36404f', 2.0), ('trunk', '#3f4a5b', 2.4), ('motorway', '#4c586b', 3.0)]


def tile_xy(lat, lon, z):
    n = 2 ** z
    return (lon + 180) / 360 * n, (1 - math.asinh(math.tan(math.radians(lat))) / math.pi) / 2 * n


def get_json(url):
    return json.loads(urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30).read())


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--lat', type=float, required=True)
    ap.add_argument('--lon', type=float, required=True)
    ap.add_argument('--km', type=float, default=22, help='half-width of the map in km (default 22, covers a 20 km radar)')
    ap.add_argument('--size', type=int, default=1100, help='output image size in pixels')
    ap.add_argument('--labels', type=int, default=11, help='max number of place names')
    ap.add_argument('--out', default='.')
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)

    tj = get_json('https://tiles.openfreemap.org/planet')
    url = tj['tiles'][0]
    dlat = a.km / 110.57
    dlon = a.km / (111.32 * math.cos(math.radians(a.lat)))
    x0, y0 = tile_xy(a.lat + dlat, a.lon - dlon, Z)
    x1, y1 = tile_xy(a.lat - dlat, a.lon + dlon, Z)
    X0, X1, Y0, Y1 = int(x0), int(x1), int(y0), int(y1)
    SS = 2
    W, H = (X1 - X0 + 1) * 256 * SS, (Y1 - Y0 + 1) * 256 * SS
    img = Image.new('RGB', (W, H), BG)
    d = ImageDraw.Draw(img)
    layers = {}
    n = (X1 - X0 + 1) * (Y1 - Y0 + 1)
    print(f'Downloading {n} map tiles…')
    for x in range(X0, X1 + 1):
        for y in range(Y0, Y1 + 1):
            u = url.replace('{z}', str(Z)).replace('{x}', str(x)).replace('{y}', str(y))
            for attempt in range(3):
                try:
                    raw = urllib.request.urlopen(urllib.request.Request(u, headers=UA), timeout=30).read()
                    break
                except Exception as e:
                    if attempt == 2: raise
                    time.sleep(2)
            t = mvt.decode(raw, default_options={'y_coord_down': True})
            for L, v in t.items():
                ext = v.get('extent', 4096)
                for ft in v['features']:
                    layers.setdefault(L, []).append((x - X0, y - Y0, ext, ft))
            time.sleep(0.05)

    def P(x, y, ext, pts):
        return [((x + px / ext) * 256 * SS, (y + py / ext) * 256 * SS) for px, py in pts]

    def polys(g):
        return [g['coordinates']] if g['type'] == 'Polygon' else (g['coordinates'] if g['type'] == 'MultiPolygon' else [])

    def lines(g):
        return [g['coordinates']] if g['type'] == 'LineString' else (g['coordinates'] if g['type'] == 'MultiLineString' else [])

    def fill(L, col_fn):
        for x, y, ext, ft in layers.get(L, []):
            col = col_fn(ft['properties'])
            if not col: continue
            for poly in polys(ft['geometry']):
                d.polygon(P(x, y, ext, poly[0]), fill=col)
                for hole in poly[1:]: d.polygon(P(x, y, ext, hole), fill=BG)

    fill('landcover', lambda p: LANDCOVER.get(p.get('class')))
    fill('landuse', lambda p: LANDUSE.get(p.get('class')))
    fill('water', lambda p: WATER if p.get('class') != 'swimming_pool' else None)
    for x, y, ext, ft in layers.get('waterway', []):
        for ln in lines(ft['geometry']): d.line(P(x, y, ext, ln), fill=WATER, width=3 * SS)
    fill('aeroway', lambda p: {'aerodrome': '#151c26', 'apron': '#1f2733'}.get(p.get('class')))
    for x, y, ext, ft in layers.get('aeroway', []):
        c = ft['properties'].get('class')
        for ln in lines(ft['geometry']):
            d.line(P(x, y, ext, ln), fill='#5b6880' if c == 'runway' else '#2c3542', width=(7 if c == 'runway' else 2) * SS)
        if c == 'runway':
            for poly in polys(ft['geometry']): d.polygon(P(x, y, ext, poly[0]), fill='#5b6880')
    for x, y, ext, ft in layers.get('transportation', []):
        if ft['properties'].get('class') == 'rail':
            for ln in lines(ft['geometry']): d.line(P(x, y, ext, ln), fill='#262e3a', width=max(1, int(1.4 * SS)))
    for cls, col, w in ROADS:
        for x, y, ext, ft in layers.get('transportation', []):
            if ft['properties'].get('class') != cls or ft['properties'].get('brunnel') == 'tunnel': continue
            for ln in lines(ft['geometry']): d.line(P(x, y, ext, ln), fill=col, width=max(1, int(w * SS)), joint='curve')

    cx, cy = tile_xy(a.lat, a.lon, Z)
    cx, cy = (cx - X0) * 256 * SS, (cy - Y0) * 256 * SS
    mpp = 156543.03 * math.cos(math.radians(a.lat)) / 2 ** Z / SS
    half = a.km * 1000 / mpp
    img.crop((round(cx - half), round(cy - half), round(cx + half), round(cy + half))).resize((a.size, a.size), Image.LANCZOS) \
        .save(os.path.join(a.out, 'radarmap.jpg'), quality=85)

    # place names: biggest places first, skip ones that would overlap
    rank = {'city': 0, 'town': 1, 'village': 3}
    cands, seen = [], set()
    for x, y, ext, ft in layers.get('place', []):
        p = ft['properties']
        cls = p.get('class')
        name = p.get('name:latin') or p.get('name')
        if cls not in rank or not name or name in seen: continue
        g = ft['geometry']
        if g['type'] != 'Point': continue
        px, py = P(x, y, ext, [g['coordinates']])[0]
        kx, ky = (px - cx) * mpp / 1000, -(py - cy) * mpp / 1000
        if kx * kx + ky * ky > (a.km * 0.9) ** 2: continue
        seen.add(name)
        cands.append((rank[cls], p.get('rank', 99) or 99, name, kx, ky, cls))
    cands.sort()
    labels = []
    for r, _, name, kx, ky, cls in cands:
        if len(labels) >= a.labels: break
        if any(abs(kx - l[1]) < 0.12 * a.km * max(len(name), len(l[0])) / 8 and abs(ky - l[2]) < 0.07 * a.km for l in labels): continue
        labels.append([name, round(kx, 2), round(ky, 2), 1 if cls == 'city' or (r <= 1 and len(labels) < 3) else 0])
    for x, y, ext, ft in layers.get('water_name', []):
        p = ft['properties']
        if p.get('class') not in ('ocean', 'sea', 'bay') or not p.get('name'): continue
        g = ft['geometry']
        if g['type'] != 'Point': continue
        px, py = P(x, y, ext, [g['coordinates']])[0]
        kx, ky = (px - cx) * mpp / 1000, -(py - cy) * mpp / 1000
        if kx * kx + ky * ky < (a.km * 0.85) ** 2:
            labels.append([p.get('name:latin') or p['name'], round(kx, 2), round(ky, 2), 2]); break

    meta = {'lat': a.lat, 'lon': a.lon, 'halfKm': a.km, 'credit': '© OpenStreetMap contributors · OpenFreeMap', 'labels': labels}
    with open(os.path.join(a.out, 'radarmap.js'), 'w') as f:
        f.write('// Map for the radar: generated by tools/make_map.py\nwindow.FF_MAP=' + json.dumps(meta, ensure_ascii=False) + ';\n')
    print(f'Wrote radarmap.jpg and radarmap.js to {a.out} ({len(labels)} place names). Edit the names in radarmap.js if you like.')


if __name__ == '__main__':
    main()
