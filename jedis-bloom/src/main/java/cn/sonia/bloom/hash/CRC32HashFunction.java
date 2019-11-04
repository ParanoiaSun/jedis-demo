package cn.sonia.bloom.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Create By Sonia_Sun on 2019-11-04
 */
public class CRC32HashFunction extends HashFunction {

    private static final long serialVersionUID = 3175307974705695650L;

    @Override
    public List<Integer> hash(byte[] value, int m, int k) {
        Checksum cs = new CRC32();
        List<Integer> positionList = new ArrayList<Integer>(k);
        int hashes = 0;
        int salt = 0;
        while (hashes < k) {
            cs.reset();
            cs.update(value, 0, value.length);
            // Modify the data to be checksummed by adding the number of already
            // calculated hashes, the loop counter and
            // a static seed
            cs.update(hashes + salt++ + seed32);
            int hash = rejectionSample((int) cs.getValue(), m);
            if (hash != -1) {
                positionList.add(hash);
                hashes++;
            }
        }
        return positionList;
    }

}
