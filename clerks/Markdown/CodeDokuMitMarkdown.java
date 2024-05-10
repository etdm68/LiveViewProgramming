
Clerk.markdown(STR."""
# Die Code-Dokumentation mit Markdown

Für die Code-Dokumentation mit Markdown sind String-Templates und der Text-Skill entscheidende Hilfsmittel. Zum einen erlauben sie, Code in Markdown einzubinden, zum anderen, den Gebrauch von "spitzen" Klammern (`<>`) zu ermöglichen.
""");

// Fakultätsfunktion
long factorial(int n) {
    assert n >= 0 : "Positive Ganzzahl erforderlich";
    if (n == 0 || n == 1) return n;
    return n * factorial(n - 1);
}
// Testfälle
assert factorial(0) == 1 && factorial(1) == 1;
assert factorial(2) == 2 && factorial(3) == 6;
assert factorial(4) == 24 && factorial(5) == 120;
// Ende Fakultätsfunktion

// STR-Beispiel
int num;
String s = STR."Die Fakultät von \{num = 6} ist \{factorial(num)}."
// STR-Beispiel


Clerk.markdown(STR."""
## String-Templates mit eingebetteten Ausdrücken

Mit Java 21 haben String-Templates als Preview-Feature Einzug in Java gehalten ‒ mehr Informationen zu diesem neuen Sprachkonstrukt finden sich [hier](https://docs.oracle.com/en/java/javase/22/language/string-templates.html). Mit String-Templates lassen sich Ausdrücke zur Auswertung in einen String einbinden.

Ein Beispiel: Der Template-Prozessor `STR` bekommt einen String übergeben mit zwei eingebetteten Ausdrücken, die jeweils durch `\\{` und `}`markiert sind. Die Ergebnisse der Auswertung werden vom Template-Prozessor in den resultierenden String eingefügt.

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "// STR-Beispiel")}
```

Das Ergebnis der Zeichenkette ist

```
\{s}
```

String-Templates bieten die Basis, um auf einfache Weise ganze Textauszüge in den Markdown-Text, der mit `Clerk.markdown()` erzeugt wird, einzubinden.
""");

Clerk.markdown(STR."""
## Texte ausschneiden mit `Text.cut`

Mit dem Skill `Text` kann Text aus einer Datei ausgeschnitten werden. Der Methodenkopf von `cutOut` erwartet einen Dateinamen, zwei boolsche Werte und eine beliebige Anzahl an Labels.

```
static String cutOut(String fileName, boolean includeStartLabel, boolean includeEndLabel, String... labels)
```

Labels sind Zeichenketten, nach denen als vollständige Textzeile in der angegebenen Datei gesucht wird. Mit den boolschen Werten wird angegeben, ob das öffnende bzw. schliessende Label beim Ausschnitt mit inkludiert werden soll.

Nehmen wir als Beispiel eine Datei mit folgendem Inhalt:

```
\{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java", "// STR-Beispiel")}
```

""");

/**
// Gesamtes CutOut-Beispiel
// LabelA
1. Textstelle, gerahmt von einem LabelA und einem LabelB
// LabelB
// LabelC
Textstelle, umschlossen von einem LabelC
// LabelC
// LabelA
2. Textstelle, gerahmt von einem LabelA und einem LabelB
// LabelB
// Gesamtes CutOut-Beispiel
*/



// Den vom `LabelC` umschlossenen Text bekommt man mit

// ```
// \{Text.cutOut("clerks/Markdown/CodeDokuMitMarkdown.java"), false, false, "// LabelC"}
// ```
