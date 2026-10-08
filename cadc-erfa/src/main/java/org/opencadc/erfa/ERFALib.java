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
 *  : 5 $
 *
 ************************************************************************
 */

package org.opencadc.erfa;

import org.apache.log4j.Logger;

/** <p>ERFALib is a Java JNI wrapper around Time transformations in the
 * ERFA (Essential Routines for Fundamental Astronomy) C library. Specifically:
 * </p>
 * <code>
 * TIA to TT
 * TAI to UTC
 * TT to TAI
 * UTC to TAI
 * </code>
 * <p>where
 * </p>
 * <code>
 * TAI - International Atomic Time
 * TT - Terrestrial Time
 * UTC - Coordinated Universal Time
 * </code>
 */
public class ERFALib {
    private static final Logger log = Logger.getLogger(ERFALib.class);
    private static final String JNI_FILE = "liberfaLibJNI";
    private static final String LINUX_ERFA_LIBRARY_FILE = "liberfa.so.1.8.1";
    private static final String MAC_ERFA_LIBRARY_FILE = "liberfa.1.dylib";

    static {
        try {
            NativeUtil.loadJNI(
                ERFALib.class.getClassLoader(), JNI_FILE,
                LINUX_ERFA_LIBRARY_FILE, MAC_ERFA_LIBRARY_FILE);
        } catch (NativeInitializationException e) {
            log.info("Checking library file " + JNI_FILE + ": FAIL");
        }
    }

    public ERFALib() {
    }

    /**
     * <p>Convenience method to transform TT to UTC by calling TT to TAI and TAI to UTC.
     * </p>
     * <p>tt1+tt2 is a two part Julian Date, apportioned in any convenient
     * way between the two arguments, for example where tt1 is the Julian
     * Day Number and tt2 is the fraction of a day.  The returned
     * utc1+utc2 form an analogous pair, except that a special convention
     * is used, to deal with the problem of leap seconds.
     * </p>
     * <p>JD cannot unambiguously represent UTC during a leap second unless
     * special measures are taken.  The convention in the present
     * function is that the returned quasi-JD UTC1+UTC2 represents UTC
     * days whether the length is 86399, 86400 or 86401 SI seconds.
     * </p>
     * @param tt1 TT as a 2-part Julian Date.
     * @param tt2 TT as a 2-part Julian Date.
     * @return UTC as a 2-part quasi Julian Date.
     * @throws ERFALibException if an error occurs doing the transformation.
     * @throws DubiousYearException if a date predates the introduction of the UTC timescale
     *                              or are too far in the future to be trusted.
     * @throws UnacceptableDateException if a date is so early that a Julian Date
     *                                   could not be computed.
     */
    public static double[] tt2utc(double tt1, double tt2)
        throws ERFALibException, DubiousYearException, UnacceptableDateException {
        double[] tai = tt2tai(tt1, tt2);
        return tai2utc(tai[0], tai[1]);
    }

    /**
     * <p>Convenience method to transform UTC to TT by calling UTC to TAI and TAI to TT.
     * </p>
     * <p>utc1+utc2 is a 2-part Julian Date, apportioned in any convenient way
     * between the two arguments, for example where tai1 is the Julian
     * Day Number and tai2 is the fraction of a day.  The returned
     * tt1+tt2 follow suit.
     * </p>
     * <p>JD cannot unambiguously represent UTC during a leap second unless
     * special measures are taken.  The convention in the present
     * function is that the JD day represents UTC days whether the
     * length is 86399, 86400 or 86401 SI seconds.  In the 1960-1972 era
     * there were smaller jumps (in either direction) each time the
     * linear UTC(TAI) expression was changed, and these "mini-leaps"
     * are also included in the ERFA convention.
     * </p>
     * @param utc1 UTC as a 2-part quasi Julian Date.
     * @param utc2 UTC as a 2-part quasi Julian Date.
     * @return TT as a 2-part Julian Date.
     * @throws ERFALibException if an error occurs doing the transformation.
     * @throws DubiousYearException if a date predates the introduction of the UTC timescale
     *                              or are too far in the future to be trusted.
     * @throws UnacceptableDateException if a date is so early that a Julian Date
     *                                   could not be computed.
     */
    public static double[] utc2tt(double utc1, double utc2)
        throws DubiousYearException, UnacceptableDateException, ERFALibException {
        double[] tai = utc2tai(utc1, utc2);
        return tai2tt(tai[0], tai[1]);
    }

    /**
     * <p>Time scale transformation: International Atomic Time, TAI, to Terrestrial Time, TT.
     * </p>
     * <p>tai1+tai2 is a two part Julian Date, apportioned in any convenient
     * way between the two arguments, for example where tai1 is the Julian
     * Day Number and tai2 is the fraction of a day.  The returned tt1,tt2
     * follow suit.
     * </p>
     * @param tai1 TAI as a 2-part Julian Date
     * @param tai2 TAI as a 2-part Julian Date
     * @return TT as a 2-part Julian Date.
     * @throws ERFALibException if an error occurs doing the conversion.
     **/
    public static double[] tai2tt(double tai1, double tai2)
        throws ERFALibException {
        return eraTai2tt(tai1, tai2);
    }

    /**
     * <p>Time scale transformation: International Atomic Time, TAI, to Coordinated Universal Time, UTC.
     * </p>
     * <p>tai1+tai2 is a 2-part Julian Date, apportioned in any convenient way
     * between the two arguments, for example where tai1 is the Julian
     * Day Number and tai2 is the fraction of a day.  The returned
     * utc1+utc2 form an analogous pair, except that a special convention
     * is used, to deal with the problem of leap seconds.
     * </p>
     * <p>JD cannot unambiguously represent UTC during a leap second unless
     * special measures are taken.  The convention in the present
     * function is that the JD day represents UTC days whether the
     * length is 86399, 86400 or 86401 SI seconds.  In the 1960-1972 era
     * there were smaller jumps (in either direction) each time the
     * linear UTC(TAI) expression was changed, and these "mini-leaps"
     * are also included in the ERFA convention.
     * </p>
     * @param tai1 TAI as a 2-part Julian Date.
     * @param tai2 TAI as a 2-part Julian Date.
     * @return UTC as a 2-part quasi Julian Date.
     * @throws ERFALibException if an error occurs doing the transformation.
     * @throws DubiousYearException if a date predates the introduction of the UTC timescale
     *                              or are too far in the future to be trusted.
     * @throws UnacceptableDateException if a date is so early that a Julian Date
     *                                   could not be computed.
     **/
    public static double[] tai2utc(double tai1, double tai2)
        throws ERFALibException, DubiousYearException, UnacceptableDateException {
        return eraTai2utc(tai1, tai2);
    }

    /**
     * <p>Time scale transformation: Terrestrial Time, TT, to International Atomic Time, TAI.
     * </p>
     * <p>tt1+tt2 is a two part Julian Date, apportioned in any convenient
     * way between the two arguments, for example where tt1 is the Julian
     * Day Number and tt2 is the fraction of a day.  The returned tai1+tai2
     * follow suit.
     * </p>
     * @param tt1 TT as a 2-part Julian Date
     * @param tt2 TT as a 2-part Julian Date
     * @return TAI as a 2-part Julian Date.
     * @throws ERFALibException if an error occurs doing the conversion.
     **/
    public static double[] tt2tai(double tt1, double tt2)
        throws ERFALibException {
        return eraTt2tai(tt1, tt2);
    }

    /**
     * <p>Time scale transformation: Coordinated Universal Time, UTC, to International Atomic Time, TAI.
     * </p>
     * <p>utc1+utc2 is a 2-part Julian Date, apportioned in any convenient way
     * between the two arguments, for example where tai1 is the Julian
     * Day Number and tai2 is the fraction of a day.  The returned
     * tai1+tai2 follow suit.
     * </p>
     * <p>JD cannot unambiguously represent UTC during a leap second unless
     * special measures are taken.  The convention in the present
     * function is that the JD day represents UTC days whether the
     * length is 86399, 86400 or 86401 SI seconds.  In the 1960-1972 era
     * there were smaller jumps (in either direction) each time the
     * linear UTC(TAI) expression was changed, and these "mini-leaps"
     * are also included in the ERFA convention.
     * </p>
     * @param utc1 UTC as a 2-part quasi Julian Date.
     * @param utc2 UTC as a 2-part quasi Julian Date.
     * @return TAI as a 2-part Julian Date.
     * @throws ERFALibException if an error occurs doing the transformation.
     * @throws DubiousYearException if a date predates the introduction of the UTC timescale
     *                              or are too far in the future to be trusted.
     * @throws UnacceptableDateException if a date is so early that a Julian Date
     *                                   could not be computed.
     **/
    public static double[] utc2tai(double utc1, double utc2)
        throws ERFALibException, DubiousYearException, UnacceptableDateException {
        return eraUtc2tai(utc1, utc2);
    }


    /**
     * native methods
     */
    private static native double[] eraTai2tt(double tia1, double tai2)
        throws ERFALibException;

    private static native double[] eraTai2utc(double tai1, double tai2)
        throws ERFALibException, DubiousYearException, UnacceptableDateException;

    private static native double[] eraTt2tai(double tt1, double tt2)
        throws ERFALibException;

    private static native double[] eraUtc2tai(double utc1, double utc2)
        throws ERFALibException, DubiousYearException, UnacceptableDateException;

}
