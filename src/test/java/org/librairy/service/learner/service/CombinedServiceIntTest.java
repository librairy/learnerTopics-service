package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.Application;
import org.librairy.service.learner.facade.model.Document;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.modeler.facade.model.ModelerService;
import org.librairy.service.modeler.facade.model.TopicSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class CombinedServiceIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(CombinedServiceIntTest.class);

    @Autowired
    ModelerService modelerService;

    @Autowired
    LearnerService learnerService;

    @Autowired
    ExportService exportService;



    Map<String,String> texts = new HashMap<>();

    @Before
    public void setup() throws AvroRemoteException {
        texts.put("3 of Hearts is the self-titled debut studio album by the American group 3 of Hearts, released on March 6, 2001, through the record label RCA Nashville. It is a teen pop and country music album, though according to some music critics, it leans more towards pop music. The album was managed by American producer Byron Gallimore; its marketing focused on the group's crossover appeal to teenagers and young adults. 3 of Hearts performed on a national tour sponsored by Seventeen magazine and another sponsored by Walmart, and the singers were featured in several marketing campaigns. Reviews of the album were mixed; some critics praised the group's vocals and public image, but others criticized the songs as generic and lacking an authentic country sound.","News");
        texts.put("The Shape of Water is a 2017 American fantasy drama film directed by Guillermo del Toro and written by del Toro and Vanessa Taylor.[3][4] It stars Sally Hawkins, Michael Shannon, Richard Jenkins, Doug Jones, Michael Stuhlbarg, and Octavia Spencer. Set in Baltimore in 1962, the plot follows a mute custodian at a high-security government laboratory who falls in love with a captured human-amphibian creature.","Film");
        texts.put("Guillermo del Toro Gómez is a Mexican film director, screenwriter, producer, and novelist. In his filmmaking career, del Toro has alternated between Spanish-language dark fantasy pieces, such as the gothic horror films The Devil's Backbone (2001) and Pan's Labyrinth (2006), and more mainstream American action films, such as the vampire superhero action film Blade II (2002), the supernatural superhero film Hellboy (2004), its sequel Hellboy II: The Golden Army (2008), Trollhunters (2016) and the science fiction monster film Pacific Rim (2013). His 2017 fantasy film The Shape of Water received critical acclaim and won a Golden Lion at the 74th Venice International Film Festival as well as the Academy Award for Best Picture. Del Toro received an Academy Award for Best Director for the film, as well as the Golden Globe, BAFTA, Critics' Choice, and Directors Guild of America.","Film");
        texts.put("Blade II is a 2002 American superhero film based on the fictional character of the same name from Marvel Comics. It is the sequel of the first film and the second part of the Blade film series, followed by Blade: Trinity. It was written by David S. Goyer, who also wrote the previous film, directed by Guillermo del Toro, and had Wesley Snipes returning as the lead character and producer.","Film");
        texts.put("Blade: Trinity (also known as Blade III or Blade III: Trinity) is a 2004 American superhero film written, produced and directed by David S. Goyer, who also wrote the screenplays to Blade and Blade II. It stars Wesley Snipes, who also produced, in the title role based on the Marvel Comics character Blade alongside Ryan Reynolds, Jessica Biel, Kris Kristofferson, Dominic Purcell, Parker Posey and Triple H.","Film");
        texts.put("A binary search algorithm is a method to determine the position of a target value within a sorted array (an ordered list). Binary search compares the target value to the middle element of the array. If they are not equal, the half in which the target cannot lie is eliminated and the search continues on the remaining half, again taking the middle element to compare to the target value, and so on.","News");
        texts.put("The 2012 Miller Superbike World Championship round was the sixth round of the 2012 Superbike World Championship season. It took place on the weekend of May 26–28, 2012 at Miller Motorsports Park, in Tooele, Utah, United States. The races were held on Memorial Day Monday.","News");
        texts.put("Though the magazine's focus for the first five years or so of its existence was experimental/underground music, its focus at the turn of the century broadened to include an emphasis on covering alt-country and indie acts such as Wilco, Steve Earle, The New Pornographers, The Shins, and even more established acts such as Tom Petty. Despite this, it still maintains a section devoted to free jazz and obscure electronic-based music in each issue. It has also done long articles on jazz icons Albert Ayler, Ken Vandermark, and Ornette Coleman. The photographic style of subjects has also evolved from inventive avant-garde settings to stark, no-frills closeups of band members. Magnet has paid much less attention over the years to metal and gangsta rap.","News");


}

    @After
    public void destroy() throws AvroRemoteException {
        //service.reset();
    }

    @Test
    public void train() throws IOException, InterruptedException {

        learnerService.reset();

        AtomicInteger counter = new AtomicInteger();

        texts.entrySet().stream().forEach( text -> {
            Document document = Document.newBuilder().setId(text.getKey().substring(0,10)).setName(text.getKey().substring(0,10)).setLabels(Arrays.asList(new String[]{text.getValue()})).setText(text.getKey()).build();
            try {
                learnerService.addDocument(document,true,false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Map<String,String> parameters = new HashMap<>();

        parameters.put("topics","5");
        parameters.put("pos","NOUN VERB ADJECTIVE");
        parameters.put("minfreq","1");
        parameters.put("maxdocratio","0.95");
        parameters.put("algorithm","llda");
        parameters.put("iterations","100");
        parameters.put("inference","true");
        parameters.put("multiwords","false");


        String result = learnerService.train(parameters);

        LOG.info("Result: " + result);

        LOG.info("Waiting for finish");
        Boolean completed = false;

        while(!completed){
            Thread.sleep(1000);
            completed = !modelerService.getTopics().isEmpty();
        }

        List<TopicSummary> topics = modelerService.getTopics();

        LOG.info("Topics: " + topics);




    }
}