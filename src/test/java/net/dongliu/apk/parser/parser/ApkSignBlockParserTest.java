package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.utils.Inputs;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

public class ApkSignBlockParserTest {

    @Test
    public void parse() throws IOException, CertificateException {
        byte[] bytes = Inputs.readAll(getClass().getResourceAsStream("/sign/gmail_sign_block"));
        ApkSignBlockParser parser = new ApkSignBlockParser(ByteBuffer.wrap(bytes));
        parser.parse();
    }
}