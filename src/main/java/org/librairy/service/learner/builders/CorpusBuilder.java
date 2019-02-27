package org.librairy.service.learner.builders;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.BuiltInLanguages;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.librairy.service.learner.model.Document;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.librairy.service.modeler.service.BoWService;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class CorpusBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusBuilder.class);

    LibrairyNlpClient librairyNlpClient;

    public static final String SEPARATOR = ";;";

    private static final String DEFAULT_LANG = "en";

    private BufferedWriter writer;
    private Path filePath;
    private Boolean isClosed = true;
    private AtomicInteger counter   = new AtomicInteger(0);
    private String updated = "";
    private String language = null;
    private AtomicInteger pendingDocs = new AtomicInteger();


    private final Escaper escaper = Escapers.builder()
            .addEscape('\'',"")
            .addEscape('\"',"")
            .addEscape('\n'," ")
            .addEscape('\r'," ")
            .addEscape('\t'," ")
            .build();

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;


    public CorpusBuilder(Path filePath, LibrairyNlpClient librairyNlpClient) throws IOException {

        this.filePath = filePath;

        this.librairyNlpClient = librairyNlpClient;

        //load all languages:
        LanguageProfileReader langReader = new LanguageProfileReader();

        List<LanguageProfile> languageProfiles = new ArrayList<>();

        Iterator it = BuiltInLanguages.getLanguages().iterator();

        List<String> availableLangs = Arrays.asList(new String[]{"en","es","fr","de","pt"});
        while(it.hasNext()) {
            LdLocale locale = (LdLocale)it.next();
            if (availableLangs.contains(locale.getLanguage())) {
                LOG.info("language added: " + locale);
                languageProfiles.add(langReader.readBuiltIn(locale));
            }
        }


        //build language detector:
        this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        //create a text object factory
        this.textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    }

    public Integer getNumDocs(){
        return counter.get();
    }

    public void add(Document document, Boolean multigrams, Boolean raw) throws IOException {
        if (Strings.isNullOrEmpty(document.getText()) || Strings.isNullOrEmpty(document.getText().replace("\n","").trim())) {
            LOG.warn("Document is empty: " + document.getId());
            return;
        }
        try{
            pendingDocs.incrementAndGet();
            StringBuilder row = new StringBuilder();
            row.append(document.getId()).append(SEPARATOR);
            row.append(escaper.escape(document.getId())).append(SEPARATOR);
            String labels = document.getLabels().stream().collect(Collectors.joining(" "));
            if (Strings.isNullOrEmpty(labels)) labels = "default";
            row.append(labels).append(SEPARATOR);
            updateLanguage(document.getText());
            // bow from nlp-service
            String text = raw? document.getText().replaceAll("\\P{Print}", "") : BoWService.toText(librairyNlpClient.bow(document.getText().replaceAll("\\P{Print}", ""), language, Arrays.asList(PoS.NOUN, PoS.VERB, PoS.ADJECTIVE), multigrams));
            row.append(text);
            updated = DateBuilder.now();
            write(row.toString());
            LOG.debug("Added document: [" + document.getId() +"] to corpus");
        }finally{
            pendingDocs.decrementAndGet();
        }
    }

    private synchronized void write(String text) {
        try {
            if (isClosed) {
                writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filePath.toFile(), true))));
                setClosed(false);
            }
            writer.write(text);
            writer.newLine();
            counter.incrementAndGet();
        } catch (IOException e) {
            LOG.warn("Error writing on file: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected Error writing on file: " + e.getMessage(), e);
        }
    }


    public synchronized boolean load(){

        if (!filePath.toFile().exists()) return false;
        LOG.info("Loading an existing corpus..");
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath.toFile()))));
            counter.set(Long.valueOf(reader.lines().count()).intValue());
            updated = DateBuilder.from(filePath.toFile().lastModified());
            return true;
        }catch (Exception e){
            LOG.debug("Error reading lines in existing file: " + filePath,e);
            return false;
        }finally{
            if (reader != null) try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String updateLanguage(String text){
        if (Strings.isNullOrEmpty(text)){
            LOG.warn("empty text! english by default");
            return DEFAULT_LANG;
        }
        if (Strings.isNullOrEmpty(language)){
            LOG.info("detecting language from text: " + text.substring(0, text.length()>50? 50 : text.length()));
            TextObject textObject = textObjectFactory.forText(text);
            Optional<LdLocale> lang = languageDetector.detect(textObject);
            if (!lang.isPresent()){
                LOG.warn("language not detected! english by default");
                lang = Optional.of(LdLocale.fromString(DEFAULT_LANG));
            }
            LOG.info("Language=" + lang.get());
            language = lang.get().getLanguage();
        }
        return language;
    }

    public void close() {
        while(pendingDocs.get() > 0){
            LOG.info("waiting for adding "+pendingDocs.get()+" pending docs to close it... ");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted",e);
            }
        }

        if (pendingDocs.get() >0) LOG.info("Pending docs: " + pendingDocs.get());

        setClosed(true);
        if (writer != null){
            try{
//                writer.flush();
                writer.close();
                LOG.info("Writer closed with " + counter.get() + " documents added");
            }catch (IOException e){
                LOG.error("Writer closing error",e);
            }
        }
        LOG.info("Corpus closed");
    }

    private synchronized void setClosed(Boolean status){
        this.isClosed = status;
    }

    public Path getFilePath() {
        return filePath;
    }
}
