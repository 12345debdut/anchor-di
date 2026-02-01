/**
 * Creating a sidebar enables you to:
 * - create an ordered group of docs
 * - render a sidebar for each doc of that group
 * - provide next/previous navigation
 *
 * The sidebars can be generated from the filesystem, or explicitly defined here.
 *
 * @type {import('@docusaurus/plugin-content-docs').SidebarsConfig}
 */
const sidebars = {
  docsSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Getting Started',
      link: {
        type: 'generated-index',
        description: 'Learn the basics of Anchor DI and dependency injection.',
      },
      items: [
        'getting-started/overview',
        'getting-started/dependency-injection',
        'getting-started/quick-example',
      ],
    },
    {
      type: 'category',
      label: 'Installation',
      link: {
        type: 'generated-index',
        description: 'Add Anchor DI to your Kotlin Multiplatform project.',
      },
      items: [
        'installation/setup',
        'installation/kmp-compose',
        'installation/kmp-without-compose',
        'installation/multi-module',
        'installation/platform-specific',
      ],
    },
    {
      type: 'category',
      label: 'Core Concepts',
      link: {
        type: 'generated-index',
        description: 'Components, scopes, and binding fundamentals.',
      },
      items: [
        'core/concepts',
        'core/modules-bindings',
        'core/qualifiers',
      ],
    },
    {
      type: 'category',
      label: 'Scopes',
      link: {
        type: 'generated-index',
        description: 'Built-in and custom scopes for managing object lifetimes.',
      },
      items: [
        'scopes/built-in',
        'scopes/custom-scopes',
      ],
    },
    {
      type: 'category',
      label: 'Advanced',
      link: {
        type: 'generated-index',
        description: 'Lazy injection, multibinding, and more.',
      },
      items: [
        'advanced/lazy-provider',
        'advanced/multibinding',
      ],
    },
    {
      type: 'category',
      label: 'Kotlin Multiplatform',
      link: {
        type: 'generated-index',
        description: 'Using Anchor DI across Android, iOS, Desktop, and Web.',
      },
      items: [
        'kmp/overview',
        'kmp/expect-actual',
      ],
    },
    {
      type: 'category',
      label: 'Compose Multiplatform',
      link: {
        type: 'generated-index',
        description: 'Compose integration: anchorInject, viewModelAnchor, navigation.',
      },
      items: [
        'compose/overview',
        'compose/navigation-scoped',
      ],
    },
    {
      type: 'category',
      label: 'Guides',
      link: {
        type: 'generated-index',
        description: 'Real-world examples and best practices.',
      },
      items: [
        'guides/real-world-example',
        'guides/testing',
      ],
    },
    'troubleshooting',
  ],
};

module.exports = sidebars;
