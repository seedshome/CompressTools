package com.toogen.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import lombok.extern.slf4j.Slf4j;

/**
 * The compress tools.
 * 
 * @author Toogen
 * @since 2021/8/3
 */
@Slf4j
public class CompressTools {

	/**
	 * Determine whether the current file type is ZIP(.zip) or GZIP(.gz).
	 */
	public static CompressionType compressionType(String file) {
		try (InputStream is = Files.newInputStream(Paths.get(file));
				BufferedInputStream bis = new BufferedInputStream(is);
				GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis);) {

			return CompressionType.GZIP;
		} catch (Exception e) {
			log.debug("An exception occurred while constructing the GzipCompressorInputStream.", e);

			try (ZipFile zipFile = new ZipFile(file);) {
			} catch (Exception e1) {
				log.debug("An exception occurred while constructing the ZipFile.", e1);
				return CompressionType.UNKNOWN;
			}
			return CompressionType.ZIP;
		}
	}
	
	/**
	 * Uncompress the gzip file, save in current directory.
	 */
	public static void uncompressGzip(String gzipFile) {
		// Obtain the directory where the gzip file resides.
		Path gzipPath = Paths.get(gzipFile);
		Path gzipParent= gzipPath.getParent();
		
		try (InputStream is = Files.newInputStream(Paths.get(gzipFile));
				BufferedInputStream bis = new BufferedInputStream(is);
				GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis);
				) {
			
			GzipParameters metaData = gcis.getMetaData();
			String uncompressedName = metaData.getFilename();
			Path uncompressedPath = Paths.get(gzipParent.toString(), uncompressedName);
			
			try (OutputStream os = Files.newOutputStream(uncompressedPath);
					BufferedOutputStream bos = new BufferedOutputStream(os);) {
				
				final byte[] buffer = new byte[1024];
				int n = 0;
				while (-1 != (n = gcis.read(buffer))) {
					bos.write(buffer, 0, n);
				}
			} catch (Exception e) {
				log.error("An exception occurred while uncompressing the gzip file.", e);
				throw new RuntimeException(e);
			}
			
		} catch (Exception e) {
			log.error("An exception occurred while uncompressing the gzip file.", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Uncompress the zip file, save in current directory.
	 */
	public static void uncompressZip(String zipFile) {
		try (ZipFile zipFileZ = new ZipFile(zipFile);) {
			// Obtain the directory where the zip file resides.
			Path zipPath = Paths.get(zipFile);
			Path zipParent = zipPath.getParent();

			Enumeration<ZipArchiveEntry> entries = zipFileZ.getEntries();
			while (entries.hasMoreElements()) {

				ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) entries.nextElement();
				Path uncompressFile = Paths.get(zipParent.toString(), zipArchiveEntry.getName());
				
				if (Files.isDirectory(uncompressFile)) { // The ZipArchiveEntry is directory.
					if (!Files.exists(uncompressFile)) {
						Files.createDirectory(uncompressFile);
					}
					continue;
				}
				
				Path uncompressFileParent = uncompressFile.getParent(); // The ZipArchiveEntry is regular file.
				if (!Files.exists(uncompressFileParent)) {
					Files.createDirectory(uncompressFileParent);
				}
				
				try (InputStream is = zipFileZ.getInputStream(zipArchiveEntry);
						BufferedInputStream bis = new BufferedInputStream(is);

						OutputStream os = Files.newOutputStream(uncompressFile);
						BufferedOutputStream bos = new BufferedOutputStream(os);) {

					final byte[] buffer = new byte[1024];
					int n = 0;
					while (-1 != (n = bis.read(buffer))) {
						bos.write(buffer, 0, n);
					}

				} catch (Exception e) {
					log.error("An exception occurred while uncompressing the zip file.", e);
					throw new RuntimeException(e);
				}
			}
		} catch (IOException e) {
			log.error("An exception occurred while uncompressing the zip file.", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Compress the zip file.
	 */
	public static void compressZip(String zipFile) {
		
	}
	
	public static void main(String[] args) {
		uncompressZip("D:\\EclipseWS-New\\files\\20210801\\1020\\xixi.zip");
	}
}
