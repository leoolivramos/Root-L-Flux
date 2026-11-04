# ADR-001: Estrutura Inicial do Projeto Root L Flux

- **Estado:** Aceito
- **Data:** 2026-11-04
- **Projeto:** `root-l-flux`

---

## 1. Contexto

Inicialização padronizada do projeto `root-l-flux` dentro do ecossistema **Root L Workspace**.

## 2. Problema

Garantir isolamento de contexto, rastreabilidade de decisões, padronização documental e integração limpa com a Knowledge Base central.

## 3. Alternativas Consideradas

1. **Estrutura ad-hoc sem metadados de contexto:** Rejeitada por dificultar a recuperação precisa de contexto pelo agente.
2. **Template padronizado Root L Workspace com `project-context.md`, `.agents/` e `docs/`:** Selecionada.

## 4. Decisão

Adotar a estrutura padrão do Root L Workspace com repositório Git próprio, regras locais em `.agents/rules/`, skill específica em `.agents/skills/root-l-flux-development/` e compartilhamento de leitura da Knowledge Base.

## 5. Consequências

- Recuperação determinística de contexto pelo Antigravity.
- Separação clara entre código (`src/`), testes (`tests/`) e decisões (`docs/decisions/`).

