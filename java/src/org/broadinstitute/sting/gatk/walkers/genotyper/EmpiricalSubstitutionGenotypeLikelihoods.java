package org.broadinstitute.sting.gatk.walkers.genotyper;

import org.broadinstitute.sting.utils.BaseUtils;

import static java.lang.Math.log10;
import static java.lang.Math.pow;
import java.util.TreeMap;
import java.util.EnumMap;

import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMReadGroupRecord;

/**
 * Created by IntelliJ IDEA.
 * User: mdepristo
 * Date: Aug 23, 2009
 * Time: 9:47:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmpiricalSubstitutionGenotypeLikelihoods extends NewHotnessGenotypeLikelihoods {
    // --------------------------------------------------------------------------------------------------------------
    //
    // Static methods to manipulate machine platforms
    //
    // --------------------------------------------------------------------------------------------------------------
    public enum SequencerPlatform {
        SOLEXA,         // Solexa / Illumina
        ROCHE454,       // 454
        SOLID,          // SOLiD
        UNKNOWN         // No idea -- defaulting to 1/3
    }

    private final static String SAM_PLATFORM_TAG = "PL";

    private static TreeMap<String, SequencerPlatform> PLFieldToSequencerPlatform = new TreeMap<String, SequencerPlatform>();
    private static void bind(String s, SequencerPlatform x) {
        PLFieldToSequencerPlatform.put(s, x);
        PLFieldToSequencerPlatform.put(s.toUpperCase(), x);
        PLFieldToSequencerPlatform.put(s.toLowerCase(), x);
    }

    //
    // Static list of platforms supported by this system.
    //
    static {
        bind("LS454", SequencerPlatform.ROCHE454);
        bind("454", SequencerPlatform.ROCHE454);
        bind("ILLUMINA", SequencerPlatform.SOLEXA);
        bind("solid", SequencerPlatform.SOLID);
    }

    public static SequencerPlatform standardizeSequencerPlatform( final String sequencerString ) {
        if ( sequencerString != null && PLFieldToSequencerPlatform.containsKey(sequencerString) ) {
            return PLFieldToSequencerPlatform.get(sequencerString);
        } else {
            return SequencerPlatform.UNKNOWN;
        }
    }

    public static SequencerPlatform getReadSequencerPlatform( SAMRecord read ) {
        final String readGroupString = ((String)read.getAttribute("RG"));
        SAMReadGroupRecord readGroup = readGroupString == null ? null : read.getHeader().getReadGroup(readGroupString);
        final String platformName = readGroup == null ? null : (String)readGroup.getAttribute(SAM_PLATFORM_TAG);
        return standardizeSequencerPlatform(platformName);
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // Static methods to get at the transition tables themselves
    //
    // --------------------------------------------------------------------------------------------------------------

    /**
     * A matrix of value i x j -> log10(p) where
     *
     *  i      - char of the miscalled base (i.e., A)
     *  j      - char of the presumed true base (i.e., C)
     *  log10p - empirical probability p that A is actually C
     *
     * The table is available for each technology
     */
    private final static EnumMap<SequencerPlatform, double[][]> log10pTrueGivenMiscall = new EnumMap<SequencerPlatform, double[][]>(SequencerPlatform.class);

    private static void addMisCall(final SequencerPlatform pl, char miscalledBase, char trueBase, double p) {
        if ( ! log10pTrueGivenMiscall.containsKey(pl) )
            log10pTrueGivenMiscall.put(pl, new double[4][4]);

        double[][] misCallProbs = log10pTrueGivenMiscall.get(pl);
        int i = BaseUtils.simpleBaseToBaseIndex(miscalledBase);
        int j = BaseUtils.simpleBaseToBaseIndex(trueBase);
        misCallProbs[i][j] = log10(p);
    }

    private static double getProbMiscallIsBase(SequencerPlatform pl, char miscalledBase, char trueBase) {
        int i = BaseUtils.simpleBaseToBaseIndex(miscalledBase);
        int j = BaseUtils.simpleBaseToBaseIndex(trueBase);

        double logP = log10pTrueGivenMiscall.get(pl)[i][j];
        if ( logP == 0.0 )
            throw new RuntimeException(String.format("Bad miscall base request miscalled=%c true=%b", miscalledBase, trueBase));
        else
            return logP;
    }

    private static void addSolexa() {
        SequencerPlatform pl = SequencerPlatform.SOLEXA;
        addMisCall(pl, 'A', 'C', 57.7/100.0);
        addMisCall(pl, 'A', 'G', 17.1/100.0);
        addMisCall(pl, 'A', 'T', 25.2/100.0);

        addMisCall(pl, 'C', 'A', 34.9/100.0);
        addMisCall(pl, 'C', 'G', 11.3/100.0);
        addMisCall(pl, 'C', 'T', 53.9/100.0);

        addMisCall(pl, 'G', 'A', 31.9/100.0);
        addMisCall(pl, 'G', 'C',  5.1/100.0);
        addMisCall(pl, 'G', 'T', 63.0/100.0);

        addMisCall(pl, 'T', 'A', 45.8/100.0);
        addMisCall(pl, 'T', 'C', 22.1/100.0);
        addMisCall(pl, 'T', 'G', 32.0/100.0);
    }
//
//    // TODO -- delete me for testing only
//    private static void addSolexa() {
//        SequencerPlatform pl = SequencerPlatform.SOLEXA;
//        addMisCall(pl, 'A', 'C', 59.2/100.0);
//        addMisCall(pl, 'A', 'G', 15.3/100.0);
//        addMisCall(pl, 'A', 'T', 25.6/100.0);
//
//        addMisCall(pl, 'C', 'A', 54.2/100.0);
//        addMisCall(pl, 'C', 'G', 10.3/100.0);
//        addMisCall(pl, 'C', 'T', 35.5/100.0);
//
//        addMisCall(pl, 'G', 'A', 26.4/100.0);
//        addMisCall(pl, 'G', 'C',  5.6/100.0);
//        addMisCall(pl, 'G', 'T', 68.0/100.0);
//
//        addMisCall(pl, 'T', 'A', 41.8/100.0);
//        addMisCall(pl, 'T', 'C', 17.3/100.0);
//        addMisCall(pl, 'T', 'G', 40.9/100.0);
//
//    }

    private static void addSOLiD() {
        SequencerPlatform pl = SequencerPlatform.SOLID;
        addMisCall(pl, 'A', 'C', 18.7/100.0);
        addMisCall(pl, 'A', 'G', 42.5/100.0);
        addMisCall(pl, 'A', 'T', 38.7/100.0);

        addMisCall(pl, 'C', 'A', 27.0/100.0);
        addMisCall(pl, 'C', 'G', 18.9/100.0);
        addMisCall(pl, 'C', 'T', 54.1/100.0);

        addMisCall(pl, 'G', 'A', 61.0/100.0);
        addMisCall(pl, 'G', 'C', 15.7/100.0);
        addMisCall(pl, 'G', 'T', 23.2/100.0);

        addMisCall(pl, 'T', 'A', 40.5/100.0);
        addMisCall(pl, 'T', 'C', 34.3/100.0);
        addMisCall(pl, 'T', 'G', 25.2/100.0);
    }

    private static void add454() {
        SequencerPlatform pl = SequencerPlatform.ROCHE454;
        addMisCall(pl, 'A', 'C', 23.2/100.0);
        addMisCall(pl, 'A', 'G', 42.6/100.0);
        addMisCall(pl, 'A', 'T', 34.3/100.0);

        addMisCall(pl, 'C', 'A', 19.7/100.0);
        addMisCall(pl, 'C', 'G',  8.4/100.0);
        addMisCall(pl, 'C', 'T', 71.9/100.0);

        addMisCall(pl, 'G', 'A', 71.5/100.0);
        addMisCall(pl, 'G', 'C',  6.6/100.0);
        addMisCall(pl, 'G', 'T', 21.9/100.0);

        addMisCall(pl, 'T', 'A', 43.8/100.0);
        addMisCall(pl, 'T', 'C', 37.8/100.0);
        addMisCall(pl, 'T', 'G', 18.5/100.0);
    }

    private static void addUnknown() {
        SequencerPlatform pl = SequencerPlatform.UNKNOWN;
        for ( char b1 : BaseUtils.BASES ) {
            for ( char b2 : BaseUtils.BASES ) {
                if ( b1 != b2 )
                    addMisCall(pl, b1, b2, 1.0/3.0);
            }

        }
    }

    static {
        addSolexa();
        add454();
        addSOLiD();
        addUnknown();
    }


    // --------------------------------------------------------------------------------------------------------------
    //
    // The actual objects themselves
    //
    // --------------------------------------------------------------------------------------------------------------
    private boolean raiseErrorOnUnknownPlatform = true;
    private SequencerPlatform defaultPlatform = SequencerPlatform.UNKNOWN;

    //
    // forwarding constructors -- don't do anything at all
    //
    public EmpiricalSubstitutionGenotypeLikelihoods() { super(); }

    public EmpiricalSubstitutionGenotypeLikelihoods(DiploidGenotypePriors priors) { super(priors); }

    public EmpiricalSubstitutionGenotypeLikelihoods(DiploidGenotypePriors priors, boolean raiseErrorOnUnknownPlatform) {
        super(priors);
        this.raiseErrorOnUnknownPlatform = raiseErrorOnUnknownPlatform;
    }

    public EmpiricalSubstitutionGenotypeLikelihoods(DiploidGenotypePriors priors, SequencerPlatform assumeUnknownPlatformsAreThis) {
        super(priors);

        if ( assumeUnknownPlatformsAreThis != null ) {
            raiseErrorOnUnknownPlatform = false;
            defaultPlatform = assumeUnknownPlatformsAreThis;
        }
    }

    /**
     * Cloning of the object
     * @return
     * @throws CloneNotSupportedException
     */
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected double log10PofTrueBaseGivenMiscall(char observedBase, char chromBase, SAMRecord read, int offset) {
        boolean fwdStrand = ! read.getReadNegativeStrandFlag();
        SequencerPlatform pl = getReadSequencerPlatform(read);

        if ( pl == SequencerPlatform.UNKNOWN ) {
            if ( raiseErrorOnUnknownPlatform )
                throw new RuntimeException("Unknown Sequencer platform for read " + read.format());
            else {
                pl = defaultPlatform;
            }
        }

        //System.out.printf("%s for %s%n", pl, read);

        double log10p = 0.0;
        if ( fwdStrand ) {
            log10p = getProbMiscallIsBase(pl, observedBase, chromBase);
        } else {
            log10p = getProbMiscallIsBase(pl, BaseUtils.simpleComplement(observedBase), BaseUtils.simpleComplement(chromBase));
        }

        //System.out.printf("p = %f for %s %c %c fwd=%b %d at %s%n", pow(10,log10p), pl, observedBase, chromBase, fwdStrand, offset, read.getReadName() );

        return log10p;
    }
}
