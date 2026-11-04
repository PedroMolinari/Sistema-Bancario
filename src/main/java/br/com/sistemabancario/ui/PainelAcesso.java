package br.com.sistemabancario.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Tela inicial de acesso ao aplicativo compartilhado; não representa autenticação. */
final class PainelAcesso extends JPanel {
    private final JButton entrar = EstiloSwing.botao("Entrar no aplicativo", true);

    PainelAcesso(Runnable aoEntrar) {
        super(new GridBagLayout());
        Objects.requireNonNull(aoEntrar);
        setBackground(EstiloSwing.FUNDO);

        JPanel coluna = new JPanel(new BorderLayout(0, 24));
        coluna.setOpaque(false);
        coluna.setPreferredSize(new Dimension(354, 600));

        JPanel marca = new JPanel(new BorderLayout(0, 12));
        marca.setOpaque(false);
        JLabel monograma = new JLabel("SB", SwingConstants.CENTER);
        monograma.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
        monograma.setForeground(Color.WHITE);
        monograma.setOpaque(true);
        monograma.setBackground(EstiloSwing.AZUL);
        monograma.setPreferredSize(new Dimension(76, 76));
        monograma.setBorder(BorderFactory.createLineBorder(EstiloSwing.AZUL_ATIVO, 2, true));
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setOpaque(false);
        logo.add(monograma);
        JLabel nome = new JLabel("Sistema Bancário", SwingConstants.CENTER);
        nome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
        nome.setForeground(EstiloSwing.MARINHO);
        JLabel subtitulo = new JLabel("Sua rotina financeira, em um só lugar", SwingConstants.CENTER);
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        subtitulo.setForeground(EstiloSwing.SECUNDARIO);
        JPanel textos = new JPanel(new BorderLayout(0, 7));
        textos.setOpaque(false);
        textos.add(nome, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.SOUTH);
        marca.add(logo, BorderLayout.NORTH);
        marca.add(textos, BorderLayout.CENTER);

        JPanel cartao = new JPanel(new BorderLayout(0, 16));
        EstiloSwing.cartao(cartao);
        JLabel titulo = new JLabel("Bem-vindo", SwingConstants.CENTER);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titulo.setForeground(EstiloSwing.TEXTO);
        JLabel explicacao = new JLabel("Acesse as funções de contas e operações bancárias.", SwingConstants.CENTER);
        explicacao.setFont(EstiloSwing.FONTE_CORPO);
        explicacao.setForeground(EstiloSwing.SECUNDARIO);
        JPanel descricao = new JPanel(new BorderLayout(0, 9));
        descricao.setOpaque(false);
        descricao.add(titulo, BorderLayout.NORTH);
        descricao.add(explicacao, BorderLayout.CENTER);
        entrar.setPreferredSize(new Dimension(300, 54));
        entrar.addActionListener(evento -> aoEntrar.run());
        JPanel acao = new JPanel(new GridBagLayout());
        acao.setOpaque(false);
        acao.add(entrar);
        cartao.add(descricao, BorderLayout.NORTH);
        cartao.add(acao, BorderLayout.CENTER);

        JLabel aviso = new JLabel("Acesso geral ao aplicativo · sem login individual", SwingConstants.CENTER);
        aviso.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        aviso.setForeground(EstiloSwing.SECUNDARIO);
        aviso.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(aviso, BorderLayout.CENTER);
        coluna.add(marca, BorderLayout.NORTH);
        coluna.add(cartao, BorderLayout.CENTER);
        coluna.add(inferior, BorderLayout.SOUTH);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(24, 18, 24, 18);
        add(coluna, c);
    }

    JButton botaoEntrar() { return entrar; }
}
