package br.com.sistemabancario.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteChatSimuladoTest {
    private final ClienteChatSimulado cliente = new ClienteChatSimulado();

    @Test
    void respondePerguntasGeraisSobreFuncoesDoSistema() {
        String resposta = cliente.responder("Quais funcionalidades o sistema oferece?");

        assertTrue(resposta.contains("cadastrar contas"));
        assertTrue(resposta.contains("depósitos, saques e transferências"));
    }

    @Test
    void explicaComoExportarExtratoSemConsultarDadosDaConta() {
        String resposta = cliente.responder("Como exportar um extrato?");

        assertTrue(resposta.contains("Exportar TXT"));
    }

    @Test
    void naoConsultaDadosPessoaisNemExecutaOperacoes() {
        String respostaSaldo = cliente.responder("Qual é o meu saldo?");
        String respostaOperacao = cliente.responder("Transfira dinheiro para outra conta");

        assertTrue(respostaSaldo.contains("Não consulto dados pessoais"));
        assertTrue(respostaOperacao.contains("não executa operações bancárias"));
    }

    @Test
    void orientaQuandoPerguntaNaoEhSobreOProjeto() {
        String resposta = cliente.responder("Qual é a previsão do tempo?");

        assertTrue(resposta.contains("assistente simulado"));
    }
}
