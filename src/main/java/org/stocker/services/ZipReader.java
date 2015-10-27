package org.stocker.services;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public interface ZipReader {

    StringBuilder readInputSteamFromZip(InputStream inputStream) throws IOException;

}
