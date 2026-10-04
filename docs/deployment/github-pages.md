# GitHub Pages deployment — CleanRoute (legacy fallback)

GitHub Pages is the temporary rollback path while the CleanRoute landing is migrated to Cloudflare Pages.

## Legacy URL

`https://trigenys.github.io/cleanroute/`

## Legacy custom-domain state

The historical custom-domain target is `https://cleanroute.trigenys.com/`, with DNS:

- Type: `CNAME`
- Name/Host: `cleanroute`
- Target/Value: `trigenys.github.io`

During the Cloudflare cutover, do not remove the `gh-pages` branch until the Cloudflare production deployment and HTTPS custom domain have both been verified.

After cutover, the custom-domain CNAME moves to the Cloudflare Pages hostname returned by the dashboard. The GitHub Pages branch can then remain as a rollback artifact without owning `cleanroute.trigenys.com`.

## Legacy deployment workflow

`.github/workflows/pages.yml` builds `site/` and publishes the generated output to `gh-pages`. GitHub's Pages deployment then serves that branch.

The generated `site/dist/` directory is ephemeral and must not be committed.

For the active migration and production target, see [Cloudflare Pages deployment](cloudflare-pages.md).
