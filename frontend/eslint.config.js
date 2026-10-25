// @ts-check
const eslint = require('@eslint/js');
const { defineConfig, globalIgnores } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');
const sonarjs = require('eslint-plugin-sonarjs');

module.exports = defineConfig([
  globalIgnores(['dist/', 'coverage/', '.angular/']),
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.strictTypeChecked,
      tseslint.configs.stylisticTypeChecked,
      angular.configs.tsRecommended,
      sonarjs.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: __dirname,
      },
    },
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],
      '@angular-eslint/consistent-component-styles': 'error',
      '@angular-eslint/no-implicit-take-until-destroyed': 'error',
      '@angular-eslint/no-uncalled-signals': 'error',
      '@angular-eslint/prefer-output-emitter-ref': 'error',
      '@angular-eslint/prefer-output-readonly': 'error',
      '@angular-eslint/prefer-signals': 'error',
      '@angular-eslint/relative-url-prefix': 'error',
      '@typescript-eslint/no-extraneous-class': ['error', { allowWithDecorator: true }],
      eqeqeq: 'error',
      'no-console': ['error', { allow: ['error'] }],
      'no-duplicate-imports': 'error',
      'sonarjs/no-commented-code': 'error',
      'sonarjs/no-duplicate-string': 'error',
    },
  },
  {
    files: ['**/*.spec.ts'],
    rules: {
      'sonarjs/no-duplicate-string': 'off',
    },
  },
  {
    files: ['**/*.html'],
    extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
    rules: {
      '@angular-eslint/template/button-has-type': 'error',
      '@angular-eslint/template/conditional-complexity': 'error',
      '@angular-eslint/template/cyclomatic-complexity': 'error',
      '@angular-eslint/template/no-any': 'error',
      '@angular-eslint/template/no-inline-styles': 'error',
      '@angular-eslint/template/prefer-self-closing-tags': 'error',
    },
  },
]);
