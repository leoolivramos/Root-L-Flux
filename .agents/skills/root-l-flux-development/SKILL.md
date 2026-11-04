---
name: root-l-flux-development
description: >-
  Guides development, architecture alignment, testing and documentation updates
  specifically for the Root L Flux (root-l-flux) project.
---

# Root L Flux Development

## Objective

Desenvolver, testar e documentar funcionalidades no projeto **Root L Flux** (`root-l-flux`) preservando seu contexto e decisões arquiteturais.

## Context acquisition

Antes de executar:

1. Ler `project-context.md` na raiz do projeto.
2. Consultar `docs/architecture/overview.md` e os ADRs em `docs/decisions/`.
3. Consultar a Engineering Knowledge Base em `../../knowledge/index.md` (especialmente `java/`, `spring/`, `distributed-systems/`, `messaging/`, `databases/`, `architecture/`) para padrões técnicos reutilizáveis.
4. Inspecionar a estrutura atual em `src/` e `tests/`.

## Workflow

1. Confirmar o requisito e verificar se exige atualização de ADR.
2. Planejar a alteração mantendo baixo acoplamento em `src/`.
3. Implementar o código seguindo os padrões de qualidade e segurança.
4. Criar ou atualizar testes automatizados em `tests/` e executá-los localmente.
5. Atualizar `project-context.md` e a documentação em `docs/`.
6. Se novo conhecimento reutilizável foi identificado, propor rascunho em `../../knowledge/inbox/`.

## Quality criteria

- Todos os testes locais em `tests/` passando.
- Conformidade com `.agents/rules/architecture.md` e `.agents/rules/project-security.md`.
- Documentação e `project-context.md` sincronizados com o código.

## Expected output

- Código-fonte implementado em `src/`.
- Testes automatizados verificados em `tests/`.
- Documentação técnica e diff prontos para revisão humana.

## Restrictions

Não executar operações destrutivas ou commits sem aprovação quando envolver mudanças arquiteturais.
Não misturar artefatos deste projeto com outros projetos de `projects/`.
