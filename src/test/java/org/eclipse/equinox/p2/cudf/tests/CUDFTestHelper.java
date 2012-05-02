package org.eclipse.equinox.p2.cudf.tests;

import java.io.*;
import org.apache.tools.bzip2.CBZip2InputStream;

public class CUDFTestHelper {

	public static InputStream getStream(File file) throws IOException {
		InputStream inputStream = null;
		if (file.getAbsolutePath().endsWith(".bz2")) {
			inputStream = new FileInputStream(file);
			int b = inputStream.read();
			if (b != 'B') {
				throw new IOException("not a bz2 file");
			}
			b = inputStream.read();
			if (b != 'Z') {
				throw new IOException("not a bz2 file");
			}
			inputStream = new CBZip2InputStream(inputStream);
		} else
			inputStream = new FileInputStream(file);
		return inputStream;
	}
}
