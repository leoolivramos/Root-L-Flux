# Root L Flux — Agent Instructions

Você está trabalhando no projeto **Root L Flux** (`root-l-flux`).

## Context Resolution (Ordem Obrigatória)

Antes de propor arquitetura, escrever código ou executar comandos neste projeto:

1. Leia [`project-context.md`](./project-context.md) para compreender objetivo, escopo, stack tecnológica, restrições e estado atual.
2. Consulte [`docs/index.md`](./docs/index.md) e as decisões arquiteturais em [`docs/decisions/`](./docs/decisions/).
3. Consulte a Knowledge Base compartilhada em `c:\Users\01923483102\Documents\pessoal\root l workspace\knowledge\index.md`.
4. Verifique as regras locais em `.agents/rules/` e a skill do projeto em `.agents/skills/root-l-flux-development/SKILL.md`.

## Isolamento e Governança

- Não misture código, configurações ou dependências deste projeto com outros projetos de `projects/`.
- Operações destrutivas (Nível 3) ou alterações em conhecimento compartilhado / infraestrutura (Nível 2) exigem aprovação humana explícita.
- Toda decisão arquitetural relevante deve ser registrada como ADR em `docs/decisions/`.

