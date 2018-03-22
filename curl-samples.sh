#!/usr/bin/env bash
curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"id": "doc1",  "labels": [], "name": "doc1",  "text": "3 of Hearts is the self-titled debut studio album by the American group 3 of Hearts, released on March 6, 2001, through the record label RCA Nashville"}' http://localhost:8080/documents
curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"id": "doc2",  "labels": [], "name": "doc2",  "text": "The Shape of Water is a 2017 American fantasy drama film directed by Guillermo del Toro and written by del Toro and Vanessa Taylor"}' http://localhost:8080/documents
curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"id": "doc3",  "labels": [], "name": "doc3",  "text": "Guillermo del Toro Gómez is a Mexican film director, screenwriter, producer, and novelist"}' http://localhost:8080/documents
curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"id": "doc4",  "labels": [], "name": "doc4",  "text": "Blade II is a 2002 American superhero film based on the fictional character of the same name from Marvel Comics"}' http://localhost:8080/documents
curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"id": "doc5",  "labels": [], "name": "doc5",  "text": "Blade: Trinity (also known as Blade III or Blade III: Trinity) is a 2004 American superhero film written, produced and directed by David S. Goyer, who also wrote the screenplays to Blade and Blade II"}' http://localhost:8080/documents

curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{"parameters": { "topics":"2"}}' http://localhost:8080/dimensions


curl -i --user learner:oeg2018 -H "Content-Type: application/json" -H "Accept: application/json" -X GET http://localhost:8080/dimensions