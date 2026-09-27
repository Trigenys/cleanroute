# GitHub Pages deployment — CleanRoute

The CleanRoute landing page is built from `site/` by `.github/workflows/pages.yml`.

## Default URL

`https://trigenys.github.io/cleanroute/`

## Custom domain target

`https://cleanroute.trigenys.com/`

### GitHub

Repository → **Settings** → **Pages**.

1. Ensure Pages uses **GitHub Actions** as the publishing source.
2. Under **Custom domain**, enter `cleanroute.trigenys.com`.
3. Save the domain before publishing the DNS record.

The connected GitHub integration used for normal repository writes does not expose repository Pages-administration settings, so this one-time setting is intentionally documented rather than hidden in CI.

### DNS

At the DNS provider for `trigenys.com`, create:

- Type: `CNAME`
- Name/Host: `cleanroute`
- Target/Value: `trigenys.github.io`
- TTL: Auto/default

If proxying is available, start DNS-only until GitHub validates the domain and provisions TLS.

After GitHub reports the DNS check as successful, enable **Enforce HTTPS**.

## Deployment workflow

Pull requests validate the static build. Pushes to `main` that change `site/**` or the Pages workflow build and deploy the site through GitHub Pages.

The generated `site/dist/` directory is ephemeral and must not be committed.
