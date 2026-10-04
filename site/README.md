# CleanRoute landing page

Static product landing page for CleanRoute.

## Source

The page is kept in semantic HTML fragments under `site/src/` so the Stitch composition stays reviewable without introducing a frontend framework only for marketing.

Build locally:

```bash
python3 site/build.py
```

The generated site is written to `site/dist/` and is not committed.

## Content rule

The landing must distinguish:

- **shipped MVP capabilities** — clients, zone-based collection, offline local writes, payments/arrears, individual call/WhatsApp actions, canonical Excel import and XLSX export;
- **prototype/spike capabilities** — mapping and route optimization;
- **future contract** — remote multi-device sync.

Do not publish savings claims, encryption claims, automated messaging claims or routing/GPS claims unless they are backed by the product and representative evidence.

## Hosting

The production target is Cloudflare Pages:

- production branch: `main`;
- build command: `python3 site/build.py`;
- build output directory: `site/dist`;
- custom domain: `https://cleanroute.trigenys.com/`.

GitHub Pages remains a temporary rollback path during the migration.

See `docs/deployment/cloudflare-pages.md` for the cutover procedure and `docs/deployment/github-pages.md` for the legacy fallback.
