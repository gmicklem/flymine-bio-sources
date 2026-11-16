package org.intermine.bio.dataconversion;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.intermine.dataconversion.ItemsTestCase;
import org.intermine.dataconversion.MockItemWriter;
import org.intermine.metadata.Model;
import org.intermine.model.fulldata.Item;

public class FlybaseAllelesConverterTest extends ItemsTestCase
{
    Model model = Model.getInstanceByName("genomic");
    FlybaseAllelesConverter converter;
    MockItemWriter itemWriter;

    public FlybaseAllelesConverterTest(String arg) {
        super(arg);
    }

    public void testProcess() throws Exception {
        itemWriter = new MockItemWriter(new HashMap<String, Item>());
        converter = new FlybaseAllelesConverter(itemWriter, model);
        String input = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
                "disease_model_annotations_fb_2020_01.tsv"), "UTF-8");
        converter.process(new StringReader(input));
        converter.close();

        // uncomment to write out a new target items file
        //writeItemsFile(itemWriter.getItems(), "flybase-alleles-tgt-items.xml");

        Set<org.intermine.xml.full.Item> expected = readItemSet("FlyBaseAllelesConverterTest_tgt.xml");

        assertEquals(expected, itemWriter.getItems());
    }

    /**
     * Lines of fewer than 11 columns (the evidence is the 11th) are skipped, not read past their end.
     */
    public void testShortLinesAreSkipped() throws Exception {
        itemWriter = new MockItemWriter(new HashMap<String, Item>());
        converter = new FlybaseAllelesConverter(itemWriter, model);
        String line10 = "FBgn0000001\tUba5\tHGNC:23230\tmodel of\tDOID:0050950\tataxia\tFBal0257945\tUba5[HMS01352]\tHGNC:23230\tUBA5\n";
        String old7 = "FBal0257945\tUba5[HMS01352]\tmodel of\tataxia\tDOID:0050950\tinferred from mutant phenotype\tFBrf0230959\n";
        converter.process(new StringReader(line10 + old7));
        converter.close();

        for (org.intermine.xml.full.Item item : itemWriter.getItems()) {
            assertFalse(item.getClassName(), "Allele".equals(item.getClassName())
                    || "DOAnnotation".equals(item.getClassName())
                    || "DOEvidence".equals(item.getClassName()));
        }
    }
}
