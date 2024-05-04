package balbucio.emailsender;

import de.milchreis.uibooster.UiBooster;
import de.milchreis.uibooster.components.ProgressDialog;
import de.milchreis.uibooster.model.Form;
import de.milchreis.uibooster.model.LoginCredentials;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    private UiBooster ui;

    public Main() {
        this.ui = new UiBooster();
        LoginCredentials login = ui.showLogin("Insira as credenciais de login:", "Email Login", "Login", "Pass", "Continuar", "Cancelar");
        try {
            String[] emails = ui.showTextInputDialog("Enviar para quem? (use , para adicionar outros emails)")
                    .replace(" ", "").split(",");

            List<File> atts = new ArrayList<>();
            Form form = ui.createForm("Detalhes do Email")
                    .addText("Qual é o assunto?")
                    .addText("Insira uma descrição")
                    .addTextArea("Insira o conteúdo HTML:")
                    .addButton("Adicionar arquivo", () -> {
                        File f = ui.showFileSelection("Adicionar arquivos para enviar");
                        atts.add(f);
                    })
                    .show();

            ProgressDialog progress = ui.showProgressDialog("Enviando emails...", "Enviando", 0, emails.length);
            int i = 0;
            for (String email : emails) {
                progress.setProgress(i++);
                HtmlEmail mail = getHtmlEmail(login, form, atts);
                mail.addTo(email);
                mail.send();
            }
            progress.close();
            System.exit(0);
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }

    private static HtmlEmail getHtmlEmail(LoginCredentials login, Form form, List<File> atts) throws EmailException {
        HtmlEmail mail = new HtmlEmail();
        mail.setHostName("smtp.zoho.com");
        mail.setSmtpPort(465);
        mail.setAuthentication(login.getUsername(), login.getPassword());
        mail.setSSLOnConnect(true);
        mail.setFrom("noreply@hyperpowered.net");
        mail.setSubject(form.getByIndex(0).asString());
        mail.setTextMsg(form.getByIndex(1).asString());
        mail.setHtmlMsg(form.getByIndex(2).asString());
        atts.forEach(f -> {
            try {
                FileInputStream in = new FileInputStream(f);
                mail.attach(new ByteArrayDataSource(in, "application/octet-stream"), f.getName(), "File sent is "+f.getName(), EmailAttachment.ATTACHMENT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return mail;
    }
}