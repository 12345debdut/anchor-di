# Anchor DI Documentation

This directory contains the Docusaurus documentation site for Anchor DI.

## Local Development

```bash
cd website
npm install
npm start
```

Open [http://localhost:3000](http://localhost:3000). With `baseUrl: '/anchor-di/'`, you may need to run:

```bash
npm start -- --base-url /anchor-di/
```

Or serve the built site:

```bash
npm run build
npm run serve -- --locale en
```

## Deployment

Docs are deployed to GitHub Pages via GitHub Actions when changes are pushed to `main` in the `website/` directory.

### Enable GitHub Pages (required before first deploy)

If you see **"Get Pages site failed"**, enable GitHub Pages manually:

1. Open your repo on GitHub
2. Go to **Settings** → **Pages**
3. Under **Build and deployment**, set **Source** to **GitHub Actions**
4. Save (no need to select a branch or folder)
5. Push a change to `website/` or run the workflow manually

The site will be live at: `https://12345debdut.github.io/anchor-di/`
