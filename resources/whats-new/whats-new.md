KONTAKT Script Processor (KSP) VS Code Extension
================================================

## What's New in v1.1.0

### New Features

#### 1. Language Server: Added Prefer Snippet Insertion mode

Added configuration item `ksp.completion.preferSnippetInsertion` to control whether code completion should insert snippets for commands with argument overloads.

Default is `true`. If `false`, plain text will be inserted instead of snippets.


This allows snippets to be prioritized during code completion. Snippets streamline coding by providing placeholders for command arguments.

#### 2. Added Snippets

Added following snippets for a typical statement.

1. declare variable statement
    - declare
    - const
    - polyphonic
    - int / intarray
    - real / realarray
    - string / stringarray
2. if / else statement
3. while statement
4. select statement
5. KSP preprocessor statement

### Language Server Bug fixes, improvements

- Compilation error if a UI variable array has fewer elements than the initialization parameters
- Syntax and Semantic analysis


See the [CHANGELOG](https://marketplace.visualstudio.com/items/rkoubou.ksp/changelog) for earlier versions.
