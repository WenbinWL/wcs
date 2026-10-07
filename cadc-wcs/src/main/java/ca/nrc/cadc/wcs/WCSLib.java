/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2024.                            (c) 2024.
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
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.wcs;

import ca.nrc.cadc.wcs.Transform.Result;
import ca.nrc.cadc.wcs.exceptions.WCSLibInitializationException;
import ca.nrc.cadc.wcs.exceptions.WCSLibRuntimeException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * This class contains the native method definitions to the WCSLIB 4.2 C routines
 * wcss2p() and wcsp2s(), and provides package level access to methods implementing
 * the native definitions.
 *
 * @author jburke
 */
final class WCSLib {
    private static final Logger LOGGER = Logger.getLogger(WCSLib.class);
    private static final String JNI_FILE = "libwcsLibJNI.8";
    private static final String LINUX_WCS_LIBRARY = "libwcs.8.4";
    private static final String LINUX_WCS_LIBRARY_FILE = "libwcs.so.8.4";
    private static final String MAC_WCS_LIBRARY = "libwcs.8.10";
    private static final String MAC_WCS_LIBRARY_FILE = "libwcs.8.dylib";

    static {
        LOGGER.info("Loaded WCSLib major version " + WCSLib.loadNativeLibrary());
    }

    /**
     * Constant used to represent a null or uninitialized value.
     * Taken the WCSLib wcsmath.h.
     * #define UNDEFINED 987654321.0e99
     */
    protected static double UNDEFINED = 987654321.0e99;

    /**
     * Load the WCSLib 8 JNI binding.
     *
     * @return The JNI file that was successfully loaded.
     * @throws WCSLibInitializationException If the binding cannot be loaded.
     */
    private static String loadNativeLibrary() throws WCSLibInitializationException {
        final ClassLoader classLoader = WCSLib.class.getClassLoader();
        try {
            LOGGER.info("Checking library file " + JNI_FILE);
            NativeUtil.loadJNI(
                classLoader, JNI_FILE,
                LINUX_WCS_LIBRARY, LINUX_WCS_LIBRARY_FILE,
                MAC_WCS_LIBRARY, MAC_WCS_LIBRARY_FILE);
            LOGGER.info("Checking library file " + JNI_FILE + ": OK");
            return JNI_FILE;
        } catch (NativeInitializationException ex) {
            LOGGER.info("Checking library file " + JNI_FILE + ": FAIL");
            throw new WCSLibInitializationException("failed to load " + JNI_FILE, -1, ex);
        }
    }

    /**
     * Status return values from the native code.
     *
     * <p>The first 14 status errors correspond to WCSLIB C status return values.
     * The remaining status errors are returned by the wrapper C code.
     */
    private static final Map<Integer, String> ERROR_MAP = new TreeMap<>();

    static {
        // Errors from wcslib C library.
        ERROR_MAP.put(0, "Success");
        ERROR_MAP.put(1, "Null wcsprm pointer passed");
        ERROR_MAP.put(2, "Memory allocation failed");
        ERROR_MAP.put(3, "Linear transformation matrix is singular");
        ERROR_MAP.put(4, "Inconsistent or unrecognized coordinate axis types");
        ERROR_MAP.put(5, "Invalid parameter value");
        ERROR_MAP.put(6, "Invalid coordinate transformation parameters");
        ERROR_MAP.put(7, "Ill-conditioned coordinate transformation parameters");
        ERROR_MAP.put(8, "One or more of the pixel coordinates were invalid");
        ERROR_MAP.put(9, "One or more of the world coordinates were invalid");
        ERROR_MAP.put(10, "Invalid world coordinate");
        ERROR_MAP.put(11, "No solution found in the specified interval");
        ERROR_MAP.put(12, "Invalid subimage specification (no spectral axis).");
        ERROR_MAP.put(13, "Non-separable subimage coordinate system");

        // Errors from C wrapper class.
        ERROR_MAP.put(100, "CRPIX memory allocation failed.");
        ERROR_MAP.put(101, "PC memory allocation failed.");
        ERROR_MAP.put(102, "CDELT memory allocation failed.");
        ERROR_MAP.put(103, "CRVAL memory allocation failed.");
        ERROR_MAP.put(104, "CUNIT memory allocation failed.");
        ERROR_MAP.put(105, "CUNIT array index out of bounds.");
        ERROR_MAP.put(106, "CTYPE memory allocation failed.");
        ERROR_MAP.put(107, "CTYPE array index out of bounds.");
        ERROR_MAP.put(108, "LONPOLE memory allocation failed.");
        ERROR_MAP.put(109, "LATPOLE memory allocation failed.");
        ERROR_MAP.put(110, "RESTFRQ memory allocation failed.");
        ERROR_MAP.put(111, "RESTWAV memory allocation failed.");
        ERROR_MAP.put(112, "PS value memory allocation failed.");
        ERROR_MAP.put(113, "PS value array index out of bounds.");
        ERROR_MAP.put(114, "CD memory allocation failed.");
        ERROR_MAP.put(115, "CROTA memory allocation failed.");
        ERROR_MAP.put(116, "Pixel coordinates array index out of bounds.");
        ERROR_MAP.put(117, "World coordinates array index out of bounds.");
        ERROR_MAP.put(118, "Result array index out of bounds.");
    }

    private WCSLib() {
    }

    /**
     * Transforms pixel coordinates to world coordinates using the
     * native WCSLIB wcsp2s() C method.
     *
     * @param naxis NAXIS - Number of axes in WCS description.
     * @param crpix CRPIXi - Pixel coordinate of the reference point.
     * @param pc PCi_j - Pixel coordinate transformation matrix.
     * @param cdelt CDELTi - Coordinate increment.
     * @param crval CRVALi - Coordinate value at reference point.
     * @param cunit CUNITi - Units of CRVAL and CDELT.
     * @param ctype CTYPEi - Axis type.
     * @param lonpole LONPOLE - Native longitude of the celestial pole.
     * @param latpole LATPOLE - Native latitude of the celestial pole.
     * @param restfrq RESTFRQ - Line rest frequency (Hz).
     * @param restwav RESTWAV - Line rest wavelength in vacuum (m).
     * @param pvi PVi_ma - Axis number i (1-relative).
     * @param pvm PVi_ma - Parameter number m (0-relative).
     * @param pvv PVi_ma - Parameter value.
     * @param psi PSi_ma - Axis number i (1-relative).
     * @param psm PSi_ma - Parameter number m (0-relative).
     * @param psv PSi_ma - Parameter value.
     * @param cd CDi_j - Spectrum coordinate matrix.
     * @param crota CROTAi - Coordinate rotation.
     * @param pixcrd Pixel coordinates.
     * @return World coordinates.
     * @throws WCSLibInitializationException if initialization of the WCSLIB C wcsprm struct fails.
     * @throws WCSLibRuntimeException if WCSLIB function execution fails.
     */
    protected static Result pix2sky(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                    String[] cunit, String[] ctype, double[] lonpole, double[] latpole,
                                    double[] restfrq, double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi,
                                    int[] psm, String[] psv, double[] cd, double[] crota, double[] pixcrd) {
        // Bug fix for wcslib 6 only to check for invalid spectral CUNIT's
        // which cause a segmentation fault.
        // JB 2021.07.09
        SpectralUtil.isValidCunit(ctype, cunit);

        double[] world = new double[pixcrd.length];
        String[] worldUnits = new String[pixcrd.length];
        int status = wcsp2s(naxis, crpix, pc, cdelt, crval, cunit, ctype, lonpole, latpole, restfrq, restwav, pvi, pvm,
                            pvv, psi, psm, psv, cd, crota, pixcrd, world, worldUnits);

        if (status == 0) {
            return new Result(world, worldUnits);
        } else {
            String message = ERROR_MAP.get(status);
            if (message == null) {
                message = "BUG: unknown status value returned by wcsLibJNI";
            }
            throw new WCSLibRuntimeException(message, status);
        }
    }

    /**
     * Transforms world coordinates to pixel coordinates using the
     * native WCSLIB wcss2p() C method.
     *
     * @param naxis NAXIS - Number of axes in WCS description.
     * @param crpix CRPIXi - Pixel coordinate of the reference point.
     * @param pc PCi_j - Pixel coordinate transformation matrix.
     * @param cdelt CDELTi - Coordinate increment.
     * @param crval CRVALi - Coordinate value at reference point.
     * @param cunit CUNITi - Units of CRVAL and CDELT.
     * @param ctype CTYPEi - Axis type.
     * @param lonpole LONPOLE - Native longitude of the celestial pole.
     * @param latpole LATPOLE - Native latitude of the celestial pole.
     * @param restfrq RESTFRQ - Line rest frequency (Hz).
     * @param restwav RESTWAV - Line rest wavelength in vacuum (m).
     * @param pvi PVi_ma - Axis number i (1-relative).
     * @param pvm PVi_ma - Parameter number m (0-relative).
     * @param pvv PVi_ma - Parameter value.
     * @param psi PSi_ma - Axis number i (1-relative).
     * @param psm PSi_ma - Parameter number m (0-relative).
     * @param psv PSi_ma - Parameter value.
     * @param cd CDi_j - Spectrum coordinate matrix.
     * @param crota CROTAi - Coordinate rotation.
     * @param world World coordinates.
     * @return Pixel coordinates.
     * @throws WCSLibInitializationException if initialization of the WCSLIB C wcsprm struct fails.
     * @throws WCSLibRuntimeException if WCSLIB method execution fails.
     */
    protected static Result sky2pix(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                    String[] cunit, String[] ctype, double[] lonpole, double[] latpole,
                                    double[] restfrq, double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi,
                                    int[] psm, String[] psv, double[] cd, double[] crota, double[] world) {
        // Bug fix for wcslib 6 only to check for invalid spectral CUNIT's
        // which cause a segmentation fault.
        // JB 2021.07.09
        SpectralUtil.isValidCunit(ctype, cunit);

        double[] pixcrd = new double[world.length];
        String[] pixcrdUnits = new String[pixcrd.length];
        int status = wcss2p(naxis, crpix, pc, cdelt, crval, cunit, ctype, lonpole, latpole, restfrq, restwav, pvi, pvm,
                            pvv, psi, psm, psv, cd, crota, world, pixcrd, pixcrdUnits);

        if (status == 0) {
            return new Result(pixcrd, pixcrdUnits);
        } else {
            String message = ERROR_MAP.get(status);
            if (message == null) {
                message = "BUG: unknown status value returned by wcsLibJNI";
            }
            throw new WCSLibRuntimeException(message, status);
        }
    }

    /**
     * Transforms world coordinates to pixel coordinates using the
     * native WCSLIB wcss2p() C method.
     *
     * @param naxis NAXIS - Number of axes in WCS description.
     * @param crpix CRPIXi - Pixel coordinate of the reference point.
     * @param pc PCi_j - Pixel coordinate transformation matrix.
     * @param cdelt CDELTi - Coordinate increment.
     * @param crval CRVALi - Coordinate value at reference point.
     * @param cunit CUNITi - Units of CRVAL and CDELT.
     * @param ctype CTYPEi - Axis type.
     * @param lonpole LONPOLE - Native longitude of the celestial pole.
     * @param latpole LATPOLE - Native latitude of the celestial pole.
     * @param restfrq RESTFRQ - Line rest frequency (Hz).
     * @param restwav RESTWAV - Line rest wavelength in vacuum (m).
     * @param pvi PVi_ma - Axis number i (1-relative).
     * @param pvm PVi_ma - Parameter number m (0-relative).
     * @param pvv PVi_ma - Parameter value.
     * @param psi PSi_ma - Axis number i (1-relative).
     * @param psm PSi_ma - Parameter number m (0-relative).
     * @param psv PSi_ma - Parameter value.
     * @param cd CDi_j - Spectrum coordinate matrix.
     * @param crota CROTAi - Coordinate rotation.
     * @param spectralAxis Spectral axis (set to -1 to allow wcslib to locate).
     * @param spectralCtype Desired spectral ctype.
     * @return Pixel coordinates.
     * @throws WCSLibInitializationException if initialization of the WCSLIB C wcsprm struct fails.
     * @throws WCSLibRuntimeException if WCSLIB method execution fails.
     */
    protected static int translate(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                   String[] cunit, String[] ctype, double[] lonpole, double[] latpole, double[] restfrq,
                                   double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi, int[] psm,
                                   String[] psv, double[] cd, double[] crota, int spectralAxis,
                                   String spectralCtype) {
        // Bug fix for wcslib 6 only to check for invalid spectral CUNIT's
        // which cause a segmentation fault.
        // JB 2021.07.09
        SpectralUtil.isValidCunit(ctype, cunit);

        int status = wcssptr(naxis, crpix, pc, cdelt, crval, cunit, ctype, lonpole, latpole, restfrq, restwav, pvi, pvm,
                             pvv, psi, psm, psv, cd, crota, spectralAxis, spectralCtype);

        if (status == 0) {
            return status;
        } else {
            String message = ERROR_MAP.get(status);
            if (message == null) {
                message = "BUG: unknown status value returned by wcsLibJNI";
            }
            throw new WCSLibRuntimeException(message, status);
        }
    }

    /**
     * Native method to transforms pixel coordinates to world coordinates
     * using the WCSLIB 4.2 wcsp2s() C method.
     *
     * @return native method status value, 0 indicates success, other values
     *      indicate a problem during method exection. The STATUS_ERRORS
     *      array maps the status value to an error message.
     */
    private static native int wcsp2s(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                     String[] cunit, String[] ctype, double[] lonpole, double[] latpole,
                                     double[] restfrq, double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi,
                                     int[] psm, String[] psv, double[] cd, double[] crota, double[] pixcrd,
                                     double[] world, String[] worldUnits);

    /**
     * Native method to transforms world coordinates to pixel coordinates
     * using the WCSLIB 4.2 wcss2p() C method.
     *
     * @return native method status value, 0 indicates success, other values
     *      indicate a problem during method exection. The STATUS_ERRORS
     *      array maps the status value to an error message.
     */
    private static native int wcss2p(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                     String[] cunit, String[] ctype, double[] lonpole, double[] latpole,
                                     double[] restfrq, double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi,
                                     int[] psm, String[] psv, double[] cd, double[] crota, double[] world,
                                     double[] pixcrd, String[] pixcrdUnits);

    /**
     * Native method to translate the spectral axis in a wcsprm struct
     * using the WCSLIB 4.2 wcssptr() C method.
     *
     * @return native method status value, 0 indicates success, other values
     *      indicate a problem during method exection. The STATUS_ERRORS
     *      array maps the status value to an error message.
     */
    private static native int wcssptr(int naxis, double[] crpix, double[] pc, double[] cdelt, double[] crval,
                                      String[] cunit, String[] ctype, double[] lonpole, double[] latpole,
                                      double[] restfrq, double[] restwav, int[] pvi, int[] pvm, double[] pvv, int[] psi,
                                      int[] psm, String[] psv, double[] cd, double[] crota, int spectralAxis,
                                      String spectralType);

}
