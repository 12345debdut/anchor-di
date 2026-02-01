// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when a type is not explicitly defined).

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Anchor DI',
  tagline: 'Compile-time Dependency Injection for Kotlin Multiplatform',
  favicon: 'img/logo.svg',

  // GitHub Pages deployment URL
  url: 'https://12345debdut.github.io',
  baseUrl: '/anchor-di/',

  organizationName: '12345debdut',
  projectName: 'anchor-di',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/12345debdut/anchor-di/tree/main/website/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Anchor DI',
        logo: {
          alt: 'Anchor DI',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'docsSidebar',
            position: 'left',
            label: 'Docs',
          },
          {
            href: 'https://github.com/12345debdut/anchor-di',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Getting Started',
                to: '/intro',
              },
              {
                label: 'Installation',
                to: '/installation/setup',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/12345debdut/anchor-di',
              },
              {
                label: 'Issues',
                href: 'https://github.com/12345debdut/anchor-di/issues',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Maven Central',
                href: 'https://central.sonatype.com/artifact/io.github.12345debdut/anchor-di-api',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Anchor DI. Apache 2.0 License.`,
      },
      prism: {
        // Use Docusaurus defaults (no custom theme require to avoid module resolution issues)
        additionalLanguages: ['kotlin'],
      },
    }),
};

module.exports = config;
