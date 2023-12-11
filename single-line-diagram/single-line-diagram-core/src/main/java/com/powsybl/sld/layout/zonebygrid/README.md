# Zone Matrix Layout

On veut afficher toutes les "substations" d'une zone en ligne et en colonne.<BR>
En choississant l'emplacement de chaque "Substation".

## Paramètres d'entrée

- `VoltageLevelLayoutFactory`: donne le "layout" des "voltagelevels"<br>
- `SubstationLayoutFactory`: donne le "layout" des "substations"<br>
- `2D String array`: donne la position matricielle des "substations" de la zone<br>

**Exemple d'utilisation:**<BR>
Voici une zone constituée de 5 "substations" représenté sur 3 colonnes et 2 lignes.<BR>
Avec une celulle vide au milieu de la seconde ligne.
```java
// build zone graph
Network network = ...
List<String> zone = Arrays.asList("A", "B", "C", "D", "E");
ZoneGraph g = new NetworkGraphBuilder(network).buildZoneGraph(zone);

// Create substation 2D array representation
String[][] substationsIds = {{"A", "B", "C"},
                             {"D", "", "E"}};
// Create matrix zone layout using 2D array
Layout matrixLayout = new MatrixZoneLayoutFactory().create(g, substationsIds, new HorizontalSubstationLayoutFactory(), new PositionVoltageLevelLayoutFactory());
// Apply matrix zone layout
matrixLayout.run(layoutParameters);
```

## Principe de fonctionnement

### Postulas de départ: 
- Toutes les lignes de la matrice ont la même hauteur
- Toutes les colonnes de la matrice ont la même largeur
- Chaque lignes sont espacées de `LayoutParameters.getMatrixLayoutPadding` : TOP et BOTTOM
- Chaque colonnes sont espacées de `LayoutParameters.getMatrixLayoutPadding` : LEFT et RIGHT

Exemple:

|          |       |    Top     |        |           |          |       |    Top     |       |   |   |       |    Top     |       |   |
|:--------:|:-----:|:----------:|:------:|:---------:|:--------:|:-----:|:----------:|:-----:|:-:|:-:|:-----:|:----------:|:-----:|:-:|
|          | __X__ |   __X__    | __X__  |           |          | __X__ |   __X__    | __X__ |   |   | __X__ |   __X__    | __X__ |   |
| __Left__ | __X__ |   __A__    | __X__  | __Right__ | __Left__ | __X__ |   __B__    | __X__ |   |   | __X__ |   __C__    | __X__ |   |
|          | __X__ |   __X__    | __X__  |           |          | __X__ |   __X__    | __X__ |   |   | __X__ |   __X__    | __X__ |   |
|          |       | __Bottom__ |        |           |          |       | __Bottom__ |       |   |   |       | __Bottom__ |       |   |
|          |       |  __Top__   |        |           |          |       |  __Top__   |       |   |   |       |  __Top__   |       |   |
|          | __X__ |   __X__    | __X__  |           |          | __X__ |   __X__    | __X__ |   |   | __X__ |   __X__    | __X__ |   |
| __Left__ | __X__ |   __D__    | __X__  | __Right__ | __Left__ | __X__ |     _      | __X__ |   |   | __X__ |   __E__    | __X__ |   |
|          | __X__ |   __X__    | __X__  |           |          | __X__ |   __X__    | __X__ |   |   | __X__ |   __X__    | __X__ |   |
|          |       | __Bottom__ |        |           |          |       | __Bottom__ |       |   |   |       | __Bottom__ |       |   |


La classe représentant le "layout" matriciel est la suivante:
```java
class MatrixZoneLayout
```
On positionne chaque "substation" dans la ligne et la colonne spécifiée par l'utilisateur. 
```java
protected void calculateCoordSubstations(LayoutParameters layoutParameters) {
```
On applique le `SubstationLayout` sur chacune des "substations" de la zone.
Chaque `SubstationGraph` est ajouté au modèle du layout matriciel.
```java

    for (int row = 0; row < matrix.length; row++) {
        for (int col = 0; col < matrix[row].length; col++) {
            String id = matrix[row][col];
            SubstationGraph graph = getGraph().getSubstationGraph(id);
            if (graph != null) {
                // Display substations
                layoutBySubstation.get(graph).run(layoutParameters);
            }
            model.addGraph(graph, col, row);
        }
    }
    ...
```

### Cacul d'une snakeline entre substation

On représente le diagram de sortie avec la classe `Grid`,
qui se constitue d'une liste de `Node` sous la forme d'un tableau à 2 dimensions.
Chaque `Node` possède sa position en coordonées carthésiennes et un coût de traversée.

#### Calcul d'une zone d'exclusion
Une zone d'exclusion est un ensemble de `Node` dont le coût est affecté à `-1` 
Cette zone est interdite pour le tracé d'une `snakeline`.
Cette zone permet à la snakeline d'éviter:
- les voltagelevels
- les zones de la matrice vides (substations absentes)
- les snakelines précédentes

Exemple:


#### Calcul du chemin le plus court

Voici les différentes étapes :
* on applique un coût nulle au point de départ
* on cherche les voisins proches (gauche, droite, haut et bas) : pas de déplacement en diagonale
  * ces voisins sont considérés seulement si
    * le voisin est disponible (cost != -1)
    * le voisin n'a pas déjà était considéré
  * si le choix du voisin produit un angle droit alors 