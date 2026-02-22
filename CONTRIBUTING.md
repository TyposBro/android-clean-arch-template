# Contributing

Thanks for your interest in contributing! This guide will help you get started.

## Local Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/android-clean-arch-template.git
   ```
2. Open the project in Android Studio (Hedgehog or later).
3. Run `./gradlew assembleDebug` to verify the build.
4. Run `./gradlew test` to verify tests pass.

## Architecture

This project follows **Clean Architecture** with three layers per feature:

- **data** -- repositories, data sources, DTOs
- **domain** -- use cases, models, repository interfaces
- **presentation** -- ViewModels, Compose UI

See the [README](README.md) for a detailed breakdown.

## Adding a New Feature

Use the existing **Notes** feature as a reference:

1. Create a package under `app/src/main/java/.../feature/<name>/`.
2. Add `data/`, `domain/`, and `presentation/` sub-packages.
3. Define your repository interface in `domain`, implement it in `data`.
4. Write use cases in `domain` and inject them into your ViewModel.
5. Build Compose screens in `presentation`.
6. Register any new Hilt modules.

## Code Style

- Follow standard [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Compose: keep composables small, hoist state, use `remember` wisely.
- Name branches with a prefix: `feat/`, `fix/`, `refactor/`.
- Keep functions focused -- one responsibility per function.

## PR Process

1. Fork the repository and create a branch from `main`.
2. Make your changes following the architecture patterns above.
3. Ensure `./gradlew assembleDebug` and `./gradlew test` both pass.
4. Open a Pull Request against `main` and fill out the PR template.
5. Address any review feedback promptly.

## Questions?

Open an issue or start a discussion -- happy to help.
