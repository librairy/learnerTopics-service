package org.librairy.service.learner.builders;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.base.Strings;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Lemmatizer extends Pipe implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Lemmatizer.class);
    private final List<PoS> pos;


    static{
        Unirest.setDefaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        Unirest.setDefaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
//        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private final LibrairyNlpClient client;
    private final String language;


    public Lemmatizer(LibrairyNlpClient client, String language, List<PoS> pos){

        this.pos        = pos;
        this.client     = client;
        this.language   = language;

    }

    public Instance pipe (Instance carrier)
    {
        String text = (String) carrier.getData();

        if (Strings.isNullOrEmpty(text)) return carrier;

        String description = text.length() > 25? new String(text.substring(0,25)) : text;

        LOG.debug("retrieving lemmas for text: '" + description + "' ..");

        String tokens = client.lemmatize(carrier.getData().toString(), language, this.pos);
        carrier.setData(tokens);

        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt ();
    }


}
