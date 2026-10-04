# Cloudflare Pages deployment — CleanRoute

CleanRoute's public landing is a static site built from `site/`. Cloudflare Pages is the production target because the `cleanroute.trigenys.com` hostname is a subdomain and the authoritative DNS can remain with the existing external provider.

## Production contract

- Repository: `Trigenys/cleanroute`
- Production branch: `main`
- Root directory: repository root
- Build command: `python3 site/build_cloudflare.py`
- Build output directory: `site/dist`
- Environment variables: none required
- Production domain: `https://cleanroute.trigenys.com/`

Cloudflare Pages creates a `*.pages.dev` hostname for the project. Use the exact hostname assigned by Cloudflare as the DNS target.

## Why the Cloudflare build is separate

`site/build.py` creates the static landing only.

`site/build_cloudflare.py` then downloads the public `pilot-latest` APK and checksum from GitHub Releases, verifies SHA-256, and places both files in `site/dist/`. This preserves the existing public download contract without storing a Cloudflare API token or a large binary in Git.

The current pilot APK is below Cloudflare Pages' 25 MiB per-file limit. If a future APK crosses that limit, move release binaries to GitHub Releases or object storage instead of forcing them into Pages.

## Cloudflare project setup

In **Cloudflare → Workers & Pages → Create → Pages → Connect to Git**:

1. Connect the GitHub repository `Trigenys/cleanroute`.
2. Set the production branch to `main`.
3. Keep the repository root as the root directory.
4. Set the build command to `python3 site/build_cloudflare.py`.
5. Set the build output directory to `site/dist`.
6. Deploy and verify the generated `*.pages.dev` URL before touching production DNS.

No Cloudflare API token needs to be stored in GitHub when Cloudflare's Git integration owns the build.

## Custom domain and external DNS

After the first successful Pages deployment:

1. Open the Pages project → **Custom domains**.
2. Add `cleanroute.trigenys.com` and continue until Cloudflare gives the DNS target / verification state.
3. At the authoritative DNS provider, replace the existing record:

```text
cleanroute  CNAME  trigenys.github.io
```

with:

```text
cleanroute  CNAME  <cleanroute-project>.pages.dev
```

Use the exact `*.pages.dev` hostname assigned to the project.

Do not point the DNS record at Pages before adding the custom domain inside Cloudflare. Cloudflare documents that manually pointing a hostname at a Pages project before associating it can fail with a 522.

## Cutover verification

The migration is complete only when all of these are true:

- the production `*.pages.dev` deployment returns the expected landing;
- `https://cleanroute.trigenys.com/` returns the same landing;
- the custom domain is Active in Cloudflare;
- HTTPS is valid;
- `/CleanRoute-pilot.apk` returns the current pilot package;
- `/CleanRoute-pilot.apk.sha256` matches the deployed APK;
- the QR code still points to the production custom domain;
- `robots.txt`, `sitemap.xml`, favicon and Open Graph image resolve.

## Rollback

Until the Cloudflare proof of done passes, keep the `gh-pages` branch and the legacy GitHub Pages deployment intact.

If rollback is required before the migration is finalized, restore the `cleanroute` CNAME to `trigenys.github.io` and revalidate GitHub Pages.
