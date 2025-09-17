# GitHub Agent Instructions

Authoritative guidelines for any automated or human contributor ("agent") working on the Thread Dump Diagnostic Agent repository.

---
## 1. Technology & Version Policy
- Java: 21 ONLY. Never downgrade. Use `<java.version>` / compiler properties already defined in `pom.xml`.
- Spring Boot: Use the exact version defined by the `spring-boot.version` property (`3.5.5` at time of writing). Never hardcode a different version in plugins or dependencies.
- Dependency versions: Inherit from Spring Boot BOM where possible. New direct versions must be justified in PR description.
- When upgrading Spring Boot or Java: update the property, run full build & tests, add CHANGELOG entry.

## 2. Architectural Principles
- Layered design already established: Controller → Service → Analyzer/Parser/Formatter → Model.
- Keep controllers thin (validation + delegation). No parsing, IO-heavy logic, or branching complexity inside controllers.
- Services encapsulate business logic; keep them cohesive and testable.
- Parsing & analysis logic must be side-effect free (pure) where feasible to ease testing.
- Avoid static global state; prefer Spring-managed beans.
- Favor constructor injection; no field injection.
- Prevent cyclic dependencies—refactor with ports (interfaces) if coupling increases.

## 3. Spring Boot Best Practices
- Profiles: Introduce new environment-specific behavior via Spring profiles (`dev`, `test`, `prod`) — never hardcode env conditionals.
- Configuration: Externalize tunables (thread thresholds, limits) in `application.yml` under a coherent namespace (`thread-dump.*`). Provide type-safe binding with `@ConfigurationProperties` for new config groups.
- Validation: Use `jakarta.validation` on request DTOs & configuration properties. Always include a global exception handler if expanding error surfaces.
- Exception Handling: Map predictable domain errors to 4xx responses; unexpected ones to 500. Avoid leaking raw exception messages in production.
- Logging: Use parameterized logging (`log.info("... {}", value)`). No `System.out`. Avoid logging entire raw thread dumps at INFO or WARN; if needed for debug, truncate & guard with `log.isDebugEnabled()`.
- Observability: Prefer Micrometer metrics for counters (e.g., analyzed dumps count, detected deadlocks). Guard expensive metric construction with conditional checks.
- Actuator: Keep default endpoints minimal; restrict sensitive ones via security layer if added later.
- Graceful Shutdown: Rely on Spring Boot's default; do not spawn unmanaged threads.
- Thread Safety: Analyzer components must avoid shared mutable state. If caching is added, use thread-safe structures & document invalidation rules.

## 4. Performance & Memory Guidelines
- Thread dumps may be very large (multi‑MB). Avoid:
  - O(n^2) scans across all threads when grouping—pre-index where possible.
  - Storing duplicate full stack traces; normalize & reference.
- Prefer streaming parsing if future refactor introduces extreme input size handling.
- Add benchmark tests before optimizing prematurely; justify structural performance changes in PR.

## 5. Security & Privacy
- Treat thread dump content as potentially sensitive (class names, infrastructure identifiers). Never log full content at INFO/WARN.
- Do not persist thread dumps unless feature explicitly added with opt‑in.
- Sanitize outbound error messages—no internal stack frames beyond what Spring provides by default.
- If authentication/authorization is introduced later, integrate via Spring Security; keep controllers unchanged where possible.

## 6. Data & Models
- Extend models with backward compatibility in mind: add fields (never remove) until a major version.
- Use immutable record-like patterns or Java records where appropriate for new DTOs.
- Avoid exposing internal entity structures directly in API responses; create dedicated response records as the complexity grows.

## 7. Formatting & Output
- Output format negotiation governed by `ReportFormat` enum. Add new formats by:
  1. Adding enum constant with proper MIME type & extension.
  2. Implementing `ReportFormatter`.
  3. Ensuring `ReportFormatterService` auto-detects via Spring component scanning.
- Keep formatters stateless and thread-safe.

## 8. Testing Strategy
- Mandatory: Unit tests for any new logic branch or public method with business rules.
- Use JUnit 5 & Mockito.
- Prefer slice tests (@WebMvcTest) for controller additions; only use `@SpringBootTest` when full context is required.
- Add representative sample thread dumps for edge cases (deadlocks, high blocking, identical stacks). Keep test resources small.
- For performance-sensitive utilities, include focused micro-benchmarks (if JMH introduced), but do not add JMH to default build without justification.
- Ensure test names express scenario + expectation (e.g., `analyze_shouldDetectDeadlock_whenCircularLocksPresent`).

## 9. Code Quality & Style
- Follow existing JavaDoc + naming conventions; all public types/methods require JavaDoc explaining intent.
- No wildcard imports. Optimize imports automatically.
- No commented‑out code blocks—delete or replace with explanatory JavaDoc/TODO referencing an issue.
- Keep methods < ~40 LOC; extract private helpers when branching grows.
- Use meaningful constant names instead of magic numbers in analyzers.

## 10. Handling Large / Malformed Input
- Validate non-empty input early; respond with 400 for blank or clearly invalid dumps.
- Add defensive parsing: guard against truncated stack traces, missing headers, or unknown thread state tokens.
- When rejecting input, do not echo full content in response.

## 11. Dependency Management
- Before adding a new library:
  - Check if JDK / Spring already provides equivalent functionality.
  - Consider impact on startup time and footprint.
  - Document rationale in PR description.
- Remove unused dependencies promptly to reduce attack surface.

## 12. Build & CI Guidelines
Suggested pipeline stages (automate via future GitHub Actions):
1. `mvn -q -DskipTests=false clean verify`
2. Static analysis (SpotBugs / SonarQube) – integrate when infra available.
3. Security scan (OWASP Dependency Check) – optional future addition.
4. Produce artifact via Spring Boot plugin.

Quality gates (must pass locally before PR):
- Build success
- Tests green
- No new warnings or unchecked casts unless justified
- No TODOs without linked issue

## 13. Versioning & Releases
- Use Semantic Versioning: MAJOR.MINOR.PATCH.
- Increment MINOR for backward-compatible features; PATCH for bug fixes.
- Include brief CHANGELOG entries (add `CHANGELOG.md` if not present when first needed).

## 14. Commit & Branch Conventions
- Branch naming: `feature/<short-desc>`, `bugfix/<issue-id>`, `chore/<scope>`, `perf/<scope>`, `test/<scope>`.
- Commit messages: Conventional Commits (e.g., `feat(analyzer): detect identical lock owners`).
- Squash merge recommended; preserve meaningful commit scopes.

## 15. Introducing New Analyzer Rules
For each new diagnostic rule:
1. Define detection criteria clearly in JavaDoc.
2. Add unit tests (positive + negative cases).
3. Ensure severity mapping consistent with existing types.
4. Update README (Capabilities section) + sample output if user-visible.
5. Keep suggestion phrasing actionable.

## 16. Logging Standards
- Levels: ERROR (failures), WARN (potential misconfig / partial analysis), INFO (high-level events: start/finish analysis), DEBUG (detailed parsing), TRACE (rare; temporary deep diagnostics only).
- Never log secrets, raw memory addresses (beyond those present intrinsically in dumps), or full dumps at INFO/WARN.

## 17. Error Handling
- Anticipated invalid PID → 400.
- Inaccessible process / permission issue → 503 or 403 depending on context (future enhancement).
- Parsing failures → 422 Unprocessable Entity (introduce when validation layer added); currently fallback to 400 acceptable until refactor.

## 18. Extensibility Roadmap (Non-binding)
- Add pluggable rule engine interface for analyzer.
- Support incremental or streaming dump ingestion.
- Add OpenTelemetry tracing around analysis execution.
- Provide optional compression for large JSON reports.

## 19. AI Agent Operational Checklist (Every Change)
1. Read `pom.xml` → confirm Java 21 & Spring Boot property unchanged (unless upgrading intentionally).
2. Search for duplicated logic before adding new code.
3. Add/adjust tests first (TDD where practical).
4. Implement code; ensure no star imports; add JavaDoc.
5. Run `mvn clean verify` locally.
6. Update README only if public behavior changed.
7. Reference this file in PR body for compliance confirmation.

## 20. Prohibited Actions
- Downgrading Java or Spring Boot.
- Introducing blocking calls on request threads for long-running operations without justification (consider async if added later).
- Committing generated or IDE metadata (except minimal required config).
- Adding experimental libraries without discussion.

## 21. Acceptance Definition for a PR
A PR is ready when:
- All tests pass.
- Coverage for new logic ≥ existing average (do not reduce).
- Analyzer performance not degraded (spot-check with a large sample dump if rule complexity added).
- Documentation updated for any new user-facing capability.
- Adheres to every relevant section above.

---
Maintainers may evolve this document; agents must always read the latest version prior to changes.

