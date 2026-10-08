/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2023.                            (c) 2023.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package org.opencadc.erfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author pdowler
 */
public class NativeUtil {
    private static final Logger log = Logger.getLogger(NativeUtil.class);
    private static final boolean IS_MAC_OS;
    private static final String OPERATING_SYSTEM;
    private static final String EXTENSION;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        IS_MAC_OS = osName.startsWith("mac os x");
        if (IS_MAC_OS) {
            OPERATING_SYSTEM = "osx";
            EXTENSION = ".dylib";
        } else if (osName.startsWith("linux")) {
            OPERATING_SYSTEM = "linux";
            EXTENSION = ".so";
        } else {
            OPERATING_SYSTEM = null;
            EXTENSION = null;
        }
    }

    private NativeUtil() {
    }

    static void loadJNI(ClassLoader cl, String name,
                        String linuxDependencyFileName, String macDependencyFileName)
        throws NativeInitializationException {
        if (OPERATING_SYSTEM == null) {
            throw new NativeInitializationException(
                "unsupported operating system: " + System.getProperty("os.name"));
        }

        String architecture = normalizeArchitecture(System.getProperty("os.arch"));
        String dependencyFileName = IS_MAC_OS ? macDependencyFileName : linuxDependencyFileName;
        String resourceDirectory = OPERATING_SYSTEM + "/" + architecture + "/";
        String jniFileName = name + EXTENSION;
        final UUID uuid = UUID.randomUUID();
        File parent = new File(System.getProperty("java.io.tmpdir"));
        File tmpdir = new File(parent, name + "-" + uuid);

        try {
            if (!tmpdir.mkdir()) {
                throw new NativeInitializationException(
                    "failed to create temporary directory: " + tmpdir.getAbsolutePath());
            }
            tmpdir.deleteOnExit();

            String dependencyResource = resourceDirectory + dependencyFileName;
            File dependency = extractResource(cl, dependencyResource, tmpdir, dependencyFileName);
            loadLibrary(dependency, dependencyResource);

            String jniResource = resourceDirectory + jniFileName;
            File jni = extractResource(cl, jniResource, tmpdir, jniFileName);
            loadLibrary(jni, jniResource);
        } catch (IOException ex) {
            log.error("failed to extract shared library", ex);
            throw new NativeInitializationException(
                "failed to extract shared library to: " + tmpdir.getAbsolutePath(), ex);
        }
    }

    private static File extractResource(ClassLoader cl, String resourceName, File tmpdir, String fileName)
        throws IOException, NativeInitializationException {
        URL url = cl.getResource(resourceName);
        if (url == null) {
            throw new NativeInitializationException("not found via ClassLoader: " + resourceName);
        }

        File tmp = new File(tmpdir, fileName);
        if (tmp.exists()) {
            throw new NativeInitializationException("found pre-existing " + tmp.getAbsolutePath());
        }

        log.debug("found: " + url);
        URLConnection uc = url.openConnection();
        uc.setUseCaches(false);
        try (InputStream istream = uc.getInputStream();
             FileOutputStream ostream = new FileOutputStream(tmp)) {
            byte[] buf = new byte[65536];
            int nb = istream.read(buf);
            while (nb != -1) {
                ostream.write(buf, 0, nb);
                nb = istream.read(buf);
            }
        }
        tmp.deleteOnExit();
        log.debug("extracted: " + url.toExternalForm() + " -> " + tmp.getAbsolutePath());
        return tmp;
    }

    private static void loadLibrary(File library, String resourceName) throws NativeInitializationException {
        try {
            System.load(library.getAbsolutePath());
            log.debug("loaded: " + library.getAbsolutePath());
        } catch (Error e) {
            log.error("failed to load shared library: " + library);
            throw new NativeInitializationException("failed to load shared lib: " + resourceName, e);
        }
    }

    static String normalizeArchitecture(String architecture) {
        String value = architecture.toLowerCase();
        if ("arm64".equals(value)) {
            return "aarch64";
        }
        if ("amd64".equals(value)) {
            return "x86_64";
        }
        return value;
    }
}
