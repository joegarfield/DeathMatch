package is.sly.garfield.deathmatch.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MapIOUtils {

    /**
     * Function that extracts a zip file into a given directory.
     *
     * @param source          location of the zip file to extract, a Path
     * @param targetDirectory directory to extract the zip in, a Path
     * @throws IOException when extraction fails
     */
    public static void extract(Path source, Path targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            //
            Path target = targetDirectory.resolve(zipEntry.getName()).normalize();
            if (zipEntry.isDirectory()) {
                Files.createDirectories(target);
            } else {
                if (target.getParent() != null) {
                    if (Files.notExists(target.getParent())) {
                        Files.createDirectories(target.getParent());
                    }
                }
                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
    }

    /**
     * Function that deletes a directory
     *
     * @param pathToDelete directory to delete, a Path
     * @throws IOException when directory deletion fails
     */
    public static void deleteDirectory(Path pathToDelete) throws IOException {
        Files.walk(pathToDelete)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
