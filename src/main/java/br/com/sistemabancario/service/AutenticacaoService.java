package br.com.sistemabancario.service;

import br.com.sistemabancario.domain.Cliente;
import br.com.sistemabancario.domain.PerfilAcesso;
import br.com.sistemabancario.repository.jdbc.UsuarioPersistido;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Autentica usuários e emite sessões não forjáveis usadas pelo serviço bancário autorizado. */
public final class AutenticacaoService {
    private static final int ITERACOES = 210_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private final ServicoBancario banco;
    private final ConcurrentHashMap<UUID, Identidade> sessoes = new ConcurrentHashMap<>();

    public AutenticacaoService(ServicoBancario banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    public SessaoBancaria autenticar(String login, char[] senha) {
        char[] senhaRecebida = Objects.requireNonNull(senha, "A senha é obrigatória.");
        try {
            String normalizado = normalizarLogin(login);
            UsuarioPersistido usuario = banco.buscarUsuario(normalizado)
                    .orElseThrow(AutenticacaoService::credenciaisInvalidas);
            if (!validarSenha(senhaRecebida, usuario)) throw credenciaisInvalidas();
            return emitirSessao(usuario);
        } finally {
            java.util.Arrays.fill(senhaRecebida, '\0');
        }
    }

    public SessaoBancaria registrarCliente(String nome, String login, char[] senha, BigDecimal saldoInicial) {
        try {
            String normalizado = validarLoginNovo(login);
            validarSenhaNova(senha);
            Credencial credencial = criarCredencial(senha);
            Cliente cliente = new Cliente(nome);
            banco.criarContaAutenticada(cliente, Objects.requireNonNull(saldoInicial),
                    login.trim(), normalizado, credencial.hash(), credencial.salt());
            UsuarioPersistido usuario = banco.buscarUsuario(normalizado)
                    .orElseThrow(() -> new IllegalStateException("O acesso do cliente não foi persistido."));
            return emitirSessao(usuario);
        } finally {
            if (senha != null) java.util.Arrays.fill(senha, '\0');
        }
    }

    public boolean administradorConfigurado() { return banco.administradorConfigurado(); }

    public SessaoBancaria registrarAdministradorInicial(String nome, String login, char[] senha) {
        try {
            if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Informe o nome do administrador.");
            String normalizado = validarLoginNovo(login);
            validarSenhaNova(senha);
            Credencial credencial = criarCredencial(senha);
            banco.criarAdministrador(nome.trim(), login.trim(), normalizado, credencial.hash(), credencial.salt());
            UsuarioPersistido usuario = banco.buscarUsuario(normalizado)
                    .orElseThrow(() -> new IllegalStateException("O administrador não foi persistido."));
            return emitirSessao(usuario);
        } finally {
            if (senha != null) java.util.Arrays.fill(senha, '\0');
        }
    }

    public void encerrar(SessaoBancaria sessao) {
        if (sessao != null) sessoes.remove(sessao.identificadorInterno());
    }

    public br.com.sistemabancario.domain.PerfilAcesso perfil(SessaoBancaria sessao) {
        return exigirIdentidade(sessao).perfil();
    }

    Identidade exigirIdentidade(SessaoBancaria sessao) {
        if (sessao == null) throw new SecurityException("É necessário autenticar-se.");
        Identidade identidade = sessoes.get(sessao.identificadorInterno());
        if (identidade == null) throw new SecurityException("A sessão expirou ou foi encerrada.");
        return identidade;
    }

    private SessaoBancaria emitirSessao(UsuarioPersistido usuario) {
        PerfilAcesso perfil = PerfilAcesso.valueOf(usuario.perfil());
        SessaoBancaria sessao = new SessaoBancaria(UUID.randomUUID());
        sessoes.put(sessao.identificadorInterno(),
                new Identidade(usuario.id(), perfil, usuario.clienteId()));
        return sessao;
    }

    private static boolean validarSenha(char[] senha, UsuarioPersistido usuario) {
        try {
            byte[] salt = Base64.getDecoder().decode(usuario.saltSenha());
            byte[] esperado = Base64.getDecoder().decode(usuario.hashSenha());
            return MessageDigest.isEqual(esperado, derivar(senha, salt));
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    private static Credencial criarCredencial(char[] senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        return new Credencial(Base64.getEncoder().encodeToString(derivar(senha, salt)),
                Base64.getEncoder().encodeToString(salt));
    }

    private static byte[] derivar(char[] senha, byte[] salt) {
        PBEKeySpec especificacao = new PBEKeySpec(senha, salt, ITERACOES, TAMANHO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacao).getEncoded();
        } catch (java.security.GeneralSecurityException erro) {
            throw new IllegalStateException("Não foi possível proteger a senha.", erro);
        } finally {
            especificacao.clearPassword();
        }
    }

    private static String validarLoginNovo(String login) {
        String normalizado = normalizarLogin(login);
        if (!normalizado.matches("[a-z0-9._-]{3,64}")) {
            throw new IllegalArgumentException("O usuário deve ter de 3 a 64 caracteres: letras, números, ponto, hífen ou sublinhado.");
        }
        return normalizado;
    }

    private static String normalizarLogin(String login) {
        if (login == null) throw new IllegalArgumentException("Informe o usuário.");
        String normalizado = login.trim().toLowerCase(Locale.ROOT);
        if (normalizado.isBlank()) throw new IllegalArgumentException("Informe o usuário.");
        return normalizado;
    }

    private static void validarSenhaNova(char[] senha) {
        if (senha == null || senha.length < 10 || senha.length > 256) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 10 caracteres (máximo de 256).");
        }
    }

    private static SecurityException credenciaisInvalidas() {
        return new SecurityException("Usuário ou senha inválidos.");
    }

    record Identidade(UUID usuarioId, PerfilAcesso perfil, UUID clienteId) { }
    private record Credencial(String hash, String salt) { }
}
