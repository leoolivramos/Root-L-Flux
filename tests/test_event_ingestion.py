"""Teste de Fumaça e Validação de Contrato Estrutural do Root L Flux."""

import unittest
from pathlib import Path


class TesteContratosRootLFlux(unittest.TestCase):

    def test_estrutura_arquivos_fundamentais(self):
        raiz = Path(__file__).parent.parent
        self.assertTrue((raiz / "pom.xml").is_file(), "O arquivo pom.xml deve existir na raiz do projeto")
        self.assertTrue((raiz / "docker-compose.yml").is_file(), "O arquivo docker-compose.yml deve existir na raiz do projeto")
        self.assertTrue((raiz / "src/main/resources/application.yml").is_file(), "O arquivo application.yml deve existir")

    def test_modelos_de_dominio_presentes(self):
        diretorio_dominio = Path(__file__).parent.parent / "src/main/java/com/rootl/flux/domain/model"
        arquivos_esperados = [
            "Event.java",
            "EventId.java",
            "EventMetadata.java",
            "ExecutionStatus.java",
            "FlowExecution.java",
            "FlowDefinition.java",
            "StepDefinition.java",
            "StepExecution.java",
            "RetryPolicy.java"
        ]
        for nome_arquivo in arquivos_esperados:
            self.assertTrue((diretorio_dominio / nome_arquivo).is_file(), f"{nome_arquivo} deve existir em domain/model")


if __name__ == "__main__":
    unittest.main()
