# CleanRoute landing page

Static product landing page for CleanRoute, deployed with GitHub Pages.

## Source

The page is kept in semantic HTML fragments under `site/src/` so the Stitch composition stays reviewable without introducing a frontend framework only for marketing.

Build locally:

```bash
python site/build.py
```

The generated site is written to `site/dist/` and is not committed.

## Content rule

The landing must distinguish:

- **shipped MVP capabilities** — clients, zone-based collection, offline local writes, payments/arrears, individual call/WhatsApp actions, canonical Excel import and XLSX export;
- **prototype/spike capabilities** — mapping and route optimization;
- **future contract** — remote multi-device sync.

Do not publish savings claims, encryption claims, automated messaging claims or routing/GPS claims unless they are backed by the product and representative evidence.

## Public URLs

- GitHub Pages fallback: `https://trigenys.github.io/cleanroute/`
- Custom domain target: `https://cleanroute.trigenys.com/`

## Custom domain

For a GitHub Actions Pages deployment, configure the domain in **Repository Settings → Pages → Custom domain**.

DNS for `trigenys.com`:

```text
Type:  CNAME
Name:  cleanroute
Value: trigenys.github.io
```

Set the GitHub custom domain before publishing the DNS CNAME. If the DNS provider supports proxying, start DNS-only until GitHub validates the domain and provisions HTTPS.
