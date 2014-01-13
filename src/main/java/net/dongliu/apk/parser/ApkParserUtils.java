package net.dongliu.apk.parser;

import net.dongliu.apk.parser.struct.AndroidFiles;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * convenient methods for apk parser
 *
 * @author dongliu
 */
public class ApkParserUtils {

    /**
     * Decode binary XML files
     */
    public static String getManifestXml(String apkFile, String local) throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(apkFile);
            ZipArchiveEntry manifestEntry = null;
            ZipArchiveEntry resourceEntry = null;
            Enumeration<ZipArchiveEntry> enu = zf.getEntries();
            while (enu.hasMoreElements()) {
                ZipArchiveEntry entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().equals(AndroidFiles.RESOURCE)) {
                    resourceEntry = entry;
                }
                if (entry.getName().equals(AndroidFiles.MANIFEST)) {
                    manifestEntry = entry;
                }

            }
            ResourceTableParser resourceTableParser = new ResourceTableParser(
                    zf.getInputStream(resourceEntry));
            resourceTableParser.parse();
            ResourceTable resourceTable = resourceTableParser.getResourceTable();

            BinaryXmlParser binaryXmlParser = new BinaryXmlParser(zf.getInputStream(manifestEntry),
                    resourceTable);
            binaryXmlParser.setPreferredLocal(local);
            binaryXmlParser.parse();
            return binaryXmlParser.getXml();
        } finally {
            ZipFile.closeQuietly(zf);
        }

    }


    public static void main(String args[]) throws IOException {
        String file = args[0];

        // Parse Binary XML
        System.out.println(getManifestXml(file, null));

    }
}
