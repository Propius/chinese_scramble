## Comprehensive PR Review Prompt Template

Based on your review process, here's a detailed prompt template that
captures everything you've asked for:

Please perform a comprehensive Pull Request review for [repository/feature
name].

## Review Checklist

### 1. Initial Analysis
- Before reviewing this PR, please make sure to understand the context.
    - You can find all the relevant background information, design decisions, and ticket scope in the linked JIRA ticket. This will help you:

        - Understand why the changes were made
        - Align with the business and technical intent
        - Review the code with full context instead of assumptions

    - Once you're familiar with the JIRA context, proceed with the code review.
- List all changes in the PR (git diff)
- Count and identify all new/modified endpoints
- Understand the project context (e.g., scheduler service, API service,
  etc.)
- This service/module will be deployed in GKE and uses Google Cloud services which can have multiple replicas.

### 2. REST API Compliance Review
- Verify HTTP methods match operations (GET for reads, POST for actions)
- Check endpoint naming conventions (nouns, not verbs)
- Suggest REST-compliant alternatives for non-compliant endpoints
- Show complete API specifications with:
    - HTTP method and path
    - Request body schema
    - Query/path parameters
    - Response format
    - Status codes

### 3. Code Quality Analysis

#### Magic Numbers
- Identify all hardcoded values
- Suggest extraction to constants/configuration
- Check for configurable thresholds

#### SOLID Principles
- Single Responsibility: One class, one purpose
- Open/Closed: Extensibility without modification
- Liskov Substitution: Proper inheritance
- Interface Segregation: Focused interfaces
- Dependency Inversion: Depend on abstractions

#### Readability
- Variable and method naming clarity
- Code complexity (cognitive load)
- Comment quality and necessity
- SQL query formatting and complexity

#### Maintainability
- Configuration externalization
- Error handling completeness
- Logging strategy
- Transaction management
- Async operation handling
- Test coverage

### 4. Security Review (OWASP Top 10)
- A01: Broken Access Control - Check authentication/authorization
- A02: Cryptographic Failures - Review sensitive data handling
- A03: Injection - SQL/Command injection risks
- A04: Insecure Design - Architecture security flaws
- A05: Security Misconfiguration - Configuration issues
- A06: Vulnerable Components - Dependency security
- A07: Authentication Failures - Auth mechanism review
- A08: Data Integrity Failures - CSRF, integrity checks
- A09: Security Logging Failures - Audit trail completeness
- A10: SSRF - Request forgery protection

### 5. SonarQube Standards Compliance
Check against default SonarQube rules:

#### Code Smells
- Duplicated code blocks (> 3%)
- Cognitive complexity (< 15)
- Cyclomatic complexity (< 10)
- Method length (< 50 lines)
- Class length (< 750 lines)
- Parameter count (< 7)

#### Bugs
- Null pointer risks
- Resource leaks
- Thread safety issues
- Exception handling

#### Vulnerabilities
- SQL injection
- XSS risks
- Path traversal
- Hardcoded credentials

#### Security Hotspots
- Weak cryptography
- Permissions issues
- Input validation
- Error information leakage

#### Metrics Targets
- Code coverage: > 80%
- Documentation: All public APIs
- Technical debt ratio: < 5%
- Maintainability rating: A
- Reliability rating: A
- Security rating: A

### 6. Specific Focus Areas
Ensure the code is aligned with business requirements and project goals as stated in the JIRA ticket.
For scheduler/job services:
- Proper use of @Async and @EnableAsync
- Transaction boundaries
- Job idempotency
- Retry mechanisms
- Rate limiting
- Monitoring/observability

### 7. Deliverables

Provide:
1. **Executive Summary** - Overall assessment and risk level
2. **Critical Issues** - Must fix before merge
3. **Major Issues** - Should fix soon
4. **Minor Issues** - Nice to have improvements
5. **Code Examples** - Show both problems and solutions
6. **Metrics Score Card**:
    - REST Compliance: X/5
    - SOLID Principles: X/5
    - Security (OWASP): X/5
    - Code Quality (SonarQube): X/5
    - Maintainability: X/5
    - Test Coverage: X%

### 8. Example Format for Findings

For each issue found:
Issue Type: [Critical/Major/Minor]
Category: [Security/Quality/Design/Performance]
Location: [File:Line]
Current Code:
[code snippet]
Problem: [explanation]
Suggested Fix:
[improved code]
References: [OWASP/SonarQube rule]

Please review the code thoroughly and provide actionable feedback with
concrete examples.
