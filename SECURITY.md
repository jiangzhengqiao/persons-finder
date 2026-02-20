# SECURITY.md

## 1. How did you sanitize inputs before sending to the LLM?

We used two layers of sanitization:

- **Input validation**: Before sending the user's hobby or job to the AI, we check them against a list of blocked patterns (e.g., "ignore all instructions", "system prompt"). If a pattern is found, we reject the request with a 403 error. These patterns are stored in a database table.
- **Prompt hardening**: We put the user input into a strict prompt template. The AI only sees the data as plain text and is instructed not to follow any commands inside it. This way, even if a user tries to inject malicious instructions, they are treated as harmless text.

## 2. What are the privacy risks of sending PII (Personally Identifiable Information) like "Name" and "Location" to a third-party model?

Sending PII to a third‑party AI model (like OpenAI) creates serious risks:

- **Data leakage**: The third party may store, log, or use the data for training, which could expose sensitive information.
- **Regulatory violations**: Many countries (e.g., GDPR in Europe) have strict rules about transferring personal data outside your control.
- **Loss of control**: Once data leaves your system, you cannot guarantee its security or delete it later.

## 3. How would you architect this for a high-security banking app?

For a banking app, I would design it with privacy first:

- **Data minimization**: Remove all PII from the AI request. Send only non‑identifying details (e.g., job title, hobbies) to generate the bio. Name and location stay inside your own database.
- **Local AI model**: Deploy an open‑source model (like Llama 3) inside your own infrastructure. No data ever leaves your secure network.
- **Anonymization**: If you must use an external AI, replace real names with pseudonyms and precise location with a region (e.g., city level).
- **Encryption**: Encrypt all data in transit (TLS) and at rest (AES‑256). Use a proper secrets manager (like HashiCorp Vault) to store keys.
- **Audit logging**: Log every AI request (without PII) to detect and investigate suspicious activity.