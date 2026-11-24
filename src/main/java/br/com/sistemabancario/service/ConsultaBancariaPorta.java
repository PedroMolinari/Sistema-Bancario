package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Conta;
import br.com.sistemabancario.domain.Transacao;
import java.util.List;
import java.util.UUID;

/** Fronteira restrita às consultas bancárias, sem operações de escrita. */
public interface ConsultaBancariaPorta {
    List<Conta> listarContas();

    Conta obterConta(UUID id);

    List<Transacao> consultarExtrato(UUID contaId);
}
