package eu.planets_project.pp.plato.run;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Start up the web application from within the test context.  Good for debugging.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class PlatoStarter {

    /**
     * @param args if not 8080 then pass port as an integer arg
     * @throws Exception when it goes wrong
     */
    public static void main(String... args) throws Exception {
        System.setProperty("java.naming.factory.url.pkgs","org.mortbay.naming");
        System.setProperty("java.naming.factory.initial","org.mortbay.naming.InitialContextFactory");
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.addHandler(new WebAppContext(getProjectRoot().getAbsolutePath() + "/webapp-plato/src/main/webapp", "/plato"));
        HashUserRealm planetsRealm = new HashUserRealm("PlanetsRealm");
        server.setUserRealms(new UserRealm[] { planetsRealm });
        server.start();
    }

    private static File getProjectRoot() throws IOException {
        return getProjectRoot(new File(".").getCanonicalFile());
    }

    private static File getProjectRoot(File here) {
        if (here == null) {
            throw new RuntimeException("Couldn't find root");
        }
        else if (isProjectParentDir(here)) {
            return here;
        }
        else {
            return getProjectRoot(here.getParentFile());
        }
    }

    private static boolean isProjectParentDir(File here) {
        return checkFor(here, "webapp-plato");
    }

    private static boolean checkFor(File here, String... subDirectories) {
        File[] subdirs = here.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (String subDirectory : subDirectories) {
            if (!checkFor(subDirectory, subdirs)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFor(String name, File[] subdirs) {
        if (subdirs == null) return false;

        for (File subdir : subdirs) {
            if (subdir.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}