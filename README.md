# Root L Flux

plataforma de orquestração, processamento e gerenciamento de fluxos de eventos distribuídos.

## Estrutura do Projeto

```text
root-l-flux/
├── AGENTS.md                        # Instruções de contexto para o Antigravity
├── project-context.md               # Contexto vivo do projeto
├── root-l-flux.code-workspace  # Workspace multi-root (Projeto + Knowledge Base)
├── .agents/
│   ├── rules/                       # Regras específicas do projeto
│   └── skills/                      # Skills específicas do projeto
├── docs/
│   ├── index.md
│   ├── architecture/
│   ├── decisions/
│   ├── requirements/
│   ├── roadmap/
│   └── references/
├── src/                             # Código-fonte
└── tests/                           # Testes automatizados
```

## Como Trabalhar neste Projeto

1. Abra a raiz deste diretório (ou o arquivo `root-l-flux.code-workspace`) no Google Antigravity.
2. Mantenha `project-context.md` atualizado conforme decisões e entregas avançarem.
3. Execute a validação estrutural do projeto a partir da raiz do workspace:
   ```powershell
   python ../../tools/scripts/validate-project.py .
   ```

