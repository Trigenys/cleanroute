from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parent
SRC = ROOT / "src"
DIST = ROOT / "dist"

if DIST.exists():
    shutil.rmtree(DIST)
DIST.mkdir(parents=True)

parts = sorted(SRC.glob("*.html"))
(DIST / "index.html").write_text(
    "".join(p.read_text(encoding="utf-8") for p in parts),
    encoding="utf-8",
)

for name in ["favicon.svg", "robots.txt", "sitemap.xml", "404.html"]:
    shutil.copy2(ROOT / name, DIST / name)

print(DIST / "index.html")
