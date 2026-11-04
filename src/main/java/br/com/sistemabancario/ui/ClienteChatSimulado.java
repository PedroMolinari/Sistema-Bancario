package br.com.sistemabancario.ui;

import java.text.Normalizer;
import java.util.Locale;

/** Cliente provisório. Será substituído por um cliente de IA em uma etapa posterior. */
final class ClienteChatSimulado implements ClienteChat {
    @Override
    public String responder(String mensagem) {
        String pergunta = normalizar(mensagem);
        if (pergunta.isBlank()) {
            return "Digite uma dúvida geral sobre o Sistema Bancário.";
        }
        if (contem(pergunta, "meu saldo", "minha conta", "meu extrato", "extrato da minha conta",
                "dados pessoais", "historico da minha conta", "quanto tenho")) {
            return "Não consulto dados pessoais, saldos ou movimentações. Posso explicar como usar as funções do sistema.";
        }
        if (contem(pergunta, "transfira", "transferir agora", "deposite", "sacar agora", "pague",
                "execute a transferencia", "faca um deposito", "faca um saque", "faca a transferencia",
                "excluir a conta", "cadastre a conta")) {
            return "Este chat não executa operações bancárias. Use as telas próprias do sistema para realizar operações.";
        }
        if (contem(pergunta, "extrato", "exportar", "arquivo txt", "arquivo de texto")) {
            return "Na tela Extrato, selecione uma conta, consulte as movimentações e use “Exportar TXT” para salvar o arquivo.";
        }
        if (contem(pergunta, "transferencia", "deposito", "saque", "conta", "funcionalidade", "sistema", "faz", "oferece")) {
            return "O Sistema Bancário permite cadastrar contas, consultar contas e extratos, além de realizar depósitos, saques e transferências nas telas próprias.";
        }
        return "Sou um assistente simulado e só respondo dúvidas gerais sobre as funções do Sistema Bancário. Não consulto dados nem realizo operações.";
    }

    private static boolean contem(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(normalizar(termo))) return true;
        }
        return false;
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).strip();
    }
}
