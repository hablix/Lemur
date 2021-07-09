## Projektbericht VL Virtuelle Realität
_Hannes Bischoff_



#### Inhaltsverzeichnis

* Motivation
* Technologie
  * Kernaufgaben
    * Erkennen des Anschlags
    * Richtungserkennung
  * Sound
  * Layout
* Ausblick
* Referenz
* Quellen
* Bildnachweis


#### Beleg




# Motivation

Projektidee war eine App für ein Android Smartphone zu entwickeln die Röhrenglocken abbildet.
Das Instrument der Röhrenglocken gehört zu den Idiophonen wird seit Ende des 19. Jahrhunderts auch in Orchestern verwendet. Um es zu spielen schlägt der Schlagwerker mit einem kleinen Hämmerchen gegen eines der Röhren. Diese sind so aufgehangen das diese frei schwingen können und dabei klingen. Die meisten Röhrenglocken bestehen aus 8, 12 oder 20 Röhren.
Ziel diese Projektes ist es eine einfache, günstige Simulation dieses Spielverhaltens zu erzeugen. Die verschiedenen Sensoren sollen dabei als Trigger dienen. Imitiert man mit dem Smartphone einen Schlag, so soll analog zum anschlag mit dem Hammer ein Ton erklingen. Wird eine andere Röhre angeschlagen ändert sich die Tonhöhe, auch das soll abgebildet werden. Das wird so gelöst das abhängig von der Richtung in der das Smartphone beim Schlag gehalten wird sich die Tonhöhe ändert.

Ein Schlagwerker an den Röhrenglocken 1917 [https://de.wikipedia.org/wiki/R%C3%B6hrenglocken
](https://de.wikipedia.org/wiki/R%C3%B6hrenglocken
)

![image](https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Chimes_%28IMSO_pp55%29.jpg/255px-Chimes_%28IMSO_pp55%29.jpg)

# Technologie
## Grundlagen

Zentrales Element der Simulation ist das Smartphone. Dank seiner kompakten größe, handlichkeit und der eingebauten Sensorik lässt es sich vielseitig verwenden und ist hierfür ideal geeignet. Außerdem ist keine Anschaffung spezieller Hardware nötig wenn man handelsübliches Android Smartphone besitzt.

Die Wahl fiel auf die Android Plattform, da diese im Kern ein open-source source Betriebssystem ist und es unzählig freie Software und Tools für die Entwicklung von Apps gibt.

Der Programmcode dieses Projektes ist in Java geschrieben. Es wird als Entwicklungsumgebung das freie Android Studio mit den dazugehörigen Android Developer Kits genutzt. Layoutdateien und Design des Projektes sind androidtypisch in xml.



Um die Notwendigen und recht umfangreichen Vorarbeiten einer Android app zu verkürzen habe ich auf ein bereits existierendes Projekt mit offenem Quellcode zur auswertung der verschiedene Sensoren zurückgegriffen. Das Projekt Lemur auf github, schien dafür ideal geeignet zu sein. Es bedarf natürlich einiger Anpassungen aber verkürzte die Arbeitsaufwand immens. Layout, Grundeinstellungen, App interne Navigation und Berechtigungen systemseitig anzufragen können mitunter bei einer App, die von grund auf neu entwickelt wird mehrere Tage arbeitsaufwand bedeuten bevor auch nur eine Zeile des gewünschten Algorithmuses geschrieben werden kann.

## Kernaufgaben

Von allem Überbau jeglicher Android Projekte abgesehen, lassen sich zwei Schwerpunkte herauskristallisieren.

Das verwendete Basisprojekt liest diverse Sensorwerte über die Systemschnittstellen aus und kann diese grafisch anschaulich plotten. Die grafische darstellung der werte ist besonders nützlich um Charakteristik und Wertebereich eines ereignisses zu erkennen.

Die meisten heutzutage verkauften Smartphones verfügen über mehrere eingebaute Bewegungssensoren. In der Android Entwicklerdokumentation https://developer.android.com/guide/topics/sensors/sensors_motion sind diese beschrieben. Einige Sensoren liefern physikalische Messwerte wie z.B. Beschleunigungssensor und Gyroskop. Andere Sensoren existiere nur virtuell. der Rotationsvektor oder Schrittzähler sind Softwareimplementierungen und nutzen die Daten der anderen Sensoren und kombinieren diese und vorverarbeiten die daten.


![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie1.PNG)

### 1. Erkennen des Anschlags

Dazu wird das Telefon in einer Kippbewegung um die Z-Achse bzw in der von der Y und X-Achsen aufgespannten Displayebene nach links bewegt. Dieses Ereignis kann mit den im Smartphone eingebauten Beschleunigungssensoren erfasst werden. Der Sensor liefert Werte für Beschleunigung in der X,Y, und Z-Achse. Um die Ausgelesenen Werte zu validieren genügt ein simpler Test. Steht das Gerät senkrecht auf mit der Unterkante auf einem Tisch und wird nicht bewegt liefert der Sensor im idealfall für die Y-Achse 9,8m/s² entsprechend der lokalen Erdbeschleunigung und die Sensoren für die X und Z-Achse je 0 m/s².


![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie2.PNG)


Graphisch ist der Ausschlag sehr gut zu sehen. Im nächsten Schritt muss eine geeignete Methode entwickelt und im Algorithmus umgesetzt werden um aus der Zeitreihe das Muster zuverlässig zu erkennen. Das vom sensor gelieferte Signal ist zeitdiskret mit konstanter Abtastzeit und wertdiskret. Die Quantisierungsschritte sind sehr klein als Fließkommazahl im Java datentyp double.

Als praktikabel und zuverlässig hat sich folgende Methode herausgestellt.

Der Signalwert der X-Achse wird bei jeder Iteration mit dem wert der vorherigen Iteration verglichen. Wenn der aktuelle größer ist, also die Werte ansteigen, und dennoch unterhalb der unteren Schranke liegen, wird Trigger 1 ausgelöst. Dieser Trigger öffnet ein 300ms langes Zeitfenster (grün). Wird Trigger 1 mehrfach ausgelöst spielt das keine Rolle, dieser überschreibt den Vorherigen. Der Trigger 1 ist notwendig aber noch nicht hinreichend um den Anschlag zu erkennen. Im geöffneten Zeitfenster, wird das sSignal nun auf eine fallende Flanke untersucht. Sobald das Signal fällt also der vorherige Signalwert größer als der aktuelle ist und sich die Werte oberhalb der oberen Schranke befinden, wird dies als Maximum detektiert. Die Festlegung der Schranken dienen dazu sehr kleine Ausschläge zu ignorieren.

![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie3.PNG)


### 2. Richtungserkennung

Mithilfe des virtuellen Richtungsvektor sensors ist eine Bestimmung der orientierung des Gerätes im raum möglich. Wird das smartphone gedreht und verharrt dann in dieser position wird dies trotzdem erkannt und es wird eine angegeben in welchem winkel sich das smartphone befindet. Relativ zur angenommenen erdoberfläche und der magnetischen orientierung. Der software sensor bezieht daten aus verschiedenen quellen wie: Beschleunigungsmesser, Magnetfelddetektor und Gyroskop. Fehlt einer dieser sensoren am Gerät, fällt die genauigkeit der werte dementsprechend deutlich schlechter aus.

Für sie simulation wird die X-Z-Ebene in 7 gleich große Felder zu je 51,4° eingeteilt die jeweils einen ton repräsentieren. Je nachdem welchen winkel der Sensor für die drehung um die Y-Achse liefert wird der ton gespielt.

![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie4.PNG)

Wird ein maximum detektiert wird Je nachdem welchen winkel der Richtungsvektor Sensor für die drehung um die Y-Achse zur gleichen zeit liefert wird der dazugehörige ton gespielt.


## Sound

Für die Klang wird mangels vorhandensein von Röhrenglocken und der schwierigkeit ein frei verwendbares audio file einer Tonleiter des besagten instruments zu finden auf klangschalen ausgewichen. Das verwendete audio file ist stereo und von der website freesound. https://freesound.org/people/mooncubedesign/sounds/347975/

Die aufnahme der 12-Teiligen Tonleiter geht in halbtonschritten von C#³ bis C#⁴ und weist eine sehr gute qualität auf. Im Frequenzspektrum sind grund und obertöne gut zu erkennen. Es tritt fast kein rauschen auf, wie im Spektrum zu sehen ist.

![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie5.PNG)


Die Klangschale klingt für ca 12 sekunden, was hier für die App nicht besonders nicht hilfreich war. in audacity wurde das Audiosignal getrennt und die einzelne Töne herausgeschnitten in einzelnen foundfiles gepackt um diese separat abspielen zu können und die Hüllkurve der amplitude so moduliert, dass der ton nach 2,5 sek ausklingt ohne abrupt zu enden.

![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie6.PNG)

Da es durch die genauigkeit der Richtungsbestimmung durch den Sensor nicht sinnvoll möglich ist eine umdrehung von 360° in 12 Sektoren zu teilen können in dieser version leider nicht alle Töne der tonleiter untergebracht werden.
Die verwendeten töne im aktuellen projekt sind c#³, d³, d#³, e³, f³, f#³, g³. 7 erwies sich als gerade noch praktikable umsetzung um gezielt einen gewünschten sektor zu treffen und zu spielen. Eine weitere unterteilung für mehr töne würde eventuell mit der unterscheidung ob ein anschlag nach ober oder unten geschiet unterschieden werden können, nicht jedoch durch weitere sektoren.


## App layout

Die Activity mit der Röhrenglocken Simulation ist in das Konstrukt der Basis app integriert. Das layout ist schlicht gehalten und zeigt je nach orientierung des Smartphones an welcher ton gespielt würde, wenn das gerät jetzt zum schlag angesetzt wird. Dabei hilft zusätzlich die farbliche unterstützung in verschiedenen orange tönen. Die Gradangabe ist basis für die unterteilung der Sektoren, jedoch nicht mit der ausrichtung nach Norden übereinstimmen. Das Diagramm im unteren bereich dient nur der information und zu debugging zwecken. es veranschaulicht die Beschleunigungskräfte die Auf das telefon im moment des anschlages reagieren. Mitunter können beschleunigungen von 3 bis 4 g gemessen werden.

![image](https://raw.githubusercontent.com/hablix/Lemur/master/grafiken/Folie7.PNG)

# Ausblick

Das rotationssymmetrische erweist sich mitunter als unpraktisch, da zum teil große drehbewegungen nötig sind um von einem zum andern Ton zu wechseln.

in einer weiteren version könnten mehr Töne durch einführung einer virtuellen ebene geschaffen werden. z.B. anschlag nach oben, anschlag nach unten.



# Referenz
Projektverzeichnis der Simulation auf Github, hier kann der Gesamte Quellcode eingesehen werden und auch reproduziert werden:


[https://github.com/hablix/Lemur](https://github.com/hablix/Lemur)

# Quellen
Basis projekt welches die grundlage für die simulation ist auf Github: [https://github.com/Roslund/Lemur](https://github.com/Roslund/Lemur)

verwendeter soundfile: [https://freesound.org/people/mooncubedesign/sounds/347975/](https://freesound.org/people/mooncubedesign/sounds/347975/)

Entwicklerdoku Senroren: https://developer.android.com/guide/topics/sensors/sensors_motion

Entwicklerdoku Kordinatensystem Sensoren: https://developer.android.com/guide/topics/sensors/sensors_overview.html#sensors-coords

Entwicklingsumgebung Android studio: https://developer.android.com/studio

Audio editor Audacity: https://www.audacityteam.org/



# Bildnachweis
Achsen smartphone: [https://jp.mathworks.com/help/supportpkg/android/ref/gyroscope.html](https://jp.mathworks.com/help/supportpkg/android/ref/gyroscope.html)

Röhrenglocken: [https://de.wikipedia.org/wiki/R%C3%B6hrenglocken#/media/Datei:Chimes_(IMSO_pp55).jpg](https://de.wikipedia.org/wiki/R%C3%B6hrenglocken#/media/Datei:Chimes_(IMSO_pp55).jpg)
