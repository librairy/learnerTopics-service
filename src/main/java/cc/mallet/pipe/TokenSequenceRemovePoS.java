package cc.mallet.pipe;

import cc.mallet.types.FeatureSequenceWithBigrams;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import org.apache.commons.lang.StringUtils;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TokenSequenceRemovePoS extends Pipe implements Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger(TokenSequenceRemovePoS.class);

    boolean caseSensitive   = true;
    boolean markDeletions   = false;
    List<PoS> posList = new ArrayList();


    public TokenSequenceRemovePoS (List<PoS> posList,  boolean caseSensitive, boolean markDeletions)
    {
        this.posList = posList;
        this.caseSensitive = caseSensitive;
        this.markDeletions = markDeletions;
    }

    public TokenSequenceRemovePoS (List<PoS> posList, boolean caseSensitive)
    {
        this.posList = posList;
        this.caseSensitive = caseSensitive;
    }

    public TokenSequenceRemovePoS (List<PoS> posList)
    {
        this (posList, false);
    }


    public TokenSequenceRemovePoS setCaseSensitive (boolean flag)
    {
        this.caseSensitive = flag;
        return this;
    }

    public TokenSequenceRemovePoS setMarkDeletions (boolean flag)
    {
        this.markDeletions = flag;
        return this;
    }

    public Instance pipe (Instance carrier)
    {
        TokenSequence ts = (TokenSequence) carrier.getData();
        // xxx This doesn't seem so efficient.  Perhaps have TokenSequence
        // use a LinkedList, and remove Tokens from it? -?
        // But a LinkedList implementation of TokenSequence would be quite inefficient -AKM
        TokenSequence ret = new TokenSequence ();
        Token prevToken = null;
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String pos = StringUtils.substringBetween(t.getText(), "#", "#");
            try{
                if (posList.isEmpty() || posList.contains(PoS.valueOf(pos.toUpperCase()))){
                    t.setText(StringUtils.substringBefore(t.getText(),"#"));
                    ret.add(t);
                    prevToken = t;
                } else if (markDeletions && prevToken != null)
                    prevToken.setProperty (FeatureSequenceWithBigrams.deletionMark, StringUtils.substringBefore(t.getText(),"#"));
            }catch (Exception e){
                LOG.error("Unexpected error trying to remove tokens from '" + t.getText() + "' : " + e.getMessage());
            }
        }
        carrier.setData(ret);
        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 2;

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.writeInt (CURRENT_SERIAL_VERSION);
        out.writeBoolean(caseSensitive);
        out.writeBoolean(markDeletions);
        out.writeObject(posList); // New as of CURRENT_SERIAL_VERSION 2
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt ();
        caseSensitive = in.readBoolean();
        if (version > 0)
            markDeletions = in.readBoolean();
        if (version > 1) {
            posList = (List<PoS>) in.readObject();
        }

    }


}
