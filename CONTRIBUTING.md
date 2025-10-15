# Contributing to Mobile Network Reset

Thank you for your interest in contributing to this project! This document provides guidelines for contributing.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/android-mobile-network-reset.git`
3. Create a feature branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes thoroughly
6. Commit your changes: `git commit -m "Description of changes"`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Open a Pull Request

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 17 or higher
- Android SDK with API 26-34
- Git

### Setting Up the Project
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned repository
4. Wait for Gradle sync to complete
5. Connect an Android device or start an emulator
6. Click Run

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Use Android Architecture Components where appropriate

## Testing

- Write unit tests for business logic
- Test on multiple Android versions if possible
- Test on devices with different form factors
- Verify functionality with different network operators

## Pull Request Guidelines

### Before Submitting
- [ ] Code builds without errors
- [ ] Code follows project style guidelines
- [ ] Tests pass
- [ ] Lint checks pass
- [ ] Documentation is updated if needed
- [ ] Commit messages are clear and descriptive

### PR Description Should Include
- What changes were made and why
- How to test the changes
- Any breaking changes
- Screenshots for UI changes
- Related issue numbers (if applicable)

## Types of Contributions

### Bug Fixes
- Describe the bug clearly
- Provide steps to reproduce
- Explain how your fix addresses the issue

### New Features
- Open an issue first to discuss the feature
- Ensure the feature aligns with project goals
- Include tests and documentation

### Documentation
- Fix typos and clarify instructions
- Add examples and use cases
- Improve README or other docs

### Performance Improvements
- Benchmark before and after
- Explain the optimization approach
- Ensure no functionality is broken

## Reporting Issues

When reporting issues, please include:
- Device model and Android version
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Screenshots or logs if applicable
- Any error messages

## Code Review Process

1. Maintainers will review your PR
2. Feedback will be provided if changes are needed
3. Once approved, your PR will be merged
4. Your contribution will be credited in the release notes

## Questions?

Feel free to open an issue for any questions about contributing!

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (MIT License).
