package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.struct.signingv2.ApkSigningBlock;
import net.dongliu.apk.parser.struct.signingv2.Digest;
import net.dongliu.apk.parser.struct.signingv2.Signature;
import net.dongliu.apk.parser.struct.signingv2.SignerBlock;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.Unsigned;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

// see https://source.android.com/security/apksigning/v2

/**
 * The Apk Sign Block V2 Parser.
 */
public class ApkSignBlockParser {
    private final ByteBuffer data;

    public ApkSignBlockParser(final ByteBuffer data) {
        this.data = data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public ApkSigningBlock parse() throws CertificateException {
        // sign block found, read pairs
        final List<SignerBlock> signerBlocks = new ArrayList<>();
        while (this.data.remaining() >= 8) {
            final int id = this.data.getInt();
            final int size = Unsigned.ensureUInt(this.data.getInt());
            if (id == ApkSigningBlock.SIGNING_V2_ID) {
                final ByteBuffer signingV2Buffer = Buffers.sliceAndSkip(this.data, size);
                // now only care about apk signing v2 entry
                while (signingV2Buffer.hasRemaining()) {
                    final SignerBlock signerBlock = this.readSigningV2(signingV2Buffer);
                    signerBlocks.add(signerBlock);
                }
            } else {
                // just ignore now
                Buffers.position(this.data, this.data.position() + size);
            }
        }
        return new ApkSigningBlock(signerBlocks);
    }

    private SignerBlock readSigningV2(ByteBuffer buffer) throws CertificateException {
        buffer = this.readLenPrefixData(buffer);

        final ByteBuffer signedData = this.readLenPrefixData(buffer);
        final ByteBuffer digestsData = this.readLenPrefixData(signedData);
        final List<Digest> digests = this.readDigests(digestsData);
        final ByteBuffer certificateData = this.readLenPrefixData(signedData);
        final List<X509Certificate> certificates = this.readCertificates(certificateData);
        final ByteBuffer attributesData = this.readLenPrefixData(signedData);
        this.readAttributes(attributesData);

        final ByteBuffer signaturesData = this.readLenPrefixData(buffer);
        final List<Signature> signatures = this.readSignatures(signaturesData);

        final ByteBuffer publicKeyData = this.readLenPrefixData(buffer);
        return new SignerBlock(digests, certificates, signatures);
    }

    private List<Digest> readDigests(final ByteBuffer buffer) {
        final List<Digest> list = new ArrayList<>();
        while (buffer.hasRemaining()) {
            final ByteBuffer digestData = this.readLenPrefixData(buffer);
            final int algorithmID = digestData.getInt();
            final byte[] digest = Buffers.readBytes(digestData);
            list.add(new Digest(algorithmID, digest));
        }
        return list;
    }

    private List<X509Certificate> readCertificates(final ByteBuffer buffer) throws CertificateException {
        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        final List<X509Certificate> certificates = new ArrayList<>();
        while (buffer.hasRemaining()) {
            final ByteBuffer certificateData = this.readLenPrefixData(buffer);
            final Certificate certificate = certificateFactory.generateCertificate(
                    new ByteArrayInputStream(Buffers.readBytes(certificateData)));
            certificates.add((X509Certificate) certificate);
        }
        return certificates;
    }

    private void readAttributes(final ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            final ByteBuffer attributeData = this.readLenPrefixData(buffer);
            final int id = attributeData.getInt();
//            byte[] value = Buffers.readBytes(attributeData);
        }
    }

    private List<Signature> readSignatures(final ByteBuffer buffer) {
        final List<Signature> signatures = new ArrayList<>();
        while (buffer.hasRemaining()) {
            final ByteBuffer signatureData = this.readLenPrefixData(buffer);
            final int algorithmID = signatureData.getInt();
            final int signatureDataLen = Unsigned.ensureUInt(signatureData.getInt());
            final byte[] signature = Buffers.readBytes(signatureData, signatureDataLen);
            signatures.add(new Signature(algorithmID, signature));
        }
        return signatures;
    }


    private ByteBuffer readLenPrefixData(final ByteBuffer buffer) {
        final int len = Unsigned.ensureUInt(buffer.getInt());
        return Buffers.sliceAndSkip(buffer, len);
    }

    // 0x0101—RSASSA-PSS with SHA2-256 digest, SHA2-256 MGF1, 32 bytes of salt, trailer: 0xbc
    private static final int PSS_SHA_256 = 0x0101;
    // 0x0102—RSASSA-PSS with SHA2-512 digest, SHA2-512 MGF1, 64 bytes of salt, trailer: 0xbc
    private static final int PSS_SHA_512 = 0x0102;
    // 0x0103—RSASSA-PKCS1-v1_5 with SHA2-256 digest. This is for build systems which require deterministic signatures.
    private static final int PKCS1_SHA_256 = 0x0103;
    // 0x0104—RSASSA-PKCS1-v1_5 with SHA2-512 digest. This is for build systems which require deterministic signatures.
    private static final int PKCS1_SHA_512 = 0x0104;
    // 0x0201—ECDSA with SHA2-256 digest
    private static final int ECDSA_SHA_256 = 0x0201;
    // 0x0202—ECDSA with SHA2-512 digest
    private static final int ECDSA_SHA_512 = 0x0202;
    // 0x0301—DSA with SHA2-256 digest
    private static final int DSA_SHA_256 = 0x0301;

}
