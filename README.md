# learnerTopics-service
Discover hidden topics from textual documents

## Algorithms Parameters

| Param    | Default  | Description |
| :------- |:--------:| :---------- |
| alpha    | 50/topics    | hyperparameter |
| beta     | 0.1      | hyperparameter |
| topics   | 10       | num topics |
| iterations   | 1000       | num iterations |
| language   | *from text*       | text language |
| pos   | NOUN VERB ADJECTIVE       | part-of-speech |
| stopwords   | *empty*       | words separated by whitespace |
| minfreq   |        | words separated by whitespace |
           
            
            if (parameters.containsKey("topwords"))     ldaParameters.setNumTopWords(Integer.valueOf(parameters.get("topwords")));
           
            if (parameters.containsKey("minfreq"))      ldaParameters.setMinFreq(Integer.valueOf(parameters.get("minfreq")));
            if (parameters.containsKey("maxdocratio"))  ldaParameters.setMaxDocRatio(Double.valueOf(parameters.get("maxdocratio")));
            if (parameters.containsKey("raw"))          ldaParameters.setRaw(Boolean.valueOf(parameters.get("raw")));
            if (parameters.containsKey("inference"))    ldaParameters.setInference(Boolean.valueOf(parameters.get("inference")));
            if (parameters.containsKey("multigrams"))   ldaParameters.setEntities(Boolean.valueOf(parameters.get("multigrams")));
            if (parameters.containsKey("entities"))     ldaParameters.setEntities(Boolean.valueOf(parameters.get("entities")));
            if (parameters.containsKey("seed"))         ldaParameters.setSeed(Integer.valueOf(parameters.get("seed")));
