package org.stocker.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 *
 */
public class ZipReaderImpl implements ZipReader{

    ZipInputStream zipInputStream;

    @Override
    public StringBuilder readInputSteamFromZip(InputStream inputStream) throws IOException {
        zipInputStream = new ZipInputStream(inputStream);
        StringBuilder s = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read;
        while ((zipInputStream.getNextEntry())!= null) {
            while ((read = zipInputStream.read(buffer, 0, 1024)) >= 0) {
                s.append(new String(buffer, 0, read));
            }
        }
        zipInputStream.close();
        return s;
    }
}
