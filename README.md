UIMA DEMO
=========

A small UIMA demo combining OpenNLP with RuTA through UIMA.

Provided by KeaText (http://keatext.com) for the Big Data Week Montreal (2015).

Public Domain.

To run:

$ mvn clean install assembly:single

$ java -jar target/uimademo-0.0.1-SNAPSHOT.jar 8080


$ echo "I want to give $ 50 to Joe" |  curl -s -XPUT http://localhost:8080/ --data-binary @/dev/stdin

[{"transfers":["give $ 50 to"],"percentages":[],"moneys":["$ 50 to"],"times":[],"dates":[],"locations":[],"organizations":[],"tokens":["I","want","to","give","$","50","to","Joe"],"people":["Joe"]}]
