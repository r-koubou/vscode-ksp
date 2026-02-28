KONTAKT Script Processor (KSP) VS Code Extension
================================================

## What's New in v1.0.3

### Security

- Addressed [GHSA-3ppc-4f35-3m26](https://github.com/advisories/GHSA-3ppc-4f35-3m26) (CVE-related) via dependency updates.
  - dependencies: bumped `vscode-languageclient` from `^9.0.1` to `^10.0.0-next.20`
  - devDependencies: updated build/CI tooling dependencies

### Notes

- `vscode-languageclient` is pinned to a pre-release (`next`) version.

### Compiler bug fixes, improvements

- Fix missing argument validation in callback declaration + Run unit test on CI by [@r-koubou](https://github.com/r-koubou) in [#332](https://github.com/r-koubou/KSPCompiler/pull/332)
- Improve Semantic Analysis: Data Types and UI Information for Variable Declarations and Callback Parameters by [@r-koubou](https://github.com/r-koubou) in [#334](https://github.com/r-koubou/KSPCompiler/pull/334)
- Update UI Initialization Arguments to Use an Explicit DataType Instead of Name-Based Typing by [@r-koubou](https://github.com/r-koubou) in [#335](https://github.com/r-koubou/KSPCompiler/pull/335)
- Correct expected and actual Order in Error Messages by [@r-koubou](https://github.com/r-koubou) in [#336](https://github.com/r-koubou/KSPCompiler/pull/336)
- Correct PGS Command Arguments Mistaken for Preprocessor Symbols and Remove Trailing Whitespace by [@r-koubou](https://github.com/r-koubou) in [#337](https://github.com/r-koubou/KSPCompiler/pull/337)
- PGS-Related Fixes by [@r-koubou](https://github.com/r-koubou) in [#338](https://github.com/r-koubou/KSPCompiler/pull/338)
- Fix missing SET\_CONDITION and RESET\_CONDITION output during obfuscation by [@r-koubou](https://github.com/r-koubou) in [#339](https://github.com/r-koubou/KSPCompiler/pull/339)

See also: https://github.com/r-koubou/KSPCompiler/releases/tag/v1.0.4


See the [CHANGELOG](https://marketplace.visualstudio.com/items/rkoubou.ksp/changelog) for earlier versions.
