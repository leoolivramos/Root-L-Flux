# ADR-002: Adoção de Arquitetura Hexagonal e Monólito Modular

- **Estado:** Proposto
- **Data:** 2026-11-04
- **Projeto:** `root-l-flux`
- **Autor:** Root L Flux Team

---

## 1. Contexto

O **Root L Flux** é uma plataforma que orquestra e gerencia fluxos distribuídos de eventos. Conforme o escopo do projeto, é fundamental evitar acoplamento prematuro com provedores específicos de persistência, mensageria ou frameworks web, além de manter facilidade de testes unitários isolados.

## 2. Problema

Como estruturar o código-fonte em `src/` para que:
1. As regras de negócio de fluxos, validação de eventos e idempotência não dependam de frameworks externos (Spring, Hibernate, Jackson, Kafka).
2. Adaptadores de infraestrutura (REST, Kafka, PostgreSQL) possam ser substituídos ou evoluídos sem impactos no núcleo.
3. Não haja complexidade operacional desnecessária de microsserviços na fase inicial.

## 3. Alternativas Consideradas

1. **Arquitetura em Camadas Tradicional (Controller-Service-Repository):**
   - *Prós:* Simples, familiar.
   - *Contras:* Alto acoplamento entre regras de negócio e anotações do framework/JPA; violação do princípio de inversão de dependência em relação à infraestrutura.
2. **Arquitetura Orientada a Microsserviços Imediata:**
   - *Prós:* Isolamento físico de serviços.
   - *Contras:* Complexidade operacional excessiva para o estágio atual (deploy distribuído, consistência, rede local lenta).
3. **Monólito Modular com Arquitetura Hexagonal (Ports and Adapters):**
   - *Prós:* Domínio limpo em POJOs puros (Java 25+), dependências invertidas via interfaces (`Port`), adaptadores isolados em pacotes bem definidos, alta testabilidade unitária e facilidade futura para extrair módulos se necessário.
   - *Contras:* Maior número de classes e conversores de dados (mappers entre Domain e DTO/Entity).

## 4. Decisão

Adotar o **Monólito Modular com Arquitetura Hexagonal (Ports and Adapters)**:
- Pacote `domain`: POJOs puros sem anotações de Spring ou persistência.
- Pacote `application`: Casos de uso e portas (Input/Output ports).
- Pacote `infrastructure`: Implementações de portas (REST controllers, Spring Data JPA, Kafka publishers/listeners).

## 5. Consequências

### Positivas
- Testes de domínio rápidos e independentes de infraestrutura ou containers.
- Flexibilidade para testar com repositórios em memória ou PostgreSQL sem alterar o domínio.
- Fácil migração futura de componentes se houver necessidade de escala independente.

### Negativas / Mitigações
- Necessidade de classes de mapeamento entre modelos de domínio e modelos de persistência/transporte (mitigado pelo uso de records do Java moderno e mappers explícitos).
