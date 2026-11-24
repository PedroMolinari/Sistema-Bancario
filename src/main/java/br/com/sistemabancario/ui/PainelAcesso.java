package br.com.sistemabancario.ui;

import br.com.sistemabancario.service.SessaoBancaria;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/** Entrada autenticada, cadastro de cliente e provisionamento do primeiro administrador. */
final class PainelAcesso extends JPanel {
    interface OperacoesAcesso {
        SessaoBancaria autenticar(String login, char[] senha);
        SessaoBancaria registrarCliente(String nome, String login, char[] senha, BigDecimal saldoInicial);
        boolean administradorConfigurado();
        SessaoBancaria registrarAdministradorInicial(String nome, String login, char[] senha);
    }

    private final JButton entrar = EstiloSwing.botao("Entrar no aplicativo", true);
    private final JPanel acoesLogin = new JPanel(new GridBagLayout());
    private final JPanel areaConteudo = new JPanel(new GridBagLayout());
    private JPanel painelHero;
    private JPanel painelFormulario;
    private boolean modoAmplo;

    /** Construtor legado usado por testes isolados do alternador da aplicação. */
    PainelAcesso(Runnable aoEntrar) {
        super(new GridBagLayout());
        Objects.requireNonNull(aoEntrar);
        montarBase(false);
        entrar.addActionListener(evento -> aoEntrar.run());
    }

    PainelAcesso(OperacoesAcesso operacoes, Consumer<SessaoBancaria> acessoConcluido) {
        super(new GridBagLayout());
        Objects.requireNonNull(operacoes);
        Objects.requireNonNull(acessoConcluido);
        montarBase(true);

        JTextField login = new JTextField();
        JPasswordField senha = new JPasswordField();
        EstiloSwing.campoClaro(login);
        EstiloSwing.campoClaro(senha);
        login.setPreferredSize(new Dimension(300, EstiloSwing.TAMANHO_CAMPO.height));
        senha.setPreferredSize(new Dimension(300, EstiloSwing.TAMANHO_CAMPO.height));
        JPanel formulario = new JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        formulario.setOpaque(false);
        formulario.add(EstiloSwing.rotuloCampoClaro("Usuário"));
        formulario.add(login);
        formulario.add(EstiloSwing.rotuloCampoClaro("Senha"));
        formulario.add(senha);
        acoesLogin.setLayout(new javax.swing.BoxLayout(acoesLogin, javax.swing.BoxLayout.Y_AXIS));
        entrar.setText("Entrar");
        entrar.setPreferredSize(new Dimension(300, 40));
        entrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        entrar.setAlignmentX(LEFT_ALIGNMENT);
        formulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, formulario.getPreferredSize().height));
        formulario.setAlignmentX(LEFT_ALIGNMENT);
        acoesLogin.add(formulario);
        acoesLogin.add(javax.swing.Box.createVerticalStrut(10));
        acoesLogin.add(entrar);

        JButton novoCliente = EstiloSwing.botaoCompacto("Criar acesso de cliente", false);
        JButton configurarAdmin = EstiloSwing.botaoCompacto("Configurar administrador", false);
        novoCliente.setMaximumSize(new Dimension(Integer.MAX_VALUE, EstiloSwing.TAMANHO_BOTAO_COMPACTO.height));
        configurarAdmin.setMaximumSize(new Dimension(Integer.MAX_VALUE, EstiloSwing.TAMANHO_BOTAO_COMPACTO.height));
        configurarAdmin.setVisible(!operacoes.administradorConfigurado());
        novoCliente.setForeground(EstiloSwing.ROSA_AVERMELHADO);
        configurarAdmin.setForeground(EstiloSwing.ROSA_AVERMELHADO);
        JPanel acoesSecundarias = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        acoesSecundarias.setOpaque(false);
        acoesSecundarias.setPreferredSize(new Dimension(300, EstiloSwing.TAMANHO_BOTAO_COMPACTO.height));
        acoesSecundarias.setMaximumSize(new Dimension(Integer.MAX_VALUE, EstiloSwing.TAMANHO_BOTAO_COMPACTO.height));
        acoesSecundarias.setAlignmentX(LEFT_ALIGNMENT);
        acoesLogin.add(javax.swing.Box.createVerticalStrut(8));
        acoesSecundarias.add(novoCliente);
        acoesSecundarias.add(configurarAdmin);
        acoesLogin.add(acoesSecundarias);
        entrar.addActionListener(evento -> {
            char[] senhaDigitada = senha.getPassword();
            executarAcesso(() -> operacoes.autenticar(login.getText(), senhaDigitada),
                    acessoConcluido, senhaDigitada, senha);
        });
        novoCliente.addActionListener(evento -> cadastrarCliente(operacoes, acessoConcluido));
        configurarAdmin.addActionListener(evento -> provisionarAdmin(operacoes, acessoConcluido));
    }

    JButton botaoEntrar() { return entrar; }
    boolean modoAmplo() { return modoAmplo; }

    private void montarBase(boolean autenticar) {
        setBackground(EstiloSwing.FUNDO);
        setOpaque(true);

        JPanel marca = new JPanel(new BorderLayout(0, 10));
        marca.setOpaque(false);
        JLabel monograma = new JLabel("SB", SwingConstants.CENTER);
        monograma.setFont(EstiloSwing.FONTE_MONOGRAMA);
        monograma.setForeground(Color.WHITE);
        monograma.setOpaque(true);
        monograma.setBackground(EstiloSwing.VINHO);
        monograma.setPreferredSize(new Dimension(58, 58));
        monograma.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloSwing.ROSA_ATIVO, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setOpaque(false);
        logo.add(monograma);
        JLabel nome = new JLabel("Sistema Bancário", SwingConstants.CENTER);
        nome.setFont(EstiloSwing.FONTE_MARCA_LOGIN);
        nome.setForeground(EstiloSwing.ROSA_AVERMELHADO);
        JLabel subtitulo = new JLabel(autenticar ? "Acesso seguro às suas contas"
                : "Sua rotina financeira, em um só lugar", SwingConstants.CENTER);
        subtitulo.setFont(EstiloSwing.FONTE_AUXILIAR);
        subtitulo.setForeground(EstiloSwing.SECUNDARIO);
        JPanel textos = new JPanel(new BorderLayout(0, 6));
        textos.setOpaque(false);
        textos.add(nome, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.SOUTH);
        marca.add(logo, BorderLayout.NORTH);
        marca.add(textos, BorderLayout.CENTER);

        painelHero = new JPanel(new BorderLayout(0, 28));
        painelHero.setOpaque(false);
        painelHero.setBorder(BorderFactory.createEmptyBorder(24, 22, 24, 26));
        JLabel chamada = new JLabel("<html><body style='width:310px'>" + (autenticar
                ? "Clareza para acompanhar o que importa."
                : "Seu espaço para cuidar das finanças.") + "</body></html>");
        chamada.setFont(EstiloSwing.FONTE_CHAMADA);
        chamada.setForeground(EstiloSwing.TEXTO);
        JLabel apoioHero = new JLabel("<html><body style='width:300px'>Contas, operações e histórico em uma experiência "
                + "simples, organizada e adequada ao seu perfil de acesso.</body></html>");
        apoioHero.setFont(EstiloSwing.FONTE_CORPO);
        apoioHero.setForeground(EstiloSwing.SECUNDARIO);
        JPanel destaque = new JPanel(new BorderLayout(0, 12));
        destaque.setOpaque(false);
        JLabel detalhe = new JLabel("CONTROLE FINANCEIRO COM CLAREZA");
        detalhe.setFont(EstiloSwing.FONTE_MICRO);
        detalhe.setForeground(EstiloSwing.ROSA_ATIVO);
        destaque.add(detalhe, BorderLayout.NORTH);
        destaque.add(chamada, BorderLayout.CENTER);
        destaque.add(apoioHero, BorderLayout.SOUTH);
        painelHero.add(marca, BorderLayout.NORTH);
        painelHero.add(destaque, BorderLayout.CENTER);

        JPanel cartao = new JPanel(new BorderLayout(0, 12));
        EstiloSwing.cartaoClaro(cartao);
        JLabel titulo = new JLabel(autenticar ? "Entrar" : "Bem-vindo", SwingConstants.CENTER);
        titulo.setFont(EstiloSwing.FONTE_TITULO_CARTAO);
        titulo.setForeground(EstiloSwing.TEXTO_ESCURO);
        JLabel explicacao = new JLabel(autenticar ? "Use seu usuário e senha para continuar."
                : "Acesse as funções de contas e operações bancárias.", SwingConstants.CENTER);
        explicacao.setFont(EstiloSwing.FONTE_CORPO);
        explicacao.setForeground(EstiloSwing.SECUNDARIO_CLARO);
        JPanel descricao = new JPanel(new BorderLayout(0, 7));
        descricao.setOpaque(false);
        descricao.add(titulo, BorderLayout.NORTH);
        descricao.add(explicacao, BorderLayout.CENTER);
        entrar.setPreferredSize(new Dimension(300, 40));
        acoesLogin.setOpaque(false);
        acoesLogin.add(entrar);
        cartao.add(descricao, BorderLayout.NORTH);
        JPanel alinhamentoAcoes = new JPanel(new GridBagLayout());
        alinhamentoAcoes.setOpaque(false);
        GridBagConstraints centralizarAcoes = new GridBagConstraints();
        centralizarAcoes.gridx = 0;
        centralizarAcoes.gridy = 0;
        centralizarAcoes.weightx = 1;
        centralizarAcoes.anchor = GridBagConstraints.CENTER;
        centralizarAcoes.fill = GridBagConstraints.HORIZONTAL;
        alinhamentoAcoes.add(acoesLogin, centralizarAcoes);
        cartao.add(alinhamentoAcoes, BorderLayout.CENTER);

        JLabel aviso = new JLabel(autenticar ? "Acesso seguro · cada perfil visualiza somente os dados autorizados."
                : "Acesso geral ao aplicativo · sem login individual", SwingConstants.CENTER);
        aviso.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        aviso.setForeground(EstiloSwing.SECUNDARIO);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(aviso, BorderLayout.CENTER);
        painelFormulario = new JPanel(new BorderLayout(0, 12));
        painelFormulario.setOpaque(false);
        painelFormulario.add(cartao, BorderLayout.CENTER);
        painelFormulario.add(inferior, BorderLayout.SOUTH);
        areaConteudo.setOpaque(false);
        GridBagConstraints externo = new GridBagConstraints();
        externo.gridx = 0; externo.gridy = 0; externo.weightx = 1; externo.weighty = 1;
        externo.fill = GridBagConstraints.BOTH;
        externo.insets = new Insets(18, 18, 18, 18);
        add(areaConteudo, externo);
        ajustarLayoutAcesso(false);
    }

    @Override public void doLayout() {
        ajustarLayoutAcesso(getWidth() >= EstiloSwing.LARGURA_ACESSO_AMPLA);
        super.doLayout();
    }

    private void ajustarLayoutAcesso(boolean amplo) {
        if (painelFormulario == null || modoAmplo == amplo && areaConteudo.getComponentCount() > 0) return;
        modoAmplo = amplo;
        areaConteudo.removeAll();
        areaConteudo.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        if (amplo) {
            painelHero.setPreferredSize(new Dimension(390, 440));
            painelFormulario.setPreferredSize(new Dimension(430, 440));
            c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 0, 0, 28);
            areaConteudo.add(painelHero, c);
            c.gridx = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 0, 0, 0);
            areaConteudo.add(painelFormulario, c);
            areaConteudo.setPreferredSize(new Dimension(850, 440));
        } else {
            painelFormulario.setPreferredSize(new Dimension(354, 500));
            c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.NONE;
            areaConteudo.add(painelFormulario, c);
            areaConteudo.setPreferredSize(new Dimension(354, 500));
        }
        areaConteudo.revalidate();
        areaConteudo.repaint();
    }

    private void executarAcesso(java.util.function.Supplier<SessaoBancaria> operacao,
                                Consumer<SessaoBancaria> concluido, char[] senha, JPasswordField senhaCampo) {
        try {
            SessaoBancaria sessao = operacao.get();
            concluido.accept(sessao);
        } catch (RuntimeException erro) {
            DialogoConfirmacao.erro(this, "Acesso não autorizado", erro.getMessage());
        } finally {
            java.util.Arrays.fill(senha, '\0');
            senhaCampo.setText("");
        }
    }

    private void cadastrarCliente(OperacoesAcesso operacoes, Consumer<SessaoBancaria> concluido) {
        JTextField nome = campoDialogo();
        JTextField login = campoDialogo();
        JPasswordField senha = campoSenhaDialogo();
        JTextField saldo = campoDialogo();
        saldo.setText("0,00");
        JPanel campos = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        campos.setOpaque(false);
        adicionarCampo(campos, "Nome do titular", nome);
        adicionarCampo(campos, "Novo usuário", login);
        adicionarCampo(campos, "Senha (mínimo de 10 caracteres)", senha);
        adicionarCampo(campos, "Saldo inicial (R$)", saldo);
        if (!mostrarFormulario("Criar acesso de cliente", "A conta é vinculada ao novo perfil de cliente.",
                campos, "Criar acesso")) return;
        char[] segredo = senha.getPassword();
        try {
            SessaoBancaria sessao = operacoes.registrarCliente(nome.getText(), login.getText(), segredo,
                    PainelCadastroConta.lerValor(saldo.getText()));
            DialogoConfirmacao.sucesso(this, "Cadastro concluído", "Conta criada. Você entrou como cliente.");
            concluido.accept(sessao);
        } catch (RuntimeException erro) {
            DialogoConfirmacao.erro(this, "Cadastro não concluído", erro.getMessage());
        } finally {
            java.util.Arrays.fill(segredo, '\0');
            senha.setText("");
        }
    }

    private void provisionarAdmin(OperacoesAcesso operacoes, Consumer<SessaoBancaria> concluido) {
        if (operacoes.administradorConfigurado()) {
            DialogoConfirmacao.erro(this, "Configuração indisponível",
                    "O administrador inicial já foi configurado.");
            return;
        }
        JTextField nome = campoDialogo();
        JTextField login = campoDialogo();
        JPasswordField senha = campoSenhaDialogo();
        JPanel campos = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        campos.setOpaque(false);
        adicionarCampo(campos, "Nome do administrador", nome);
        adicionarCampo(campos, "Usuário do administrador", login);
        adicionarCampo(campos, "Senha (mínimo de 10 caracteres)", senha);
        if (!mostrarFormulario("Configurar primeiro administrador",
                "Crie o perfil administrativo inicial do sistema.", campos, "Configurar")) return;
        char[] segredo = senha.getPassword();
        try {
            SessaoBancaria sessao = operacoes.registrarAdministradorInicial(nome.getText(), login.getText(), segredo);
            DialogoConfirmacao.sucesso(this, "Configuração concluída",
                    "Administrador configurado. Nenhuma conta bancária foi criada.");
            concluido.accept(sessao);
        } catch (RuntimeException erro) {
            DialogoConfirmacao.erro(this, "Configuração não concluída", erro.getMessage());
        } finally {
            java.util.Arrays.fill(segredo, '\0');
            senha.setText("");
        }
    }

    private static JTextField campoDialogo() {
        JTextField campo = new JTextField();
        EstiloSwing.campo(campo);
        return campo;
    }

    private static JPasswordField campoSenhaDialogo() {
        JPasswordField campo = new JPasswordField();
        EstiloSwing.campo(campo);
        return campo;
    }

    private static void adicionarCampo(JPanel painel, String texto, JComponent campo) {
        JLabel rotulo = EstiloSwing.rotuloCampo(texto);
        rotulo.setFont(EstiloSwing.FONTE_AUXILIAR.deriveFont(Font.BOLD));
        campo.setPreferredSize(new Dimension(340, EstiloSwing.TAMANHO_CAMPO.height));
        painel.add(rotulo);
        painel.add(campo);
    }

    private boolean mostrarFormulario(String titulo, String descricao, JPanel campos, String textoConfirmar) {
        Window proprietaria = SwingUtilities.getWindowAncestor(this);
        JDialog dialogo = new JDialog(proprietaria, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        JPanel conteudo = new JPanel(new BorderLayout(0, 14));
        EstiloSwing.cartao(conteudo);
        JPanel cabecalho = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        cabecalho.setOpaque(false);
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(EstiloSwing.FONTE_SECAO);
        tituloLabel.setForeground(EstiloSwing.TEXTO);
        JLabel detalhe = new JLabel("<html><body style='width:340px'>" + descricao + "</body></html>");
        detalhe.setFont(EstiloSwing.FONTE_AUXILIAR);
        detalhe.setForeground(EstiloSwing.SECUNDARIO);
        cabecalho.add(tituloLabel);
        cabecalho.add(detalhe);
        conteudo.add(cabecalho, BorderLayout.NORTH);
        conteudo.add(campos, BorderLayout.CENTER);
        JButton cancelar = EstiloSwing.botaoCompacto("Cancelar", false);
        JButton confirmar = EstiloSwing.botaoCompacto(textoConfirmar, true);
        boolean[] confirmado = {false};
        cancelar.addActionListener(evento -> dialogo.dispose());
        confirmar.addActionListener(evento -> { confirmado[0] = true; dialogo.dispose(); });
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(cancelar);
        botoes.add(confirmar);
        conteudo.add(botoes, BorderLayout.SOUTH);
        dialogo.setContentPane(conteudo);
        dialogo.getRootPane().setDefaultButton(confirmar);
        dialogo.getRootPane().registerKeyboardAction(evento -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.pack();
        dialogo.setMinimumSize(new Dimension(420, dialogo.getHeight()));
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        return confirmado[0];
    }
}
