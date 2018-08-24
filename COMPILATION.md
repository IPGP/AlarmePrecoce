# Compilation

## Installations des pré-requis

Pour la compilation de l'application, les pré-requis sont

* L'environnement de développement Java (JDK) à partir de la version 1.7 (https://www.java.com).
* L'outil de gestion et d'automatisation de production des projets logiciels Maven (https://maven.apache.org).
* L'outil de production GNU make (https://www.gnu.org/software/make/).

Par exemple sous Debian

```sh
apt-get install openjdk-8-jre maven
```

## Récupération des sources de l’application

Les sources se récupèrent depuis le dépôt GitHub https://github.com/IPGP/AlarmePrecoce

```sh
git clone https://github.com/IPGP/AlarmePrecoce.git
```

## Réinitialisation du dépôt Maven local

Dans de rares cas, des problèmes de compilation peuvent survenir avec Maven. La réinitialisation du dépôt local peut résoudre ces problèmes.

```sh
rm -rf ~/.m2
```

## Utilisation de Maven

Les commandes Maven doivent être exécutées depuis le dossier EarlyWarning. Les principales actions sont encapsulées dans des commandes make.

### Lecture et installation des dépendances

Grâce au fichier pom.xml Maven peut déterminer l’arbre des dépendances de l’artefact EarlyWarning.
Le logiciel utilise deux types de dépendances :

* Des dépendances libres (Apache Commons, LF4J) ;
* Des dépendances propriétaires ou internes.

Les premières peuvent directement être récupérées et installées par Maven depuis le Maven Repository dans le local Maven repository.
Les secondes doivent cependant être installées par Maven depuis des archives JAR fournies avec le code source. Cette installation se fait grâce au plugin maven-install.

Pour installer les dépendances (libres et propriétaires), utiliser la commande :

```sh
mvn validate
```

### Tests unitaires

Les tests unitaires sont lancés avec la commande :

```sh
make test
```

### Packaging

Le packaging est lancé avec la commande :
```sh
make package
```

### Génération de la Javadoc

La création de la Javadoc est lancée la commande :

```sh
make javadoc
```

### Makefile

Le Makefile a plusieurs autres cibles, pour les connaître, lancer :

```sh
make help
```
