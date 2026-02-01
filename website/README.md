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

### Enable GitHub Pages

1. Go to **Settings** → **Pages** in your GitHub repo
2. Under **Build and deployment**, set **Source** to **GitHub Actions**
3. Push to `main`; the workflow will build and deploy

The site will be live at: `https://12345debdut.github.io/anchor-di/`
