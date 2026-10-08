from __future__ import annotations

from hashlib import sha256
from pathlib import Path
import subprocess
import sys
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent
DIST = ROOT / "dist"
RELEASE_BASE = "https://github.com/Trigenys/cleanroute/releases/download/pilot-latest"
APK_NAME = "CleanRoute-pilot.apk"
CHECKSUM_NAME = f"{APK_NAME}.sha256"
USER_AGENT = "CleanRoute-Cloudflare-Build/1.0"


def fetch(url: str) -> bytes:
    request = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(request, timeout=180) as response:
        return response.read()


subprocess.run([sys.executable, str(ROOT / "build.py")], check=True)

apk = fetch(f"{RELEASE_BASE}/{APK_NAME}")
checksum_text = fetch(f"{RELEASE_BASE}/{CHECKSUM_NAME}").decode("utf-8").strip()

try:
    expected = checksum_text.split()[0].lower()
except IndexError as exc:
    raise RuntimeError("pilot-latest checksum file is empty") from exc

actual = sha256(apk).hexdigest()
if actual != expected:
    raise RuntimeError(
        f"pilot APK checksum mismatch: expected {expected}, got {actual}"
    )

(DIST / APK_NAME).write_bytes(apk)
(DIST / CHECKSUM_NAME).write_text(
    f"{actual}  {APK_NAME}\n",
    encoding="utf-8",
)

print(f"Cloudflare bundle ready: {DIST}")
print(f"{APK_NAME} sha256={actual}")
