package org.broadinstitute.sting.gatk.walkers.variantutils;

import org.broadinstitute.sting.WalkerTest;
import org.testng.annotations.Test;

import java.util.Arrays;

public class SelectVariantsIntegrationTest extends WalkerTest {
    public static String baseTestString(String args) {
        return "-T SelectVariants -R " + b36KGReference + " -L 1 -o %s --no_cmdline_in_header" + args;
    }

    @Test
    public void testComplexSelection() {
        String testfile = validationDataLocation + "test.filtered.maf_annotated.vcf";
        String samplesFile = validationDataLocation + "SelectVariants.samples.txt";

        WalkerTestSpec spec = new WalkerTestSpec(
            baseTestString(" -sn A -se '[CDH]' -sf " + samplesFile + " -env -ef -select 'DP < 250' --variant " + testfile),
            1,
            Arrays.asList("b3df647811b5f13aa8eac080705c1be9")
        );
        spec.disableShadowBCF();
        executeTest("testComplexSelection--" + testfile, spec);
    }

    @Test
    public void testSampleExclusion() {
        String testfile = validationDataLocation + "test.filtered.maf_annotated.vcf";
        String samplesFile = validationDataLocation + "SelectVariants.samples.txt";

        WalkerTestSpec spec = new WalkerTestSpec(
            "-T SelectVariants -R " + b36KGReference + " -L 1:1-1000000 -o %s --no_cmdline_in_header -xl_sn A -xl_sf " + samplesFile + " --variant " + testfile,
            1,
            Arrays.asList("64f0f3fc7297c21325f76222ccb48b1d")
        );
        spec.disableShadowBCF();

        executeTest("testSampleExclusion--" + testfile, spec);
    }

    @Test
    public void testRepeatedLineSelection() {
        String testfile = testDir + "test.dup.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                baseTestString(" -sn A -sn B -sn C --variant " + testfile),
                1,
                Arrays.asList("20936f028bd073224f0adcdb0b9a8dfe")
        );

        executeTest("testRepeatedLineSelection--" + testfile, spec);
    }

    @Test
    public void testDiscordance() {
        String testFile = testDir + "NA12878.hg19.example1.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + hg19Reference + " -sn NA12878 -L 20:1012700-1020000 --variant " + b37hapmapGenotypes + " -disc " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("03abdc27bfd7aa36d57bba0325b31e0d")
        );
        spec.disableShadowBCF();

        executeTest("testDiscordance--" + testFile, spec);
    }

    @Test
    public void testDiscordanceNoSampleSpecified() {
        String testFile = testDir + "NA12878.hg19.example1.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + hg19Reference + " -L 20:1012700-1020000 --variant " + b37hapmapGenotypes + " -disc " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("9fb54ed003234a5847c565ffb6767b95")
        );
        spec.disableShadowBCF();

        executeTest("testDiscordanceNoSampleSpecified--" + testFile, spec);
    }

    @Test
    public void testConcordance() {
        String testFile = testDir + "NA12878.hg19.example1.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + hg19Reference + " -sn NA12878 -L 20:1012700-1020000 -conc " + b37hapmapGenotypes + " --variant " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("76857b016198c3e08a2e27bbdb49f3f0")
        );
        spec.disableShadowBCF();

        executeTest("testConcordance--" + testFile, spec);
    }

    @Test
    public void testVariantTypeSelection() {
        String testFile = testDir + "complexExample1.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b36KGReference + " -restrictAllelesTo MULTIALLELIC -selectType MIXED --variant " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("6c0b0c5f03d26f4a7a1438a2afc9fb6b")
        );

        executeTest("testVariantTypeSelection--" + testFile, spec);
    }

    @Test
    public void testUsingDbsnpName() {
        String testFile = testDir + "combine.3.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b36KGReference + " -sn NA12892 --variant:dbsnp " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("a8a26c621018142c9cba1080cbe687a8")
        );

        executeTest("testUsingDbsnpName--" + testFile, spec);
    }

    @Test
    public void testRegenotype() {
        String testFile = testDir + "combine.3.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b36KGReference + " -regenotype -sn NA12892 --variant " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("6bee6dc2316aa539560a6d9d8adbc4ff")
        );

        executeTest("testRegenotype--" + testFile, spec);
    }

    @Test
    public void testMultipleRecordsAtOnePosition() {
        String testFile = testDir + "selectVariants.onePosition.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b36KGReference + " -select 'KG_FREQ < 0.5' --variant " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("6ff686a64e98fc1be2cde9b034d4a43a")
        );

        executeTest("testMultipleRecordsAtOnePositionFirstIsFiltered--" + testFile, spec);
    }

    @Test
    public void testNoGTs() {
        String testFile = testDir + "vcf4.1.example.vcf";

        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b37KGReference + " --variant " + testFile + " -o %s --no_cmdline_in_header",
                1,
                Arrays.asList("b96a09ae32cc3aa300adfba3b5ee71ce")
        );

        executeTest("testMultipleRecordsAtOnePositionFirstIsFiltered--" + testFile, spec);
    }

    @Test
    public void testParallelization2() {
        String testfile = validationDataLocation + "test.filtered.maf_annotated.vcf";
        String samplesFile = validationDataLocation + "SelectVariants.samples.txt";
        WalkerTestSpec spec;

        spec = new WalkerTestSpec(
            baseTestString(" -sn A -se '[CDH]' -sf " + samplesFile + " -env -ef -select 'DP < 250' --variant " + testfile + " -nt 2"),
            1,
            Arrays.asList("b3df647811b5f13aa8eac080705c1be9")
        );
        spec.disableShadowBCF();
        executeTest("testParallelization (2 threads)--" + testfile, spec);
    }

    @Test
    public void testParallelization4() {
            String testfile = validationDataLocation + "test.filtered.maf_annotated.vcf";
            String samplesFile = validationDataLocation + "SelectVariants.samples.txt";
            WalkerTestSpec spec;
            spec = new WalkerTestSpec(
            baseTestString(" -sn A -se '[CDH]' -sf " + samplesFile + " -env -ef -select 'DP < 250' --variant " + testfile + " -nt 4"),
            1,
            Arrays.asList("b3df647811b5f13aa8eac080705c1be9")
        );
        spec.disableShadowBCF();

        executeTest("testParallelization (4 threads)--" + testfile, spec);
    }

    @Test
    public void testSelectFromMultiAllelic() {
        String testfile = testDir + "multi-allelic.bi-allelicInGIH.vcf";
        String samplesFile = testDir + "GIH.samples.list";
        WalkerTestSpec spec = new WalkerTestSpec(
                "-T SelectVariants -R " + b37KGReference + " -o %s --no_cmdline_in_header -sf " + samplesFile + " --excludeNonVariants --variant " + testfile,
                1,
                Arrays.asList("828b4c1ef6ea7e57aad626264cc2c8f6")
        );
        executeTest("test select from multi allelic with excludeNonVariants --" + testfile, spec);
    }
}
