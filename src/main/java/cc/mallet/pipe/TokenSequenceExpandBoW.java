package cc.mallet.pipe;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TokenSequenceExpandBoW extends Pipe implements Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger(TokenSequenceExpandBoW.class);

    String nameValueSeparator; // what separates the name from the value? (CAN'T BE WHITESPACE!)

    public TokenSequenceExpandBoW (String _nameValueSeparator) {
        nameValueSeparator = _nameValueSeparator;
    }

    public Instance pipe (Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData ();
        TokenSequence nts = new TokenSequence();
            int limit = ts.size();
            for (int i=0; i < limit; i++) {
                Token t = ts.get(i);
                try{
                    String[] values = t.getText().split(nameValueSeparator);
                    Integer times = Integer.valueOf(values[1]);
                    for (int j=0;j<times;j++){
                        nts.add(new Token(values[0]));
                    }
                }catch (Exception e){
                    LOG.error("Unexpected error trying to expand BoW from '" + t.getText()+"' : " +e.getMessage());
                }

            }

        carrier.setData (nts);
        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 1;

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.writeInt (CURRENT_SERIAL_VERSION);
        out.writeObject (nameValueSeparator);
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt ();
        nameValueSeparator = (String)in.readObject();
    }
}
